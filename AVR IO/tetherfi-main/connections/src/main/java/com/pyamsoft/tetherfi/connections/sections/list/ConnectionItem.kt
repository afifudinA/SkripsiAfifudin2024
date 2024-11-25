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

package com.pyamsoft.tetherfi.connections.sections.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager
import com.pyamsoft.tetherfi.server.clients.TetherClient
import com.pyamsoft.tetherfi.server.clients.key
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val FIRST_SEEN_DATE_FORMATTER =
    object : ThreadLocal<DateTimeFormatter>() {

      override fun initialValue(): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDateTime(
            FormatStyle.MEDIUM,
            FormatStyle.MEDIUM,
        )
      }
    }

@Composable
internal fun ConnectionItem(
    modifier: Modifier = Modifier,
    blocked: SnapshotStateList<TetherClient>,
    client: TetherClient,
    onClick: (TetherClient) -> Unit,
) {
  val hapticManager = LocalHapticManager.current
  val name = remember(client) { client.key() }
  val seenTime =
      remember(client) {
        FIRST_SEEN_DATE_FORMATTER.get().requireNotNull().format(client.mostRecentlySeen)
      }

  val isNotBlocked =
      remember(
          client,
          blocked,
      ) {
        val isBlocked = blocked.firstOrNull { it.key() == client.key() }
        return@remember isBlocked == null
      }

  Box(
      modifier =
          modifier
              .padding(horizontal = MaterialTheme.keylines.content)
              .padding(bottom = MaterialTheme.keylines.content),
  ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.Elevation,
        shape = MaterialTheme.shapes.medium,
    ) {
      Column(
          modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
      ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              modifier = Modifier.weight(1F),
              text = name,
              style = MaterialTheme.typography.h6,
          )
          Switch(
              checked = isNotBlocked,
              onCheckedChange = { newBlocked ->
                if (newBlocked) {
                  hapticManager?.toggleOn()
                } else {
                  hapticManager?.toggleOff()
                }
                onClick(client)
              },
          )
        }

        Text(
            text = "Last seen: $seenTime",
            style =
                MaterialTheme.typography.body2.copy(
                    color =
                        MaterialTheme.colors.onSurface.copy(
                            alpha = ContentAlpha.medium,
                        ),
                ),
        )
      }
    }
  }
}
