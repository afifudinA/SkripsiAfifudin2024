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

package com.pyamsoft.tetherfi.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.app.PYDroidActivityDelegate
import com.pyamsoft.pydroid.ui.app.installPYDroid
import com.pyamsoft.pydroid.ui.changelog.ChangeLogProvider
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.pydroid.util.stableLayoutHideNavigation
import com.pyamsoft.tetherfi.ObjectGraph
import com.pyamsoft.tetherfi.R
import com.pyamsoft.tetherfi.TetherFiTheme
import com.pyamsoft.tetherfi.core.Timber
import com.pyamsoft.tetherfi.getSystemDarkMode
import com.pyamsoft.tetherfi.service.ServiceLauncher
import com.pyamsoft.tetherfi.tile.ProxyTileService
import com.pyamsoft.tetherfi.ui.InstallPYDroidExtras
import com.pyamsoft.tetherfi.ui.LANDSCAPE_MAX_WIDTH
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

  @Inject @JvmField internal var viewModel: ThemeViewModeler? = null
  @Inject @JvmField internal var permissions: MainPermissions? = null
  @Inject @JvmField internal var launcher: ServiceLauncher? = null
  private var pydroid: PYDroidActivityDelegate? = null

  private fun initializePYDroid() {
    pydroid =
        installPYDroid(
            provider =
                object : ChangeLogProvider {

                  override val applicationIcon = R.mipmap.ic_launcher

                  override val changelog = buildChangeLog {
                    bugfix("Fix unbounded connection pools never closing")
                    bugfix("Track 'Invalid Argument' Proxy errors and notify the user")
                    change(
                        "Added: 'Stubborn Proxy' mode, which attempts to work-around Proxy 'Invalid Argument' launch errors.")
                    change(
                        "Internal: Move away from deprecated Accompanist method for Edge-to-Edge screen support.")

                    // NEXT
                    //                    change(
                    //                        "Support assigning a custom name to a connected Client
                    // device in the Connections tab.")
                  }
                },
        )
  }

  private fun registerPermissionRequester() {
    permissions.requireNotNull().register(this)
  }

  private fun handleShowInAppRating() {
    pydroid?.loadInAppRating()
  }

  private fun setupActivity() {
    // Setup PYDroid first
    initializePYDroid()

    // Create and initialize the ObjectGraph
    val component = ObjectGraph.ApplicationScope.retrieve(this).plusMain().create()
    component.inject(this)
    ObjectGraph.ActivityScope.install(this, component)

    // Then register for any permissions
    registerPermissionRequester()

    // Finally update the View
    stableLayoutHideNavigation()
  }

  private fun safeOpenSettingsIntent(action: String) {

    // Try specific first, may fail on some devices
    try {
      val intent = Intent(action, "package:${packageName}".toUri())
      startActivity(intent)
    } catch (e: Throwable) {
      Timber.e(e) { "Failed specific intent for $action" }
      val intent = Intent(action)
      startActivity(intent)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupActivity()

    val vm = viewModel.requireNotNull()
    val appName = getString(R.string.app_name)

    setContent {
      val theme by vm.theme.collectAsStateWithLifecycle()

      SaveStateDisposableEffect(vm)

      TetherFiTheme(
          theme = theme,
      ) {
        SystemBars(
            isDarkMode = theme.getSystemDarkMode(),
        )
        InstallPYDroidExtras(
            modifier = Modifier.fillUpToPortraitSize().widthIn(max = LANDSCAPE_MAX_WIDTH),
            appName = appName,
        )
        MainEntry(
            modifier = Modifier.fillMaxSize(),
            appName = appName,
            onShowInAppRating = { handleShowInAppRating() },
            onUpdateTile = { ProxyTileService.updateTile(this) },
            onLaunchIntent = { safeOpenSettingsIntent(it) },
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }

  override fun onResume() {
    super.onResume()

    viewModel.requireNotNull().handleSyncDarkTheme(this)
    reportFullyDrawn()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    viewModel?.handleSyncDarkTheme(this)
  }

  override fun onDestroy() {
    super.onDestroy()

    permissions?.unregister()

    pydroid = null
    permissions = null
    viewModel = null
    launcher = null
  }
}
