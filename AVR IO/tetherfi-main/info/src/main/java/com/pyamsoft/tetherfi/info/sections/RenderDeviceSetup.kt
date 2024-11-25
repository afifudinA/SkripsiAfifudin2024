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

package com.pyamsoft.tetherfi.info.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager
import com.pyamsoft.pydroid.ui.uri.rememberUriHandler
import com.pyamsoft.tetherfi.info.InfoViewState
import com.pyamsoft.tetherfi.info.MutableInfoViewState
import com.pyamsoft.tetherfi.ui.ServerViewState
import com.pyamsoft.tetherfi.ui.TestServerViewState
import com.pyamsoft.tetherfi.ui.appendLink
import com.pyamsoft.tetherfi.ui.icons.QrCode
import com.pyamsoft.tetherfi.ui.icons.Visibility
import com.pyamsoft.tetherfi.ui.icons.VisibilityOff
import com.pyamsoft.tetherfi.ui.rememberServerHostname
import com.pyamsoft.tetherfi.ui.rememberServerPassword
import com.pyamsoft.tetherfi.ui.rememberServerRawPassword
import com.pyamsoft.tetherfi.ui.rememberServerSSID

private const val LINK_TAG = "instructions"
private const val LINK_TEXT = "here"

private enum class DeviceSetupContentTypes {
  SETTINGS,
  CONNECT,
  TOGGLE,
}

internal fun LazyListScope.renderDeviceSetup(
    itemModifier: Modifier = Modifier,
    appName: String,
    state: InfoViewState,
    serverViewState: ServerViewState,
    onShowQRCode: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
) {
  item(
      contentType = DeviceSetupContentTypes.SETTINGS,
  ) {
    OtherInstruction(
        modifier = itemModifier,
    ) {
      Text(
          text = "Open the Wi-Fi settings page",
          style = MaterialTheme.typography.body1,
      )
    }
  }

  item(
      contentType = DeviceSetupContentTypes.CONNECT,
  ) {
    OtherInstruction(
        modifier = itemModifier.padding(top = MaterialTheme.keylines.content),
    ) {
      Column {
        Text(
            text = "Connect to the $appName Hotspot",
            style = MaterialTheme.typography.body2,
        )

        Row {
          val group by serverViewState.group.collectAsStateWithLifecycle()
          val ssid = rememberServerSSID(group)

          val password = rememberServerRawPassword(group)
          val isNetworkReadyForQRCode =
              remember(
                  ssid,
                  password,
              ) {
                ssid.isNotBlank() && password.isNotBlank()
              }

          Text(
              text = "Name",
              style =
                  MaterialTheme.typography.body1.copy(
                      color =
                          MaterialTheme.colors.onBackground.copy(
                              alpha = ContentAlpha.medium,
                          ),
                  ),
          )

          Text(
              modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
              text = ssid,
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W700,
                      fontFamily = FontFamily.Monospace,
                  ),
          )

          if (isNetworkReadyForQRCode) {
            // Don't use IconButton because we don't care about minimum touch target size
            Box(
                modifier =
                    Modifier.padding(start = MaterialTheme.keylines.baseline)
                        .clickable { onShowQRCode() }
                        .padding(MaterialTheme.keylines.typography),
                contentAlignment = Alignment.Center,
            ) {
              Icon(
                  modifier = Modifier.size(16.dp),
                  imageVector = Icons.Filled.QrCode,
                  contentDescription = "QR Code",
                  tint = MaterialTheme.colors.primary,
              )
            }
          }
        }

        Row {
          val group by serverViewState.group.collectAsStateWithLifecycle()
          val isPasswordVisible by state.isPasswordVisible.collectAsStateWithLifecycle()
          val password = rememberServerPassword(group, isPasswordVisible)
          val rawPassword = rememberServerRawPassword(group)

          val hapticManager = LocalHapticManager.current

          Text(
              text = "Password",
              style =
                  MaterialTheme.typography.body1.copy(
                      color =
                          MaterialTheme.colors.onBackground.copy(
                              alpha = ContentAlpha.medium,
                          ),
                  ),
          )
          Text(
              modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
              text = password,
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W700,
                      fontFamily = FontFamily.Monospace,
                  ),
          )

          if (rawPassword.isNotBlank()) {
            // Don't use IconButton because we don't care about minimum touch target size
            Box(
                modifier =
                    Modifier.padding(start = MaterialTheme.keylines.baseline)
                        .clickable {
                          if (isPasswordVisible) {
                            hapticManager?.toggleOff()
                          } else {
                            hapticManager?.toggleOn()
                          }
                          onTogglePasswordVisibility()
                        }
                        .padding(MaterialTheme.keylines.typography),
                contentAlignment = Alignment.Center,
            ) {
              Icon(
                  modifier = Modifier.size(16.dp),
                  imageVector =
                      if (isPasswordVisible) Icons.Filled.VisibilityOff
                      else Icons.Filled.Visibility,
                  contentDescription =
                      if (isPasswordVisible) "Password Visible" else "Password Hidden",
                  tint = MaterialTheme.colors.primary,
              )
            }
          }
        }

        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
            text =
                "Configure the proxy settings. Use MANUAL mode and configure both HTTP and HTTPS proxy options.",
            style = MaterialTheme.typography.body1,
        )

        Row(
            modifier = Modifier.padding(top = MaterialTheme.keylines.typography),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          val connection by serverViewState.connection.collectAsStateWithLifecycle()
          val ipAddress = rememberServerHostname(connection)

          Text(
              text = "URL",
              style =
                  MaterialTheme.typography.body1.copy(
                      color =
                          MaterialTheme.colors.onBackground.copy(
                              alpha = ContentAlpha.medium,
                          ),
                  ),
          )
          Text(
              modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
              text = ipAddress,
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W700,
                      fontFamily = FontFamily.Monospace,
                  ),
          )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = "Port",
              style =
                  MaterialTheme.typography.body1.copy(
                      color =
                          MaterialTheme.colors.onBackground.copy(
                              alpha = ContentAlpha.medium,
                          ),
                  ),
          )

          val port by serverViewState.port.collectAsStateWithLifecycle()
          val portNumber = remember(port) { if (port <= 1024) "INVALID PORT" else "$port" }
          Text(
              modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
              text = portNumber,
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W700,
                      fontFamily = FontFamily.Monospace,
                  ),
          )
        }

        FullConnectionInstructions(
            modifier = Modifier.padding(top = MaterialTheme.keylines.typography),
        )
      }
    }
  }

  item(
      contentType = DeviceSetupContentTypes.TOGGLE,
  ) {
    OtherInstruction(
        modifier = itemModifier.padding(top = MaterialTheme.keylines.content),
    ) {
      Text(
          text =
              "Turn the Wi-Fi off and back on again. It should automatically connect to the $appName Hotspot",
          style = MaterialTheme.typography.body1,
      )
    }
  }
}

