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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.Arm32CompatibilityLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android9FileManagerScreen(
    vm: VmViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showInstallDialog by remember { mutableStateOf(false) }
    var inspectedFile by remember { mutableStateOf<Arm32CompatibilityLayer.ApkArchitectureInspection?>(null) }

    data class VirtualFile(
        val name: String,
        val path: String,
        val size: String,
        val type: String, // "APK", "SO", "ROM", "FOLDER"
        val isDirectory: Boolean = false
    )

    val gamesFiles = listOf(
        VirtualFile("turbodrift_v2.4_armeabi.apk", "/sdcard/Games/turbodrift.apk", "48.6 MB", "APK"),
        VirtualFile("commando1999_armv7.apk", "/sdcard/Games/commando1999.apk", "36.2 MB", "APK"),
        VirtualFile("aerosquadron_32bit.apk", "/sdcard/Games/aerosquadron.apk", "29.8 MB", "APK"),
        VirtualFile("dungeoncrawl_retro.apk", "/sdcard/Games/dungeoncrawl.apk", "42.1 MB", "APK"),
        VirtualFile("sonic_retro_genesis.bin", "/sdcard/Games/sonic.bin", "4.0 MB", "ROM"),
        VirtualFile("castlevania_symphony.iso", "/sdcard/Games/castlevania.iso", "520 MB", "ROM")
    )

    val systemLibFiles = listOf(
        VirtualFile("libgame_renderer_hw.so", "/system/lib/libgame_renderer_hw.so", "2.4 MB", "SO"),
        VirtualFile("libGLESv3_adreno.so", "/system/lib/libGLESv3_adreno.so", "4.8 MB", "SO"),
        VirtualFile("libvulkan.so", "/system/lib/libvulkan.so", "3.2 MB", "SO"),
        VirtualFile("libart_32bit.so", "/system/lib/libart_32bit.so", "12.6 MB", "SO"),
        VirtualFile("libaudiotrack_pcm.so", "/system/lib/libaudiotrack_pcm.so", "1.1 MB", "SO"),
        VirtualFile("libc.so (32-bit Bionic)", "/system/lib/libc.so", "1.8 MB", "SO")
    )

    val activeFiles = if (selectedTab == 0) gamesFiles else systemLibFiles

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Files (Virtual Storage)",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("files_back_btn")) {
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.openApkInstaller() },
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_install_apk")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Install APK")
            }
        },
        containerColor = Color(0xFF0A0F1D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Storage Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
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
                    text = { Text("/sdcard/Games (32-bit)") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("/system/lib (ARMv7)") }
                )
            }

            // Path Header
            Surface(
                color = Color(0xFF131C2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedTab == 0) "Storage > emulated > 0 > Games" else "System > lib > armeabi-v7a",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // File List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeFiles) { file ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (file.type == "APK") {
                                    inspectedFile = vm.armLayer.inspectApkPackage(file.name, "com.retro32." + file.name.substringBefore("."))
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (icon, iconColor) = when (file.type) {
                                    "APK" -> Icons.Default.InsertDriveFile to Color(0xFF38BDF8)
                                    "SO" -> Icons.Default.Layers to Color(0xFF10B981)
                                    else -> Icons.Default.Description to Color(0xFFFBBF24)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = file.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${file.size} • ${file.type} • 32-bit ELF",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (file.type == "APK") {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0284C7)
                                ) {
                                    Text(
                                        text = "INSPECT",
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
        }
    }

    // Inspect APK Architecture Dialog
    inspectedFile?.let { inspection ->
        AlertDialog(
            onDismissRequest = { inspectedFile = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Text(
                    text = "32-Bit Architecture Inspection",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Package: ${inspection.packageName}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    Text(text = "Target ABI: ${inspection.abiType}", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "ELF Machine Code: ${inspection.elfMachine}", color = Color.White, fontSize = 11.sp)
                    Text(text = "Target Android OS: API 28 (Pie 9.0)", color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Native 32-bit Shared Libraries:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    inspection.dynamicLibraries.forEach { lib ->
                        Text(text = "• $lib", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val apk = inspection
                            inspectedFile = null
                            vm.openApkInstaller(
                                com.example.engine.ApkMetadata(
                                    fileName = "${apk.appName.lowercase().replace(" ", "_")}.apk",
                                    appName = apk.appName,
                                    packageName = apk.packageName,
                                    versionName = "1.0-pie",
                                    fileSizeMb = 34.5f,
                                    architecture = apk.abiType,
                                    is32BitCompatible = true,
                                    nativeLibraries = apk.dynamicLibraries,
                                    hasClassesDex = true,
                                    recommendedGenre = "ACTION",
                                    summary = "Installed via Android 9 Storage Manager."
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Install APK")
                    }
                    Button(
                        onClick = { inspectedFile = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Install Custom Package Dialog
    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text("Install 32-Bit APK Package", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Install pre-verified legacy 32-bit game package into the Android 9 Sandbox:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.installApkPackage(
                                    name = "Pixel Striker 32",
                                    pkg = "com.retro.pixelstriker",
                                    sizeMb = 31.4f,
                                    type = "SHMUP",
                                    isGame = true,
                                    summary = "Retro space combat 32-bit arcade classic."
                                )
                                showInstallDialog = false
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Pixel Striker 32 (APK)", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("armeabi-v7a • 31.4 MB • Android 9 Pie", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstallDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
