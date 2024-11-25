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

package com.pyamsoft.tetherfi.status.sections.network

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tetherfi.status.StatusViewState
import com.pyamsoft.tetherfi.status.sections.tiiles.RunningTiles
import com.pyamsoft.tetherfi.ui.ServerViewState

private enum class RenderRunningItemsContentTypes {
  VIEW_HOWTO,
  VIEW_SSID,
  VIEW_PASSWD,
  VIEW_PROXY,
  TILES,
}

internal fun LazyListScope.renderRunningItems(
    modifier: Modifier = Modifier,
    state: StatusViewState,
    serverViewState: ServerViewState,
    onTogglePasswordVisibility: () -> Unit,
    onShowQRCode: () -> Unit,
    onRefreshConnection: () -> Unit,
    onShowHotspotError: () -> Unit,
    onShowNetworkError: () -> Unit,
    onJumpToHowTo: () -> Unit,
) {
  item(
      contentType = RenderRunningItemsContentTypes.VIEW_HOWTO,
  ) {
    ViewInstructions(
        modifier = modifier.padding(bottom = MaterialTheme.keylines.content * 2),
        onJumpToHowTo = onJumpToHowTo,
    )
  }

  item(
      contentType = RenderRunningItemsContentTypes.VIEW_SSID,
  ) {
    ViewSsid(
        modifier = modifier.padding(bottom = MaterialTheme.keylines.baseline),
        serverViewState = serverViewState,
    )
  }

  item(
      contentType = RenderRunningItemsContentTypes.VIEW_PASSWD,
  ) {
    ViewPassword(
        modifier = modifier.padding(bottom = MaterialTheme.keylines.baseline),
        state = state,
        serverViewState = serverViewState,
        onTogglePasswordVisibility = onTogglePasswordVisibility,
    )
  }

  item(
      contentType = RenderRunningItemsContentTypes.VIEW_PROXY,
  ) {
    ViewProxy(
        modifier = modifier.padding(bottom = MaterialTheme.keylines.baseline),
        serverViewState = serverViewState,
    )
  }

  item(
      contentType = RenderRunningItemsContentTypes.TILES,
  ) {
    RunningTiles(
        modifier = modifier,
        serverViewState = serverViewState,
        onShowQRCode = onShowQRCode,
        onRefreshConnection = onRefreshConnection,
        onShowHotspotError = onShowHotspotError,
        onShowNetworkError = onShowNetworkError,
    )
  }
}
