package com.example.ui.vm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VmConfigEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android9SettingsScreen(
    vm: VmViewModel,
    config: VmConfigEntity?,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val hw = vm.hwEngine

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings (Android 9 Pie)",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0A0F1D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Hardware Acceleration Engine Controls
            SettingsCard(title = "HARDWARE ACCELERATION ENGINE", icon = Icons.Default.Bolt) {
                // Master HW Accel Switch
                SettingsSwitchRow(
                    title = "Hardware Acceleration",
                    subtitle = "Use native GPU pipeline for 60/120 FPS high performance",
                    checked = hw.isHwAccelerated,
                    onCheckedChange = { vm.toggleHwAcceleration(it) },
                    testTag = "switch_hw_accel"
                )

                // Multithreaded Rendering
                SettingsSwitchRow(
                    title = "Multi-Threaded Rendering",
                    subtitle = "Parallel worker threads for 32-bit sprite & physics calculation",
                    checked = hw.isMultiThreaded,
                    onCheckedChange = { vm.toggleMultiThreaded(it) },
                    testTag = "switch_multithread"
                )

                // Target FPS Selector
                Text(
                    text = "Target Refresh Rate / FPS Limit",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    listOf(30, 60, 90, 120).forEach { fps ->
                        FilterChip(
                            selected = hw.targetFps == fps,
                            onClick = { vm.setTargetFps(fps) },
                            label = { Text("$fps FPS") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // CRT / Shader Filter
                Text(
                    text = "Post-Processing Shader",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    listOf("CRT_SCANLINES" to "CRT Scanlines", "OFF" to "Clean Raw", "NEON_GLOW" to "Neon Bloom").forEach { (id, label) ->
                        FilterChip(
                            selected = hw.activeShader == id,
                            onClick = { vm.setShader(id) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Resolution Scale
                Text(
                    text = "Internal Resolution Scale: ${(hw.resolutionScale * 100).toInt()}%",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    listOf(0.67f to "720p (Fast)", 1.0f to "1080p (Native)", 1.25f to "1440p (Ultra)").forEach { (scale, label) ->
                        FilterChip(
                            selected = hw.resolutionScale == scale,
                            onClick = { vm.setResolutionScale(scale) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Section 2: 32-Bit ARMv7-A Architecture & Memory
            SettingsCard(title = "32-BIT ARM ARCHITECTURE SANDBOX", icon = Icons.Default.Memory) {
                InfoRow(label = "Architecture ABI", value = "armeabi-v7a (32-bit)")
                InfoRow(label = "Instruction Set", value = "ARMv7-A + NEON™ v2 SIMD")
                InfoRow(label = "Max Address Space", value = "4,096 MB (32-Bit Pointer Limit)")
                InfoRow(label = "Dalvik/ART Virtual Heap", value = "${config?.virtualMemoryMb ?: 3072} MB Allocated")
                InfoRow(label = "JIT Translation Blocks", value = "${vm.armLayer.jitTranslatedBlocksCount} compiled")
                InfoRow(label = "JIT Cache Hit Rate", value = "${vm.armLayer.jitCacheHitRatePercent}%")

                Text(
                    text = "ARM Translation Execution Mode",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    listOf("JIT_FAST" to "Fast JIT (Recommended)", "COMPAT_STRICT" to "Strict Compatibility").forEach { (mode, label) ->
                        FilterChip(
                            selected = config?.armTranslationMode == mode,
                            onClick = { vm.setArmMode(mode) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Section 3: Navigation Bar & Wallpaper
            SettingsCard(title = "SYSTEM UI & NAVIGATION", icon = Icons.Default.Palette) {
                Text(
                    text = "Android 9 Navigation Bar Style",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    listOf("PILL" to "Android 9 Pill (2-Button)", "THREE_BUTTON" to "Classic 3-Button").forEach { (type, label) ->
                        FilterChip(
                            selected = config?.navBarType == type,
                            onClick = { vm.setNavBarType(type) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Virtual Desktop Wallpaper",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    listOf("PIE_DEFAULT" to "Pie Blue", "CYBER_DARK" to "Cyber Dark", "RETRO_NEON" to "Retro Neon").forEach { (wall, label) ->
                        FilterChip(
                            selected = config?.wallpaper == wall,
                            onClick = { vm.setWallpaper(wall) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Section 4: Gamepad & Haptics
            SettingsCard(title = "VIRTUAL GAMEPAD & HAPTICS", icon = Icons.Default.Gamepad) {
                SettingsSwitchRow(
                    title = "Vibration Haptic Feedback",
                    subtitle = "Feel tactile feedback on buttons and collisions",
                    checked = config?.vibrationEnabled == true,
                    onCheckedChange = { vm.toggleVibration(it) },
                    testTag = "switch_vibration"
                )

                SettingsSwitchRow(
                    title = "Sound Synthesis FX",
                    subtitle = "Real-time PCM retro audio generator",
                    checked = vm.audioSynth.isEnabled,
                    onCheckedChange = { vm.toggleSound(it) },
                    testTag = "switch_sound"
                )

                Text(
                    text = "Gamepad On-Screen Opacity: ${(vm.gamepadOpacity * 100).toInt()}%",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Slider(
                    value = vm.gamepadOpacity,
                    onValueChange = { vm.gamepadOpacity = it },
                    valueRange = 0.3f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF38BDF8),
                        activeTrackColor = Color(0xFF0284C7)
                    )
                )

                Text(
                    text = "Gamepad Button Scale: ${(vm.buttonScale * 100).toInt()}%",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = vm.buttonScale,
                    onValueChange = { vm.buttonScale = it },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF38BDF8),
                        activeTrackColor = Color(0xFF0284C7)
                    )
                )
            }

            // Section 5: About Virtual Device
            SettingsCard(title = "ABOUT VIRTUAL PHONE", icon = Icons.Default.Info) {
                InfoRow(label = "Device Model", value = "Samsung Galaxy S9 (SM-G960F)")
                InfoRow(label = "Android Version", value = "9.0 Pie (API Level 28)")
                InfoRow(label = "Security Patch", value = "August 5, 2018")
                InfoRow(label = "Kernel Version", value = "4.14.85-pie-gki-v2 armv7l")
                InfoRow(label = "Build Number", value = "PIE.190801.002.G960FXXU2BRJ3")
                InfoRow(label = "GPU Renderer", value = "Qualcomm Adreno 630 / Vulkan 1.1")
                InfoRow(label = "Hardware Acceleration", value = if (hw.isHwAccelerated) "Active (Direct Buffer)" else "Disabled")
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7)
            )
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
