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

package com.pyamsoft.tetherfi.status.sections.tiiles

import androidx.compose.foundation.border
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.ui.defaults.CardDefaults

@Composable
internal fun StatusTile(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
  Card(
      modifier =
          modifier.border(
              width = 2.dp,
              color =
                  color.copy(
                      alpha = if (enabled) ContentAlpha.medium else ContentAlpha.disabled,
                  ),
              shape = MaterialTheme.shapes.medium,
          ),
      shape = MaterialTheme.shapes.medium,
      elevation = CardDefaults.Elevation,
  ) {
    content()
  }
}
