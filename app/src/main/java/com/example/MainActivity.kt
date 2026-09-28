package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.games.GameButton
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vm.Android9AppStoreScreen
import com.example.ui.vm.Android9FileManagerScreen
import com.example.ui.vm.Android9HomeScreen
import com.example.ui.vm.Android9NavigationBar
import com.example.ui.vm.Android9PackageInstallerDialog
import com.example.ui.vm.Android9QuickSettingsShade
import com.example.ui.vm.Android9SettingsScreen
import com.example.ui.vm.Android9StatusBar
import com.example.ui.vm.Android9TerminalScreen
import com.example.ui.vm.GameViewportScreen
import com.example.ui.vm.VmViewModel

class MainActivity : ComponentActivity() {

    private var activeViewModel: VmViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val vm: VmViewModel = viewModel()
                activeViewModel = vm

                MainEmulatorScreen(vm = vm)
            }
        }
    }

    // Hardware Controller & Keyboard Support
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val vm = activeViewModel
        if (vm != null && vm.activeGame != null) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> {
                    vm.onGameDpad(-1f, 0f)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> {
                    vm.onGameDpad(1f, 0f)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> {
                    vm.onGameDpad(0f, -1f)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> {
                    vm.onGameDpad(0f, 1f)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_SPACE -> {
                    vm.onGameButtonDown(GameButton.A)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_K -> {
                    vm.onGameButtonDown(GameButton.B)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_U -> {
                    vm.onGameButtonDown(GameButton.X)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_I -> {
                    vm.onGameButtonDown(GameButton.Y)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_L1 -> {
                    vm.onGameButtonDown(GameButton.L1)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_R1 -> {
                    vm.onGameButtonDown(GameButton.R1)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_ENTER -> {
                    vm.onGameButtonDown(GameButton.START)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val vm = activeViewModel
        if (vm != null && vm.activeGame != null) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_D -> {
                    vm.onGameDpad(0f, 0f)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_SPACE -> {
                    vm.onGameButtonUp(GameButton.A)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_K -> {
                    vm.onGameButtonUp(GameButton.B)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_U -> {
                    vm.onGameButtonUp(GameButton.X)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_I -> {
                    vm.onGameButtonUp(GameButton.Y)
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun MainEmulatorScreen(vm: VmViewModel) {
    val config by vm.vmConfig.collectAsStateWithLifecycle()
    val installedApps by vm.installedApps.collectAsStateWithLifecycle()

    // Handle system back navigation
    BackHandler(enabled = vm.activeApp != null || vm.isQuickSettingsOpen || vm.isRecentsOpen || vm.isApkInstallerOpen) {
        if (vm.isApkInstallerOpen) {
            vm.closeApkInstaller()
        } else {
            vm.navigateBack()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Android 9 System Status Bar
            Android9StatusBar(vm = vm, config = config)

            // Main Viewport Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Determine active screen / virtual app
                when {
                    vm.activeGame != null -> {
                        GameViewportScreen(vm = vm, game = vm.activeGame!!)
                    }
                    vm.activeApp?.id == "app.settings" -> {
                        Android9SettingsScreen(
                            vm = vm,
                            config = config,
                            onBack = { vm.navigateBack() }
                        )
                    }
                    vm.activeApp?.id == "app.files" -> {
                        Android9FileManagerScreen(
                            vm = vm,
                            onBack = { vm.navigateBack() }
                        )
                    }
                    vm.activeApp?.id == "app.store" -> {
                        Android9AppStoreScreen(
                            vm = vm,
                            installedApps = installedApps,
                            onLaunchApp = { vm.launchApp(it) },
                            onBack = { vm.navigateBack() }
                        )
                    }
                    vm.activeApp?.id == "app.terminal" -> {
                        Android9TerminalScreen(
                            vm = vm,
                            onBack = { vm.navigateBack() }
                        )
                    }
                    else -> {
                        Android9HomeScreen(
                            vm = vm,
                            apps = installedApps,
                            config = config,
                            onLaunchApp = { vm.launchApp(it) }
                        )
                    }
                }

                // Android 9 Pie Pull-Down Quick Settings Notification Shade
                Android9QuickSettingsShade(
                    vm = vm,
                    config = config,
                    onOpenSettings = {
                        val settingsApp = installedApps.find { it.id == "app.settings" }
                        if (settingsApp != null) {
                            vm.launchApp(settingsApp)
                        }
                    }
                )

                // Android 9 Package Installer Modal
                if (vm.isApkInstallerOpen) {
                    Android9PackageInstallerDialog(
                        vm = vm,
                        initialMetadata = vm.pendingApkToInstall,
                        onClose = { vm.closeApkInstaller() },
                        onAppInstalledAndOpen = { app ->
                            vm.closeApkInstaller()
                            vm.launchApp(app)
                        }
                    )
                }
            }

            // Android 9 Navigation Bar (Pill / 3-Button)
            Android9NavigationBar(
                vm = vm,
                navBarType = config?.navBarType ?: "PILL"
            )
        }
    }
}
