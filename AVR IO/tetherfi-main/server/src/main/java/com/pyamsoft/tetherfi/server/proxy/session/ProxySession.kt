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

package com.pyamsoft.tetherfi.server.proxy.session

import androidx.annotation.CheckResult
import com.pyamsoft.tetherfi.server.proxy.ServerDispatcher
import kotlinx.coroutines.CoroutineScope

internal interface ProxySession<T : ProxyData> {

  suspend fun exchange(
      scope: CoroutineScope,
      serverDispatcher: ServerDispatcher,
      data: T,
  )

  interface Factory<T : ProxyData> {

    @CheckResult fun create(): ProxySession<T>
  }
}
