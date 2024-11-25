/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tetherfi.service.notification

import android.app.Service
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.toNotifyId
import com.pyamsoft.tetherfi.core.Timber
import com.pyamsoft.tetherfi.server.broadcast.BroadcastNetworkStatus
import com.pyamsoft.tetherfi.server.clients.BlockedClients
import com.pyamsoft.tetherfi.server.clients.SeenClients
import com.pyamsoft.tetherfi.server.status.RunningStatus
import com.pyamsoft.tetherfi.service.ServiceInternalApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
internal class NotificationLauncherImpl
@Inject
internal constructor(
    @ServiceInternalApi private val notifier: Notifier,
    private val enforcer: ThreadEnforcer,
    private val networkStatus: BroadcastNetworkStatus,
    private val seenClients: SeenClients,
    private val blockedClients: BlockedClients,
) : NotificationLauncher {

  private val showing = MutableStateFlow(false)

  private val clientCount = MutableStateFlow(0)
  private val blockCount = MutableStateFlow(0)
  private val runningStatus = MutableStateFlow<RunningStatus>(RunningStatus.NotRunning)

  private suspend fun onStatusUpdated(
      status: RunningStatus,
      clientCount: Int,
      blockCount: Int,
  ) {
    if (!showing.value) {
      Timber.w { "Do not update notification, no longer showing!" }
      return
    }

    withContext(context = Dispatchers.Main) {
      val data =
          ServerNotificationData(
              status = status,
              clientCount = clientCount,
              blockCount = blockCount,
          )

      notifier
          .show(
              id = NOTIFICATION_ID,
              channelInfo = CHANNEL_INFO,
              notification = data,
          )
          .also { Timber.d { "Updated foreground notification: $it: $data" } }
    }
  }

  @CheckResult
  private fun <T> MutableStateFlow<T>.compareCurrent(update: T): Boolean {
    val current = this.value
    if (current != update) {
      this.value = update
      return true
    }

    return false
  }

  private suspend fun updateNotification() {
    onStatusUpdated(
        status = runningStatus.value,
        clientCount = clientCount.value,
        blockCount = blockCount.value,
    )
  }

  private fun CoroutineScope.watchNotification() {
    val scope = this

    // Supervisor job will cancel all children
    scope.launch {
      enforcer.assertOffMainThread()

      // Listen for notification updates
      networkStatus.onStatusChanged().also { f ->
        launch {
          enforcer.assertOffMainThread()

          f.collect { s ->
            if (runningStatus.compareCurrent(s)) {
              updateNotification()
            }
          }
        }
      }

      // Listen for client updates
      seenClients
          .listenForClients()
          .map { it.size }
          .also { f ->
            launch {
              enforcer.assertOffMainThread()

              f.collect { c ->
                if (clientCount.compareCurrent(c)) {
                  updateNotification()
                }
              }
            }
          }

      // Listen for block updates
      blockedClients
          .listenForBlocked()
          .map { it.size }
          .also { f ->
            launch {
              enforcer.assertOffMainThread()

              f.collect { b ->
                if (blockCount.compareCurrent(b)) {
                  updateNotification()
                }
              }
            }
          }
    }
  }

  private fun reset() {
    clientCount.value = 0
    blockCount.value = 0
    runningStatus.value = RunningStatus.NotRunning
  }

  private fun stop(service: Service) {
    enforcer.assertOnMainThread()

    Timber.d { "Stop foreground notification" }
    notifier.stopForeground(service, NOTIFICATION_ID)

    reset()
  }

  override suspend fun update() =
      withContext(context = Dispatchers.Default) {
        if (!showing.value) {
          Timber.w { "Cannot update notification since not showing" }
          return@withContext
        }

        updateNotification()
      }

  override suspend fun startForeground(service: Service) =
      withContext(context = Dispatchers.Default) {
        val scope = this

        if (showing.compareAndSet(expect = false, update = true)) {
          try {
            // Hold this here until the coroutine is cancelled
            coroutineScope {
              reset()

              withContext(context = Dispatchers.Main) {
                val data = DEFAULT_DATA

                // Initialize with blank data first
                notifier
                    .startForeground(
                        service = service,
                        id = NOTIFICATION_ID,
                        channelInfo = CHANNEL_INFO,
                        notification = data,
                    )
                    .also { Timber.d { "Started foreground notification: $it: $data" } }
              }

              // Then immediately open a channel to update
              scope.watchNotification()

              // And suspend until we are done
              awaitCancellation()
            }
          } finally {
            withContext(context = NonCancellable) {
              if (showing.compareAndSet(expect = true, update = false)) {
                withContext(context = Dispatchers.Main) { stop(service) }
              }
            }
          }
        }
      }

  companion object {

    private val NOTIFICATION_ID = 42069.toNotifyId()

    private val DEFAULT_DATA =
        ServerNotificationData(
            status = RunningStatus.NotRunning,
            clientCount = 0,
            blockCount = 0,
        )

    private val CHANNEL_INFO =
        NotifyChannelInfo(
            id = "channel_tetherfi_service_1",
            title = "TetherFi Proxy",
            description = "TetherFi Proxy Service",
        )
  }
}
