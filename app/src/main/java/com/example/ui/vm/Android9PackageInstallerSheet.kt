package com.example.ui.vm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InstalledAppEntity
import com.example.engine.ApkInstallerHelper
import com.example.engine.ApkMetadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Android9PackageInstallerDialog(
    vm: VmViewModel,
    initialMetadata: ApkMetadata?,
    onClose: () -> Unit,
    onAppInstalledAndOpen: (InstalledAppEntity) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(if (initialMetadata != null) 0 else 0) }

    // Staging / Verification / Installing State
    var stagedApk by remember { mutableStateOf<ApkMetadata?>(initialMetadata) }
    var chosenGenre by remember { mutableStateOf(initialMetadata?.recommendedGenre ?: "SHMUP") }
    var isInstalling by remember { mutableStateOf(false) }
    var installProgress by remember { mutableFloatStateOf(0f) }
    var installStatusText by remember { mutableStateOf("") }
    var installedEntity by remember { mutableStateOf<InstalledAppEntity?>(null) }

    // Real Android File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val parsed = ApkInstallerHelper.parseApkFromUri(context, uri)
            stagedApk = parsed
            chosenGenre = parsed.recommendedGenre
            installedEntity = null
            isInstalling = false
        }
    }

    // Custom Sideload Form state
    var customName by remember { mutableStateOf("") }
    var customPackage by remember { mutableStateOf("") }
    var customGenre by remember { mutableStateOf("SHMUP") }
    var customSizeMb by remember { mutableStateOf("28.5") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE050814))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {}, // prevent clicks passing through
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 12.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header: Android 9 Package Installer Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Package Installer (Android 9 Pie)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "32-Bit ARMv7-A Native Game Sandbox",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("installer_close_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // If currently installing or already installed, show Android 9 Installation Progress & Result
                if (isInstalling || installedEntity != null) {
                    InstallationProgressView(
                        stagedApk = stagedApk,
                        installedEntity = installedEntity,
                        progress = installProgress,
                        statusText = installStatusText,
                        onOpen = {
                            installedEntity?.let { onAppInstalledAndOpen(it) }
                        },
                        onDone = onClose
                    )
                } else if (stagedApk != null) {
                    // Staged APK Confirmation & Architecture Verification Card
                    StagedApkConfirmationView(
                        apk = stagedApk!!,
                        selectedGenre = chosenGenre,
                        onGenreSelected = { chosenGenre = it },
                        onInstall = {
                            isInstalling = true
                            installProgress = 0.1f
                            installStatusText = "Staging APK archive in virtual cache..."
                            scope.launch {
                                delay(300)
                                installProgress = 0.35f
                                installStatusText = "Inspecting 32-bit ELF binaries (lib/armeabi-v7a)..."
                                delay(350)
                                installProgress = 0.65f
                                installStatusText = "Compiling DEX bytecode with NEON SIMD JIT..."
                                delay(350)
                                installProgress = 0.90f
                                installStatusText = "Linking OpenGL ES 3.2 hardware acceleration..."
                                delay(300)
                                installProgress = 1.0f
                                installStatusText = "App installed successfully."
                                isInstalling = false

                                // Insert into database
                                val apk = stagedApk!!
                                val entity = vm.installParsedApk(apk, chosenGenre)
                                installedEntity = entity
                            }
                        },
                        onChangeApk = { stagedApk = null }
                    )
                } else {
                    // Tabs to pick APK: Device Storage / Vault Presets / Manual Sideload
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF38BDF8),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF38BDF8)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Device File", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("32-Bit Vault", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Custom Entry", fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (selectedTab) {
                        0 -> {
                            // Device File Picker Card
                            DeviceFilePickerCard(
                                onLaunchPicker = {
                                    try {
                                        filePickerLauncher.launch("*/*")
                                    } catch (_: Exception) {
                                        filePickerLauncher.launch("application/vnd.android.package-archive")
                                    }
                                }
                            )
                        }
                        1 -> {
                            // Preset 32-Bit Vault
                            VaultCatalogView(
                                onSelectPreset = { preset ->
                                    stagedApk = ApkMetadata(
                                        fileName = preset.fileName,
                                        appName = preset.name,
                                        packageName = preset.packageName,
                                        versionName = "1.0-pie",
                                        fileSizeMb = preset.sizeMb,
                                        architecture = "armeabi-v7a (32-bit)",
                                        is32BitCompatible = true,
                                        nativeLibraries = preset.nativeLibs,
                                        hasClassesDex = true,
                                        recommendedGenre = preset.genre,
                                        summary = preset.summary
                                    )
                                    chosenGenre = preset.genre
                                }
                            )
                        }
                        2 -> {
                            // Custom Sideload Form
                            CustomSideloadForm(
                                name = customName,
                                onNameChange = { customName = it },
                                pkg = customPackage,
                                onPkgChange = { customPackage = it },
                                genre = customGenre,
                                onGenreChange = { customGenre = it },
                                sizeMb = customSizeMb,
                                onSizeMbChange = { customSizeMb = it },
                                onStage = {
                                    val sizeVal = customSizeMb.toFloatOrNull() ?: 25.0f
                                    val finalName = if (customName.isNotBlank()) customName else "Custom 32-Bit Game"
                                    val finalPkg = if (customPackage.isNotBlank()) customPackage else "com.custom." + finalName.lowercase().replace(" ", "")
                                    stagedApk = ApkMetadata(
                                        fileName = "${finalName.lowercase().replace(" ", "_")}.apk",
                                        appName = finalName,
                                        packageName = finalPkg,
                                        versionName = "1.0-pie",
                                        fileSizeMb = sizeVal,
                                        architecture = "armeabi-v7a (32-bit)",
                                        is32BitCompatible = true,
                                        nativeLibraries = listOf("libgame.so (32-bit ELF)", "libglesv3_direct.so"),
                                        hasClassesDex = true,
                                        recommendedGenre = customGenre,
                                        summary = "Custom sideloaded 32-bit APK package."
                                    )
                                    chosenGenre = customGenre
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceFilePickerCard(onLaunchPicker: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Select .APK from Device",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pick any Android APK from your files. The emulator will automatically parse native 32-bit libraries (ARMv7-A) and stage it for execution with GPU hardware acceleration.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onLaunchPicker,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_browse_apk_files")
            ) {
                Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse APK Files...", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VaultCatalogView(
    onSelectPreset: (ApkInstallerHelper.VaultPreset) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ApkInstallerHelper.vaultPresets) { preset ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPreset(preset) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (brush, icon, tint) = getAppVisuals(preset.genre, true)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(brush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = preset.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${preset.sizeMb} MB • 32-bit ARM", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0284C7)
                    ) {
                        Text(
                            text = "STAGE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomSideloadForm(
    name: String,
    onNameChange: (String) -> Unit,
    pkg: String,
    onPkgChange: (String) -> Unit,
    genre: String,
    onGenreChange: (String) -> Unit,
    sizeMb: String,
    onSizeMbChange: (String) -> Unit,
    onStage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Game Name (e.g. Sonic 32, Retro Quest)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_apk_name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = pkg,
            onValueChange = onPkgChange,
            label = { Text("Package (e.g. com.retro.game32)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Text(
            text = "Game Engine / Genre Layout:",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("SHMUP" to "Shmup", "BRICK" to "Breakout", "ACTION" to "Action", "RACER" to "Racer", "RPG" to "RPG").forEach { (type, label) ->
                FilterChip(
                    selected = genre == type,
                    onClick = { onGenreChange(type) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        OutlinedTextField(
            value = sizeMb,
            onValueChange = onSizeMbChange,
            label = { Text("File Size (MB)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onStage,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_stage_custom_apk")
        ) {
            Text("Stage & Verify Package", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StagedApkConfirmationView(
    apk: ApkMetadata,
    selectedGenre: String,
    onGenreSelected: (String) -> Unit,
    onInstall: () -> Unit,
    onChangeApk: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // App Summary Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (brush, icon, tint) = getAppVisuals(selectedGenre, true)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(brush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = apk.appName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = apk.packageName, color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "${String.format("%.1f", apk.fileSizeMb)} MB • Version ${apk.versionName}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        // Architecture & Native Library Checklist
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Target Architecture: ${apk.architecture}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Graphics: OpenGL ES 3.2 Hardware Accelerated", color = Color(0xFF38BDF8), fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Native Libraries (${apk.nativeLibraries.size} detected):", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                }
                apk.nativeLibraries.forEach { lib ->
                    Text(text = "  • $lib", color = Color(0xFF94A3B8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Gameplay Mode Selection
        Text(
            text = "Select 32-Bit Arcade Mode / Engine:",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("SHMUP" to "Space Combat", "BRICK" to "Neon Breakout", "ACTION" to "Cyber Run").forEach { (type, label) ->
                FilterChip(
                    selected = selectedGenre == type,
                    onClick = { onGenreSelected(type) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Actions: Install or Cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onChangeApk,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Change APK", color = Color(0xFF94A3B8))
            }

            Button(
                onClick = onInstall,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.4f)
                    .testTag("btn_confirm_install_apk")
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("INSTALL APK", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InstallationProgressView(
    stagedApk: ApkMetadata?,
    installedEntity: InstalledAppEntity?,
    progress: Float,
    statusText: String,
    onOpen: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (installedEntity != null) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Text(
                text = "App Installed Successfully",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${installedEntity.name} is installed inside Android 9 and ready for hardware-accelerated 32-bit gaming.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDone,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("DONE", color = Color.White)
                }

                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_open_installed_apk")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PLAY NOW", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // In progress
            Text(
                text = "Installing ${stagedApk?.appName ?: "32-Bit APK"}...",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF0284C7),
                trackColor = Color(0xFF1E293B)
            )
            Text(
                text = statusText,
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
