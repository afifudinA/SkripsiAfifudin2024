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

package com.pyamsoft.tetherfi.service

import android.app.Service
import com.pyamsoft.tetherfi.core.Timber
import com.pyamsoft.tetherfi.server.broadcast.BroadcastNetworkUpdater
import com.pyamsoft.tetherfi.server.broadcast.BroadcastObserver
import com.pyamsoft.tetherfi.service.foreground.ForegroundLauncher
import com.pyamsoft.tetherfi.service.foreground.ForegroundWatcher
import com.pyamsoft.tetherfi.service.notification.NotificationLauncher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class ServiceRunner
@Inject
internal constructor(
    private val notificationLauncher: NotificationLauncher,
    private val broadcastObserver: BroadcastObserver,
    private val foregroundWatcher: ForegroundWatcher,
    private val foregroundLauncher: ForegroundLauncher,
    private val serviceLauncher: ServiceLauncher,
    private val networkUpdater: BroadcastNetworkUpdater,
) {
  private val runningState = MutableStateFlow(false)

  private fun CoroutineScope.startProxy(service: Service) {
    val scope = this

    // Watch the Wifi Receiver for events
    // without this block, we do not properly refresh
    // CONNECTION and GROUP info and can lead to errors
    broadcastObserver.listenNetworkEvents().also { f ->
      scope.launch(context = Dispatchers.Default) {
        f.collect { networkUpdater.updateNetworkInfo() }
      }
    }

    // Start notification first for Android O immediately
    scope.launch(context = Dispatchers.Default) {
      notificationLauncher.startForeground(
          service = service,
      )
    }

    // Prepare proxy on create
    scope.launch(context = Dispatchers.Default) {
      foregroundWatcher.bind(
          onRefreshNotification = {
            Timber.d { "Refresh event received, start notification again" }
            notificationLauncher.update()
          },
          onShutdownService = {
            Timber.d { "Shutdown event received!" }
            withContext(context = Dispatchers.Main) { serviceLauncher.stopForeground() }
          },
      )
    }

    // And Start the proxy Wifi Direct and our HTTP server
    scope.launch(context = Dispatchers.Default) {
      Timber.d { "Starting Proxy!" }
      foregroundLauncher.startProxy()
    }
  }

  /** Start the proxy */
  suspend fun start(service: Service) =
      withContext(context = Dispatchers.Default) {
        if (runningState.compareAndSet(expect = false, update = true)) {
          try {
            Timber.d { "Starting runner!" }
            coroutineScope { startProxy(service = service) }
          } finally {
            withContext(context = NonCancellable) {
              if (runningState.compareAndSet(expect = true, update = false)) {
                Timber.d { "Stopping runner!" }
              }
            }
          }
        }
      }
}
