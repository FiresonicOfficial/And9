package com.example.ui.vm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VmConfigEntity

@Composable
fun Android9StatusBar(
    vm: VmViewModel,
    config: VmConfigEntity?,
    modifier: Modifier = Modifier
) {
    val hw = vm.hwEngine

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color(0xE60A0D14))
            .clickable { vm.toggleQuickSettings() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Virtual Time & 32-bit Architecture badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "10:28",
                color = Color(0xFFF1F5F9),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 32-bit Architecture Badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0284C7)
            ) {
                Text(
                    text = "32-BIT ARM",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        // Center: Hardware Acceleration Indicator
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (hw.isHwAccelerated) Color(0xFF0F766E) else Color(0xFF7F1D1D)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "HW Accel",
                    tint = if (hw.isHwAccelerated) Color(0xFF2DD4BF) else Color(0xFFF87171),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (hw.isHwAccelerated) "HW-ACCEL ${hw.currentFps} FPS" else "SW RASTER ${hw.currentFps} FPS",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right: Wi-Fi, 4G LTE, Battery Pie
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.NetworkCell,
                contentDescription = "4G LTE",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "94%",
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun Android9QuickSettingsShade(
    vm: VmViewModel,
    config: VmConfigEntity?,
    onOpenSettings: () -> Unit
) {
    var brightness by remember { mutableFloatStateOf(0.85f) }
    val hw = vm.hwEngine

    AnimatedVisibility(
        visible = vm.isQuickSettingsOpen,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF50F172A),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header: Android 9 Pie Brand + Dismiss button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Android 9.0 Pie (API 28)",
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "32-Bit Native Gaming Sandbox & Hardware Accelerator",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7),
                            modifier = Modifier
                                .clickable {
                                    vm.isQuickSettingsOpen = false
                                    vm.openApkInstaller()
                                }
                                .testTag("qs_add_apk_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "ADD APK",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("qs_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brightness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessHigh,
                        contentDescription = "Brightness",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF0284C7),
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Settings Round Tiles Grid (Pie Style)
                Text(
                    text = "EMULATOR & HARDWARE ACCELERATION TOGGLES",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickTileItem(
                        icon = Icons.Default.Bolt,
                        label = "HW Accel",
                        sub = if (hw.isHwAccelerated) "Active" else "Software",
                        isActive = hw.isHwAccelerated,
                        onClick = { vm.toggleHwAcceleration(!hw.isHwAccelerated) }
                    )
                    QuickTileItem(
                        icon = Icons.Default.Speed,
                        label = "Target FPS",
                        sub = "${hw.targetFps} Hz",
                        isActive = hw.targetFps >= 60,
                        onClick = {
                            val next = when (hw.targetFps) {
                                30 -> 60
                                60 -> 90
                                90 -> 120
                                else -> 30
                            }
                            vm.setTargetFps(next)
                        }
                    )
                    QuickTileItem(
                        icon = Icons.Default.Memory,
                        label = "Multithread",
                        sub = if (hw.isMultiThreaded) "Dual-Core" else "Single",
                        isActive = hw.isMultiThreaded,
                        onClick = { vm.toggleMultiThreaded(!hw.isMultiThreaded) }
                    )
                    QuickTileItem(
                        icon = Icons.Default.Tv,
                        label = "CRT Shader",
                        sub = if (hw.activeShader == "CRT_SCANLINES") "On" else "Off",
                        isActive = hw.activeShader == "CRT_SCANLINES",
                        onClick = {
                            val next = if (hw.activeShader == "CRT_SCANLINES") "OFF" else "CRT_SCANLINES"
                            vm.setShader(next)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickTileItem(
                        icon = Icons.Default.VolumeUp,
                        label = "Audio FX",
                        sub = if (vm.audioSynth.isEnabled) "PCM Hi-Fi" else "Muted",
                        isActive = vm.audioSynth.isEnabled,
                        onClick = { vm.toggleSound(!vm.audioSynth.isEnabled) }
                    )
                    QuickTileItem(
                        icon = Icons.Default.Vibration,
                        label = "Haptics",
                        sub = if (config?.vibrationEnabled == true) "Active" else "Off",
                        isActive = config?.vibrationEnabled == true,
                        onClick = { vm.toggleVibration(config?.vibrationEnabled != true) }
                    )
                    QuickTileItem(
                        icon = Icons.Default.Gamepad,
                        label = "Keymapper",
                        sub = if (vm.isKeymapperOpen) "Editing" else "Locked",
                        isActive = vm.isKeymapperOpen,
                        onClick = { vm.isKeymapperOpen = !vm.isKeymapperOpen }
                    )
                    QuickTileItem(
                        icon = Icons.Default.Tune,
                        label = "32-Bit JIT",
                        sub = config?.armTranslationMode ?: "JIT_FAST",
                        isActive = true,
                        onClick = {
                            val next = if (config?.armTranslationMode == "JIT_FAST") "COMPAT_STRICT" else "JIT_FAST"
                            vm.setArmMode(next)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Telemetry Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "FPS: ${hw.currentFps} (${hw.frameTimeMs} ms)",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GPU Load: ${hw.gpuLoadPercentage}%",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "VRAM: ${hw.allocatedVramMb} MB",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickTileItem(
    icon: ImageVector,
    label: String,
    sub: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF0284C7) else Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(0xFFF1F5F9),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = sub,
            color = if (isActive) Color(0xFF38BDF8) else Color(0xFF64748B),
            fontSize = 9.sp
        )
    }
}

@Composable
fun Android9NavigationBar(
    vm: VmViewModel,
    navBarType: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        color = Color(0xEE0A0E17)
    ) {
        if (navBarType == "PILL") {
            // Android 9 2-Button Pill Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Back Button (Chevron)
                IconButton(
                    onClick = { vm.navigateBack() },
                    modifier = Modifier.testTag("nav_back_pill")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Pill Button (Tap = Home, Long Press / Drag = Recents)
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFE2E8F0))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { vm.navigateHome() },
                                onLongPress = { vm.toggleRecents() }
                            )
                        }
                        .testTag("nav_pill_home")
                )

                // Recents Task Switcher Toggle
                IconButton(
                    onClick = { vm.toggleRecents() },
                    modifier = Modifier.testTag("nav_recents_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recent Apps",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Classic 3-Button Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = { vm.navigateBack() },
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = { vm.navigateHome() },
                    modifier = Modifier.testTag("nav_home_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                    )
                }

                IconButton(
                    onClick = { vm.toggleRecents() },
                    modifier = Modifier.testTag("nav_recents_square")
                ) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                }
            }
        }
    }
}
