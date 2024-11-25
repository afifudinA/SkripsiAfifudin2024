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

package com.pyamsoft.tetherfi.status.sections.broadcast

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager
import com.pyamsoft.pydroid.ui.theme.ZeroSize
import com.pyamsoft.tetherfi.server.ServerDefaults
import com.pyamsoft.tetherfi.server.ServerNetworkBand
import com.pyamsoft.tetherfi.status.StatusViewState
import com.pyamsoft.tetherfi.ui.Label
import com.pyamsoft.tetherfi.ui.checkable.CheckableCard
import com.pyamsoft.tetherfi.ui.checkable.rememberHeightMatcherGenerator

@Composable
internal fun NetworkBands(
    modifier: Modifier = Modifier,
    isEditable: Boolean,
    state: StatusViewState,
    onSelectBand: (ServerNetworkBand) -> Unit,
) {
  val band by state.band.collectAsStateWithLifecycle()
  val canUseCustomConfig = remember { ServerDefaults.canUseCustomConfig() }
  val hapticManager = LocalHapticManager.current

  Column(
      modifier = modifier,
  ) {

    // Small label above
    Label(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.baseline),
        text = "Broadcast Frequency",
    )
    if (canUseCustomConfig) {
      val bands = remember { ServerNetworkBand.entries }
      val bandIterator = remember(bands) { bands.withIndex() }
      val generator = rememberHeightMatcherGenerator<ServerNetworkBand>()

      // Then the buttons
      Row {
        for ((index, b) in bandIterator) {
          val isSelected = remember(b, band) { b == band }
          val heightMatcher = generator.generateFor(b)

          Box(
              modifier =
                  Modifier.weight(1F)
                      .then(heightMatcher.onSizeChangedModifier)
                      .padding(
                          end =
                              if (index < bands.lastIndex) MaterialTheme.keylines.content
                              else ZeroSize,
                      ),
          ) {
            CheckableCard(
                modifier = Modifier.fillMaxWidth(),
                isEditable = isEditable,
                condition = isSelected,
                title = b.displayName,
                description = b.description,
                extraHeight = heightMatcher.extraHeight,
                onClick = {
                  hapticManager?.toggleOn()
                  onSelectBand(b)
                },
            )
          }
        }
      }
    } else {
      Text(
          text = "Network Broadcast Frequency is defined by the system and cannot be changed.",
          style =
              MaterialTheme.typography.body1.copy(
                  fontWeight = FontWeight.W700,
                  color =
                      MaterialTheme.colors.onSurface.copy(
                          alpha = ContentAlpha.medium,
                      ),
              ),
      )
    }
  }
}
