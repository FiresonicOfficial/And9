package com.example.ui.vm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.games.GameButton
import com.example.games.VirtualGame
import kotlin.math.roundToInt

@Composable
fun GameViewportScreen(
    vm: VmViewModel,
    game: VirtualGame,
    modifier: Modifier = Modifier
) {
    val hw = vm.hwEngine

    // Game Loop driven by hardware V-Sync Choreographer frame callbacks
    LaunchedEffect(game) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                hw.onFrameStart(frameTimeNanos)
                val dt = hw.getDeltaTimeSeconds()
                game.update(dt, vm.audioSynth)
                vm.armLayer.stepJitCycles(1)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // 1. Hardware Accelerated Game Viewport Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            game.render(this, size.width, size.height)

            // CRT Scanlines Shader simulation
            if (hw.activeShader == "CRT_SCANLINES") {
                val scanlineSpacing = 4f
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color(0x33000000),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.2f
                    )
                    y += scanlineSpacing
                }
            } else if (hw.activeShader == "NEON_GLOW") {
                drawRect(
                    color = Color(0x1500F0FF),
                    size = size
                )
            }
        }

        // 2. Hardware Acceleration Live Performance HUD Overlay
        if (vm.showFpsHud) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xCC0B132B),
                modifier = Modifier
                    .padding(start = 12.dp, top = 36.dp)
                    .align(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "HW Accel",
                        tint = if (hw.isHwAccelerated) Color(0xFF38BDF8) else Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${hw.currentFps} FPS",
                        color = if (hw.currentFps >= 50) Color(0xFF10B981) else Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${hw.frameTimeMs}ms • GPU ${hw.gpuLoadPercentage}%",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Quick Game Bar (HUD controls: Restart, Keymapper, HUD toggle)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { vm.resetActiveGame() },
                modifier = Modifier.testTag("game_restart_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = { vm.isKeymapperOpen = !vm.isKeymapperOpen },
                modifier = Modifier.testTag("game_keymapper_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Keymapper",
                    tint = if (vm.isKeymapperOpen) Color(0xFF38BDF8) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { vm.showFpsHud = !vm.showFpsHud },
                modifier = Modifier.testTag("game_hud_btn")
            ) {
                Icon(
                    imageVector = if (vm.showFpsHud) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle HUD",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 3. Keymapper Layout Editor Bar (when active)
        AnimatedVisibility(
            visible = vm.isKeymapperOpen,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xEE0F172A),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Keymapper Active: Drag D-Pad and Buttons to reposition",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { vm.isKeymapperOpen = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // 4. On-Screen Virtual Gamepad Overlay
        VirtualGamepadOverlay(
            vm = vm,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun VirtualGamepadOverlay(
    vm: VmViewModel,
    modifier: Modifier = Modifier
) {
    val opacity = vm.gamepadOpacity
    val scale = vm.buttonScale

    Box(modifier = modifier) {
        // Left Side: D-Pad
        Box(
            modifier = Modifier
                .offset { IntOffset(vm.dpadOffsetX.roundToInt(), -vm.dpadOffsetY.roundToInt()) }
                .align(Alignment.BottomStart)
                .then(
                    if (vm.isKeymapperOpen) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                vm.dpadOffsetX = (vm.dpadOffsetX + dragAmount.x).coerceIn(10f, 300f)
                                vm.dpadOffsetY = (vm.dpadOffsetY - dragAmount.y).coerceIn(10f, 400f)
                            }
                        }
                    } else Modifier
                )
                .alpha(opacity)
        ) {
            VirtualDpad(
                onDirection = { dx, dy -> vm.onGameDpad(dx, dy) },
                scale = scale
            )
        }

        // Right Side: Action Buttons (A, B, X, Y)
        Box(
            modifier = Modifier
                .offset { IntOffset(-vm.buttonsOffsetX.roundToInt(), -vm.buttonsOffsetY.roundToInt()) }
                .align(Alignment.BottomEnd)
                .then(
                    if (vm.isKeymapperOpen) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                vm.buttonsOffsetX = (vm.buttonsOffsetX - dragAmount.x).coerceIn(10f, 300f)
                                vm.buttonsOffsetY = (vm.buttonsOffsetY - dragAmount.y).coerceIn(10f, 400f)
                            }
                        }
                    } else Modifier
                )
                .alpha(opacity)
        ) {
            VirtualActionCluster(
                onButtonDown = { btn -> vm.onGameButtonDown(btn) },
                onButtonUp = { btn -> vm.onGameButtonUp(btn) },
                scale = scale
            )
        }

        // Top Shoulders: L1 and R1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 72.dp)
                .align(Alignment.TopCenter)
                .alpha(opacity),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VirtualShoulderButton(
                label = "L1",
                onDown = { vm.onGameButtonDown(GameButton.L1) },
                onUp = { vm.onGameButtonUp(GameButton.L1) }
            )
            VirtualShoulderButton(
                label = "R1",
                onDown = { vm.onGameButtonDown(GameButton.R1) },
                onUp = { vm.onGameButtonUp(GameButton.R1) }
            )
        }
    }
}

