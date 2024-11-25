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

package com.pyamsoft.tetherfi.service.lock

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import androidx.annotation.CheckResult
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tetherfi.core.Timber
import com.pyamsoft.tetherfi.service.ServicePreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
internal class WakeLocker
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    context: Context,
    private val preferences: ServicePreferences,
) : AbstractLocker() {

  private val mutex = Mutex()
  private val tag = createTag(context.packageName)

  private val lock by lazy {
    enforcer.assertOffMainThread()

    val powerManager = context.getSystemService<PowerManager>().requireNotNull()
    return@lazy powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag)
  }

  private val wakeAcquired = MutableStateFlow(false)

  @SuppressLint("WakelockTimeout")
  override suspend fun acquireLock() =
      withContext(context = Dispatchers.Default) {
        withContext(context = NonCancellable) {
          mutex.withLock {
            if (wakeAcquired.compareAndSet(expect = false, update = true)) {
              Timber.d { "####################################" }
              Timber.d { "Acquire CPU wakelock: $tag" }
              Timber.d { "####################################" }
              lock.acquire()
            }
          }
        }
      }

  override suspend fun releaseLock() =
      withContext(context = Dispatchers.Default) {
        withContext(context = NonCancellable) {
          mutex.withLock {
            if (wakeAcquired.compareAndSet(expect = true, update = false)) {
              Timber.d { "####################################" }
              Timber.d { "Release CPU wakelock: $tag" }
              Timber.d { "####################################" }
              lock.release()
            }
          }
        }
      }

  override suspend fun isEnabled(): Boolean =
      withContext(context = Dispatchers.Default) { preferences.listenForWakeLockChanges().first() }

  companion object {

    @JvmStatic
    @CheckResult
    private fun createTag(name: String): String {
      return "${name}:PROXY_WAKE_LOCK"
    }
  }
}