@Composable
private fun FullConnectionInstructions(
    modifier: Modifier = Modifier,
) {
  val uriHandler = rememberUriHandler()

  val textColor =
      MaterialTheme.colors.onBackground.copy(
          alpha = ContentAlpha.medium,
      )
  val linkColor = MaterialTheme.colors.primary
  val instructions =
      remember(
          textColor,
          linkColor,
      ) {
        buildAnnotatedString {
          withStyle(
              style =
                  SpanStyle(
                      color = textColor,
                  ),
          ) {
            append("Having trouble configuring Proxy Settings? See more detailed instructions ")
            appendLink(
                tag = LINK_TAG,
                linkColor = linkColor,
                text = LINK_TEXT,
                url = "https://github.com/pyamsoft/tetherfi/wiki/Setup-A-Proxy",
            )
          }
        }
      }

  ClickableText(
      modifier = modifier,
      text = instructions,
      style = MaterialTheme.typography.body1,
      onClick = { offset ->
        instructions
            .getStringAnnotations(
                tag = LINK_TAG,
                start = offset,
                end = offset + LINK_TEXT.length,
            )
            .firstOrNull()
            ?.also { uriHandler.openUri(it.item) }
      },
  )
}

@Preview
@Composable
private fun PreviewDeviceSetup() {
  LazyColumn {
    renderDeviceSetup(
        appName = "TEST",
        serverViewState = TestServerViewState(),
        state = MutableInfoViewState(),
        onTogglePasswordVisibility = {},
        onShowQRCode = {},
    )
  }
}
