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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InstalledAppEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android9AppStoreScreen(
    vm: VmViewModel,
    installedApps: List<InstalledAppEntity>,
    onLaunchApp: (InstalledAppEntity) -> Unit,
    onBack: () -> Unit
) {
    data class StoreItem(
        val id: String,
        val title: String,
        val pkg: String,
        val genre: String,
        val sizeMb: Float,
        val iconType: String,
        val description: String,
        val rating: Float
    )

    val storeCatalog = listOf(
        StoreItem(
            id = "game.drift32",
            title = "Turbo Drift 32",
            pkg = "com.retro32.turbodrift",
            genre = "Arcade Racer",
            sizeMb = 48.6f,
            iconType = "RACER",
            description = "High-velocity pseudo-3D scaler arcade racer with nitro boosting & dynamic traffic.",
            rating = 4.9f
        ),
        StoreItem(
            id = "game.commando32",
            title = "Cyber Commando: 1999",
            pkg = "com.retro32.commando99",
            genre = "Run & Gun",
            sizeMb = 36.2f,
            iconType = "ACTION",
            description = "Side-scrolling futuristic combat shooter with spread-shot powerups and boss mechs.",
            rating = 4.8f
        ),
        StoreItem(
            id = "game.squadron32",
            title = "Aero Squadron 32",
            pkg = "com.arcade32.aerosquadron",
            genre = "Vertical Shmup",
            sizeMb = 29.8f,
            iconType = "SHMUP",
            description = "Intense 32-bit bullet hell space shooter with laser cannons & screen-clearing bombs.",
            rating = 4.9f
        ),
        StoreItem(
            id = "game.dungeon32",
            title = "Dungeon Crawler 32",
            pkg = "com.retro32.dungeoncrawl",
            genre = "Retro Action RPG",
            sizeMb = 42.1f,
            iconType = "RPG",
            description = "Top-down dungeon explorer featuring sword slashes, fireball spells, and treasure chests.",
            rating = 4.7f
        ),
        StoreItem(
            id = "game.hyper_rally",
            title = "Hyper Rally 32",
            pkg = "com.retro32.hyperrally",
            genre = "Rally Racing",
            sizeMb = 22.4f,
            iconType = "RACER",
            description = "Retro rally championship on dirt, snow, and asphalt with physics drifting.",
            rating = 4.6f
        ),
        StoreItem(
            id = "game.laser_blast",
            title = "Laser Blast 32",
            pkg = "com.retro32.laserblast",
            genre = "Sci-Fi Shooter",
            sizeMb = 19.5f,
            iconType = "SHMUP",
            description = "Neon grid vector arcade shooter with particle explosions and combo chains.",
            rating = 4.5f
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "32-Bit Game Hub",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Native Android 9 Pie Compatibility Catalog",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("store_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { vm.openApkInstaller() }
                            .testTag("store_sideload_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add APK", tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ADD APK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0A0F1D)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF131C2E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "32-Bit Native Acceleration",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All titles run natively through ARMv7-A sandbox with direct GPU hardware acceleration.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            items(storeCatalog) { item ->
                val installedApp = installedApps.find { it.id == item.id }
                val isInstalled = installedApp != null

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (brush, icon, tint) = getAppVisuals(item.iconType, true)
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(brush),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${item.genre} • ${item.sizeMb} MB • ★ ${item.rating}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isInstalled) {
                                Button(
                                    onClick = { onLaunchApp(installedApp!!) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("play_btn_${item.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        vm.installApkPackage(
                                            name = item.title,
                                            pkg = item.pkg,
                                            sizeMb = item.sizeMb,
                                            type = item.iconType,
                                            isGame = true,
                                            summary = item.description
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("install_btn_${item.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GET", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.description,
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ABI: armeabi-v7a (32-bit) • Target SDK 28",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                            if (isInstalled && !installedApp!!.isSystemApp && item.id !in listOf("game.drift32", "game.commando32", "game.squadron32", "game.dungeon32")) {
                                OutlinedButton(
                                    onClick = { vm.uninstallPackage(item.id) },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Uninstall", color = Color(0xFFEF4444), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
