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

package com.pyamsoft.tetherfi.info

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tetherfi.ui.LANDSCAPE_MAX_WIDTH
import com.pyamsoft.tetherfi.ui.ServerViewState
import com.pyamsoft.tetherfi.ui.TestServerViewState
import com.pyamsoft.tetherfi.ui.renderLinks
import com.pyamsoft.tetherfi.ui.renderPYDroidExtras

private enum class InfoContentTypes {
  SPACER,
  BOTTOM_SPACER
}

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    appName: String,
    state: InfoViewState,
    serverViewState: ServerViewState,
    onTogglePasswordVisibility: () -> Unit,
    onShowQRCode: () -> Unit,
) {
  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = MaterialTheme.keylines.content),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    renderPYDroidExtras(
        modifier = Modifier.widthIn(max = LANDSCAPE_MAX_WIDTH),
    )

    renderLinks(
        modifier = Modifier.widthIn(max = LANDSCAPE_MAX_WIDTH),
        appName = appName,
    )

    item(
        contentType = InfoContentTypes.SPACER,
    ) {
      Spacer(
          modifier =
              Modifier.padding(top = MaterialTheme.keylines.content)
                  .height(MaterialTheme.keylines.baseline),
      )
    }

    renderConnectionInstructions(
        itemModifier = Modifier.widthIn(max = LANDSCAPE_MAX_WIDTH),
        appName = appName,
        state = state,
        serverViewState = serverViewState,
        onTogglePasswordVisibility = onTogglePasswordVisibility,
        onShowQRCode = onShowQRCode,
    )

    item(
        contentType = InfoContentTypes.BOTTOM_SPACER,
    ) {
      Spacer(
          modifier = Modifier.padding(top = MaterialTheme.keylines.content).navigationBarsPadding(),
      )
    }
  }
}

@Preview
@Composable
private fun PreviewInfoScreen() {
  InfoScreen(
      appName = "TEST",
      state = MutableInfoViewState(),
      serverViewState = TestServerViewState(),
      onTogglePasswordVisibility = {},
      onShowQRCode = {},
  )
}