@Composable
fun VirtualDpad(
    onDirection: (Float, Float) -> Unit,
    scale: Float
) {
    val dpadSize = (140 * scale).dp
    val buttonSize = (44 * scale).dp

    Box(
        modifier = Modifier
            .size(dpadSize)
            .background(Color(0x550F172A), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // UP
        DpadDirectionButton(
            label = "▲",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(buttonSize)
                .testTag("dpad_up"),
            onTouchDown = { onDirection(0f, -1f) },
            onTouchUp = { onDirection(0f, 0f) }
        )

        // DOWN
        DpadDirectionButton(
            label = "▼",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(buttonSize)
                .testTag("dpad_down"),
            onTouchDown = { onDirection(0f, 1f) },
            onTouchUp = { onDirection(0f, 0f) }
        )

        // LEFT
        DpadDirectionButton(
            label = "◀",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(buttonSize)
                .testTag("dpad_left"),
            onTouchDown = { onDirection(-1f, 0f) },
            onTouchUp = { onDirection(0f, 0f) }
        )

        // RIGHT
        DpadDirectionButton(
            label = "▶",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(buttonSize)
                .testTag("dpad_right"),
            onTouchDown = { onDirection(1f, 0f) },
            onTouchUp = { onDirection(0f, 0f) }
        )
    }
}

@Composable
fun DpadDirectionButton(
    label: String,
    modifier: Modifier = Modifier,
    onTouchDown: () -> Unit,
    onTouchUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) Color(0xFF0284C7) else Color(0xAA1E293B))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onTouchDown()
                        tryAwaitRelease()
                        isPressed = false
                        onTouchUp()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VirtualActionCluster(
    onButtonDown: (GameButton) -> Unit,
    onButtonUp: (GameButton) -> Unit,
    scale: Float
) {
    val clusterSize = (140 * scale).dp
    val btnSize = (48 * scale).dp

    Box(
        modifier = Modifier
            .size(clusterSize)
            .background(Color(0x550F172A), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Y Button (Top - Amber)
        ArcadeActionButton(
            label = "Y",
            color = Color(0xFFF59E0B),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(btnSize)
                .testTag("btn_y"),
            onDown = { onButtonDown(GameButton.Y) },
            onUp = { onButtonUp(GameButton.Y) }
        )

        // A Button (Bottom - Emerald Green)
        ArcadeActionButton(
            label = "A",
            color = Color(0xFF10B981),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(btnSize)
                .testTag("btn_a"),
            onDown = { onButtonDown(GameButton.A) },
            onUp = { onButtonUp(GameButton.A) }
        )

        // X Button (Left - Sky Blue)
        ArcadeActionButton(
            label = "X",
            color = Color(0xFF0284C7),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(btnSize)
                .testTag("btn_x"),
            onDown = { onButtonDown(GameButton.X) },
            onUp = { onButtonUp(GameButton.X) }
        )

        // B Button (Right - Crimson Red)
        ArcadeActionButton(
            label = "B",
            color = Color(0xFFEF4444),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(btnSize)
                .testTag("btn_b"),
            onDown = { onButtonDown(GameButton.B) },
            onUp = { onButtonUp(GameButton.B) }
        )
    }
}

@Composable
fun ArcadeActionButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isPressed) Color.White else color)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onDown()
                        tryAwaitRelease()
                        isPressed = false
                        onUp()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) Color.Black else Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun VirtualShoulderButton(
    label: String,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isPressed) Color(0xFF0284C7) else Color(0x991E293B),
        modifier = Modifier
            .width(68.dp)
            .height(34.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onDown()
                        tryAwaitRelease()
                        isPressed = false
                        onUp()
                    }
                )
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
