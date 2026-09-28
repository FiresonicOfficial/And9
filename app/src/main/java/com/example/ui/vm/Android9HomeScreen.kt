package com.example.ui.vm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InstalledAppEntity
import com.example.data.local.entity.VmConfigEntity

@Composable
fun Android9HomeScreen(
    vm: VmViewModel,
    apps: List<InstalledAppEntity>,
    config: VmConfigEntity?,
    onLaunchApp: (InstalledAppEntity) -> Unit
) {
    val wallpaperBrush = when (config?.wallpaper) {
        "CYBER_DARK" -> Brush.verticalGradient(listOf(Color(0xFF030712), Color(0xFF0B132B), Color(0xFF1C2541)))
        "RETRO_NEON" -> Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF4C1D95), Color(0xFF831843)))
        else -> Brush.verticalGradient(listOf(Color(0xFF0B192C), Color(0xFF1E3E62), Color(0xFF000000))) // Pie default
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(wallpaperBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Section: Pie Clock & Telemetry Widget
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Monday, Sep 28",
                    color = Color(0xFFE2E8F0),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "10:28",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GLES 3.2 HW ACCEL",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "ARMv7-A 32-Bit JIT Active",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pie Search Pill Widget
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0x66FFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { vm.audioSynth.playClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search 32-bit games & apps...",
                            color = Color(0xEEFFFFFF),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 2. Middle Section: App Grid (Games & System Apps)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "32-BIT GAMES & VIRTUAL SANDBOX",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0284C7),
                        modifier = Modifier
                            .clickable { vm.openApkInstaller() }
                            .testTag("home_add_apk_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add APK",
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
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(apps) { app ->
                        AppIconItem(
                            app = app,
                            onClick = { onLaunchApp(app) }
                        )
                    }

                    // Plus Add APK Item in Grid
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { vm.openApkInstaller() }
                                .testTag("app_icon_add_apk")
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Install APK",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add APK",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "+ Sideload",
                                color = Color(0xFF38BDF8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Bottom Dock Section: Quick Access Apps
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x550F172A),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dockApps = apps.filter {
                        it.id in listOf("game.drift32", "game.commando32", "app.store", "app.files", "app.settings")
                    }
                    dockApps.forEach { app ->
                        DockIconItem(
                            app = app,
                            onClick = { onLaunchApp(app) }
                        )
                    }
                }
            }
        }

        // Recents Task Switcher Overlay
        AnimatedVisibility(
            visible = vm.isRecentsOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            RecentsTaskSwitcher(
                vm = vm,
                recentApps = vm.recentApps,
                onSelectApp = { app ->
                    vm.toggleRecents()
                    vm.launchApp(app)
                },
                onClose = { vm.toggleRecents() }
            )
        }
    }
}

@Composable
fun AppIconItem(
    app: InstalledAppEntity,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("app_icon_${app.id}")
            .padding(4.dp)
    ) {
        val (bgBrush, iconVec, iconTint) = getAppVisuals(app.iconType, app.isGame)

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVec,
                contentDescription = app.name,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.name,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        if (app.isGame) {
            Text(
                text = "32-bit",
                color = Color(0xFF38BDF8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DockIconItem(
    app: InstalledAppEntity,
    onClick: () -> Unit
) {
    val (bgBrush, iconVec, iconTint) = getAppVisuals(app.iconType, app.isGame)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bgBrush)
            .clickable(onClick = onClick)
            .testTag("dock_icon_${app.id}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVec,
            contentDescription = app.name,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun getAppVisuals(iconType: String, isGame: Boolean): Triple<Brush, ImageVector, Color> {
    return when (iconType) {
        "RACER" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF9F1239))),
            Icons.Default.Speed,
            Color.White
        )
        "ACTION" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1E40AF))),
            Icons.Default.SportsEsports,
            Color.White
        )
        "SHMUP" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF0D9488), Color(0xFF115E59))),
            Icons.Default.PlayArrow,
            Color.White
        )
        "RPG" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF7C3AED), Color(0xFF5B21B6))),
            Icons.Default.Security,
            Color.White
        )
        "SETTINGS" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))),
            Icons.Default.Settings,
            Color.White
        )
        "FILES" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
            Icons.Default.Folder,
            Color.White
        )
        "STORE" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857))),
            Icons.Default.ShoppingBag,
            Color.White
        )
        "TERMINAL" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))),
            Icons.Default.Terminal,
            Color(0xFF22C55E)
        )
        else -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))),
            Icons.Default.Gamepad,
            Color.White
        )
    }
}

@Composable
fun RecentsTaskSwitcher(
    vm: VmViewModel,
    recentApps: List<InstalledAppEntity>,
    onSelectApp: (InstalledAppEntity) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Running Tasks (Android 9 Pie)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (recentApps.isEmpty()) {
                Text(
                    text = "No recent 32-bit apps running",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(recentApps) { app ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .height(280.dp)
                                .clickable { onSelectApp(app) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (brush, icon, tint) = getAppVisuals(app.iconType, app.isGame)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(brush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = app.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = app.architecture,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (app.isGame) "32-Bit Game In Memory\n[HW Accelerated]" else "System Process Active",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Text(
                                    text = "Tap to switch task",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
