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

import androidx.annotation.CheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

internal abstract class AbstractLocker protected constructor() : Locker {

  protected abstract suspend fun acquireLock()

  protected abstract suspend fun releaseLock()

  @CheckResult protected abstract suspend fun isEnabled(): Boolean

  final override suspend fun acquire() =
      withContext(context = NonCancellable) {
        withContext(context = Dispatchers.Default) {
          releaseLock()

          if (isEnabled()) {
            acquireLock()
          }
        }
      }

  final override suspend fun release() =
      withContext(context = NonCancellable) {
        withContext(context = Dispatchers.Default) { releaseLock() }
      }
}
