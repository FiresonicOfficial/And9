package com.example.games

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import com.example.engine.audio.RetroAudioSynthesizer
import java.util.Random
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class TurboDriftGame : VirtualGame {

    override val id: String = "game.drift32"
    override val title: String = "Turbo Drift 32"

    private var playerX = 0.0f // -1.0 to 1.0 (road center is 0.0)
    private var speed = 0.0f // 0.0 to 260.0 km/h
    private var maxSpeed = 220.0f
    private var nitroRemaining = 100.0f // 0 to 100%
    private var isAccelerating = false
    private var isBraking = false
    private var isNitroActive = false
    private var steerInput = 0.0f

    private var distanceTraveled = 0.0f
    private var score = 0
    private var lapTime = 0.0f
    private var gameOver = false

    // Road curve & elevation simulation
    private var roadCurve = 0.0f
    private var targetCurve = 0.0f
    private var curveTimer = 0.0f
    private val random = Random()

    // Traffic cars
    data class TrafficCar(
        var x: Float,
        var z: Float, // 0.0 (near) to 1.0 (horizon)
        var speed: Float,
        val color: Color
    )

    private val traffic = mutableListOf<TrafficCar>()

    // Nitro & exhaust particles
    data class SparkParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        val color: Color
    )

    private val particles = mutableListOf<SparkParticle>()

    override fun init(width: Float, height: Float) {
        reset()
    }

    override fun reset() {
        playerX = 0.0f
        speed = 0.0f
        maxSpeed = 220.0f
        nitroRemaining = 100.0f
        isAccelerating = false
        isBraking = false
        isNitroActive = false
        steerInput = 0.0f
        distanceTraveled = 0.0f
        score = 0
        lapTime = 0.0f
        gameOver = false
        roadCurve = 0.0f
        targetCurve = 0.0f
        curveTimer = 0.0f

        traffic.clear()
        val carColors = listOf(Color(0xFFE11D48), Color(0xFF2563EB), Color(0xFFFBBF24), Color(0xFF10B981))
        traffic.add(TrafficCar(x = -0.4f, z = 0.8f, speed = 80f, color = carColors[0]))
        traffic.add(TrafficCar(x = 0.3f, z = 0.6f, speed = 95f, color = carColors[1]))
        traffic.add(TrafficCar(x = -0.1f, z = 0.35f, speed = 110f, color = carColors[2]))
        particles.clear()
    }

    override fun update(dt: Float, audio: RetroAudioSynthesizer) {
        if (gameOver) return

        lapTime += dt
        curveTimer += dt
        if (curveTimer > 4.0f) {
            curveTimer = 0.0f
            targetCurve = (random.nextFloat() * 2.0f - 1.0f) * 1.5f
        }
        roadCurve += (targetCurve - roadCurve) * min(1.0f, dt * 1.2f)

        // Acceleration / Deceleration
        val currentMax = if (isNitroActive && nitroRemaining > 0f) 280.0f else maxSpeed
        if (isAccelerating) {
            speed += (if (isNitroActive) 140f else 85f) * dt
            if (speed > currentMax) speed = currentMax
        } else if (isBraking) {
            speed -= 160f * dt
            if (speed < 0f) speed = 0f
        } else {
            speed -= 40f * dt
            if (speed < 0f) speed = 0f
        }

        // Nitro consumption & recharge
        if (isNitroActive && nitroRemaining > 0f) {
            nitroRemaining -= 35f * dt
            if (nitroRemaining <= 0f) {
                nitroRemaining = 0f
                isNitroActive = false
            }
        } else if (!isNitroActive && nitroRemaining < 100f) {
            nitroRemaining += 10f * dt
            if (nitroRemaining > 100f) nitroRemaining = 100f
        }

        // Steering & centrifugal pull from road curve
        val speedRatio = speed / 220f
        playerX += (steerInput * 1.6f - roadCurve * 0.7f * speedRatio) * dt
        // Offroad grass penalty
        if (abs(playerX) > 0.85f) {
            speed -= 90f * dt
            if (speed < 0f) speed = 0f
            score = max(0, score - (50 * dt).toInt())
        } else {
            score += (speed * 0.15f * dt).toInt()
        }
        playerX = playerX.coerceIn(-1.2f, 1.2f)

        distanceTraveled += speed * dt * 0.277f // meters

        // Update Traffic
        for (car in traffic) {
            val relativeSpeed = (speed - car.speed) * 0.001f
            car.z -= relativeSpeed * dt * 10f
            if (car.z < 0.0f) {
                // Passed player, respawn ahead
                car.z = 1.0f + random.nextFloat() * 0.4f
                car.x = (random.nextFloat() * 1.4f - 0.7f)
                score += 250 // Overtake bonus
                audio.playCoin()
            } else if (car.z > 1.5f) {
                car.z = 0.1f
            }

            // Collision check with player
            if (car.z in 0.02f..0.15f && abs(playerX - car.x) < 0.28f) {
                speed = max(30f, speed - 80f)
                audio.playHit()
                // Spark particles
                for (p in 0..10) {
                    particles.add(
                        SparkParticle(
                            x = playerX,
                            y = 0.82f,
                            vx = (random.nextFloat() * 2f - 1f) * 0.5f,
                            vy = (random.nextFloat() * -1f) * 0.4f,
                            life = 0.4f,
                            color = Color(0xFFFBBF24)
                        )
                    )
                }
            }
        }

        // Nitro particles
        if (isNitroActive && speed > 50f) {
            for (p in 0..2) {
                particles.add(
                    SparkParticle(
                        x = playerX + (random.nextFloat() * 0.08f - 0.04f),
                        y = 0.88f,
                        vx = (random.nextFloat() * 2f - 1f) * 0.1f,
                        vy = 0.3f + random.nextFloat() * 0.2f,
                        life = 0.35f,
                        color = if (random.nextBoolean()) Color(0xFF38BDF8) else Color(0xFF818CF8)
                    )
                )
            }
        }

        // Update particles
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.life -= dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.life <= 0f) iter.remove()
        }
    }

    override fun render(drawScope: DrawScope, width: Float, height: Float) {
        val horizonY = height * 0.45f

        // 1. Retro Sky & Mountains (Hardware accelerated gradients)
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF6B21A8)),
                startY = 0f,
                endY = horizonY
            ),
            size = Size(width, horizonY)
        )

        // Sun / Moon on horizon
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFF43F5E), Color(0xFFFB7185).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(width * 0.5f + roadCurve * 40f, horizonY - 40f),
                radius = 80f
            ),
            radius = 80f,
            center = Offset(width * 0.5f + roadCurve * 40f, horizonY - 40f)
        )

        // Distant Mountain Ridges
        val mtnPath = Path().apply {
            moveTo(0f, horizonY)
            lineTo(width * 0.15f, horizonY - 50f)
            lineTo(width * 0.35f, horizonY - 20f)
            lineTo(width * 0.55f, horizonY - 65f)
            lineTo(width * 0.8f, horizonY - 30f)
            lineTo(width, horizonY - 55f)
            lineTo(width, horizonY)
            close()
        }
        drawScope.drawPath(mtnPath, color = Color(0xFF312E81).copy(alpha = 0.8f))

        // 2. Ground / Grass
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF065F46), Color(0xFF047857), Color(0xFF059669)),
                startY = horizonY,
                endY = height
            ),
            topLeft = Offset(0f, horizonY),
            size = Size(width, height - horizonY)
        )

        // 3. 3D Perspective Road
        val roadSegments = 24
        val roadTopWidth = width * 0.12f
        val roadBottomWidth = width * 0.95f
        val roadCenterX = width * 0.5f

        for (i in roadSegments downTo 1) {
            val p1 = (i - 1).toFloat() / roadSegments
            val p2 = i.toFloat() / roadSegments

            val y1 = horizonY + (height - horizonY) * (p1 * p1)
            val y2 = horizonY + (height - horizonY) * (p2 * p2)

            val w1 = roadTopWidth + (roadBottomWidth - roadTopWidth) * (p1 * p1)
            val w2 = roadTopWidth + (roadBottomWidth - roadTopWidth) * (p2 * p2)

            val curveOffset1 = roadCurve * (p1 * p1) * (width * 0.35f)
            val curveOffset2 = roadCurve * (p2 * p2) * (width * 0.35f)

            val x1 = roadCenterX + curveOffset1
            val x2 = roadCenterX + curveOffset2

            val segmentIndex = ((distanceTraveled * 0.08f) + i).toInt()
            val isEven = segmentIndex % 2 == 0

            // Road asphalt
            val roadColor = if (isEven) Color(0xFF334155) else Color(0xFF1E293B)
            val roadPath = Path().apply {
                moveTo(x1 - w1 / 2, y1)
                lineTo(x1 + w1 / 2, y1)
                lineTo(x2 + w2 / 2, y2)
                lineTo(x2 - w2 / 2, y2)
                close()
            }
            drawScope.drawPath(roadPath, color = roadColor)

            // Red/White striped curbs
            val curbWidth1 = w1 * 0.12f
            val curbWidth2 = w2 * 0.12f
            val curbColor = if (isEven) Color(0xFFE11D48) else Color(0xFFF8FAFC)

            // Left Curb
            val leftCurb = Path().apply {
                moveTo(x1 - w1 / 2 - curbWidth1, y1)
                lineTo(x1 - w1 / 2, y1)
                lineTo(x2 - w2 / 2, y2)
                lineTo(x2 - w2 / 2 - curbWidth2, y2)
                close()
            }
            drawScope.drawPath(leftCurb, color = curbColor)

            // Right Curb
            val rightCurb = Path().apply {
                moveTo(x1 + w1 / 2, y1)
                lineTo(x1 + w1 / 2 + curbWidth1, y1)
                lineTo(x2 + w2 / 2 + curbWidth2, y2)
                lineTo(x2 + w2 / 2, y2)
                close()
            }
            drawScope.drawPath(rightCurb, color = curbColor)

            // Center lane stripe
            if (isEven && i > 3) {
                val stripeW1 = max(2f, w1 * 0.03f)
                val stripeW2 = max(3f, w2 * 0.03f)
                val centerStripe = Path().apply {
                    moveTo(x1 - stripeW1 / 2, y1)
                    lineTo(x1 + stripeW1 / 2, y1)
                    lineTo(x2 + stripeW2 / 2, y2)
                    lineTo(x2 - stripeW2 / 2, y2)
                    close()
                }
                drawScope.drawPath(centerStripe, color = Color(0xFFFBBF24))
            }
        }

        // 4. Render Traffic Cars
        for (car in traffic) {
            if (car.z in 0.05f..1.0f) {
                val p = 1.0f - car.z
                val carY = horizonY + (height - horizonY) * (p * p)
                val roadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * (p * p)
                val curveOffset = roadCurve * (p * p) * (width * 0.35f)
                val carScreenX = roadCenterX + curveOffset + (car.x * roadW * 0.45f)

                val carW = max(14f, 75f * (p * p))
                val carH = max(8f, 40f * (p * p))

                // Car body
                drawScope.drawRoundRect(
                    color = car.color,
                    topLeft = Offset(carScreenX - carW / 2, carY - carH),
                    size = Size(carW, carH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(carW * 0.2f)
                )
                // Car roof
                drawScope.drawRect(
                    color = Color(0xFF0F172A).copy(alpha = 0.7f),
                    topLeft = Offset(carScreenX - carW * 0.35f, carY - carH * 0.85f),
                    size = Size(carW * 0.7f, carH * 0.45f)
                )
                // Taillights
                drawScope.drawRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(carScreenX - carW * 0.42f, carY - carH * 0.3f),
                    size = Size(carW * 0.25f, carH * 0.22f)
                )
                drawScope.drawRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(carScreenX + carW * 0.17f, carY - carH * 0.3f),
                    size = Size(carW * 0.25f, carH * 0.22f)
                )
            }
        }

        // 5. Render Player's 32-Bit Sports Car
        val playerCarY = height * 0.84f
        val playerCarScreenX = roadCenterX + (playerX * roadBottomWidth * 0.42f)
        val pcW = width * 0.24f
        val pcH = pcW * 0.58f

        // Shadow under car
        drawScope.drawOval(
            color = Color(0x66000000),
            topLeft = Offset(playerCarScreenX - pcW * 0.55f, playerCarY + pcH * 0.35f),
            size = Size(pcW * 1.1f, pcH * 0.4f)
        )

        // Tires
        val tireW = pcW * 0.18f
        val tireH = pcH * 0.45f
        drawScope.drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(playerCarScreenX - pcW * 0.48f, playerCarY + pcH * 0.1f),
            size = Size(tireW, tireH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
        drawScope.drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(playerCarScreenX + pcW * 0.30f, playerCarY + pcH * 0.1f),
            size = Size(tireW, tireH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )

        // Main Car Body (Vibrant Cherry / Cyber Red)
        drawScope.drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFF2E63), Color(0xFFC70039), Color(0xFF900C3F))
            ),
            topLeft = Offset(playerCarScreenX - pcW * 0.45f, playerCarY - pcH * 0.4f),
            size = Size(pcW * 0.9f, pcH * 0.85f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(pcW * 0.15f)
        )

        // Cabin / Rear Windshield
        drawScope.drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
            ),
            topLeft = Offset(playerCarScreenX - pcW * 0.32f, playerCarY - pcH * 0.35f),
            size = Size(pcW * 0.64f, pcH * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(pcW * 0.08f)
        )

        // Rear Spoiler Wing
        drawScope.drawRect(
            color = Color(0xFF111827),
            topLeft = Offset(playerCarScreenX - pcW * 0.48f, playerCarY - pcH * 0.45f),
            size = Size(pcW * 0.96f, pcH * 0.12f)
        )

        // Glowing Cyber Taillights
        val tailLightColor = if (isBraking) Color(0xFFFF0055) else Color(0xFFEF4444)
        drawScope.drawRoundRect(
            color = tailLightColor,
            topLeft = Offset(playerCarScreenX - pcW * 0.42f, playerCarY + pcH * 0.05f),
            size = Size(pcW * 0.28f, pcH * 0.18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
        )
        drawScope.drawRoundRect(
            color = tailLightColor,
            topLeft = Offset(playerCarScreenX + pcW * 0.14f, playerCarY + pcH * 0.05f),
            size = Size(pcW * 0.28f, pcH * 0.18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
        )

        // License plate with "ARM-32"
        drawScope.drawRect(
            color = Color(0xFFF8FAFC),
            topLeft = Offset(playerCarScreenX - pcW * 0.14f, playerCarY + pcH * 0.15f),
            size = Size(pcW * 0.28f, pcH * 0.18f)
        )

        // 6. Particles (Nitro flame & collision sparks)
        for (p in particles) {
            val px = roadCenterX + (p.x * roadBottomWidth * 0.42f)
            val py = p.y * height
            drawScope.drawCircle(
                color = p.color.copy(alpha = p.life * 2.5f.coerceAtMost(1f)),
                radius = 5f * p.life,
                center = Offset(px, py)
            )
        }

        // 7. In-Game Arcade HUD (Speed, Nitro, Score)
        renderGameHud(drawScope, width, height)
    }

    private fun renderGameHud(drawScope: DrawScope, width: Float, height: Float) {
        // Top HUD Bar
        drawScope.drawRoundRect(
            color = Color(0xCC0B0F19),
            topLeft = Offset(16f, 16f),
            size = Size(width - 32f, 48f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )

        // Speedometer Gauge (Bottom-Right)
        val speedBoxW = 160f
        val speedBoxH = 68f
        val speedBoxX = width - speedBoxW - 16f
        val speedBoxY = height - speedBoxH - 16f

        drawScope.drawRoundRect(
            color = Color(0xDD0F172A),
            topLeft = Offset(speedBoxX, speedBoxY),
            size = Size(speedBoxW, speedBoxH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )

        // Speed progress bar
        val speedRatio = (speed / 280f).coerceIn(0f, 1f)
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF10B981), Color(0xFFFBBF24), Color(0xFFEF4444))
            ),
            topLeft = Offset(speedBoxX + 12f, speedBoxY + 38f),
            size = Size((speedBoxW - 24f) * speedRatio, 8f)
        )

        // Nitro Bar (Bottom-Left)
        val nitroBoxW = 160f
        val nitroBoxH = 68f
        val nitroBoxX = 16f
        val nitroBoxY = height - nitroBoxH - 16f

        drawScope.drawRoundRect(
            color = Color(0xDD0F172A),
            topLeft = Offset(nitroBoxX, nitroBoxY),
            size = Size(nitroBoxW, nitroBoxH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )

        val nitroRatio = (nitroRemaining / 100f).coerceIn(0f, 1f)
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF818CF8))
            ),
            topLeft = Offset(nitroBoxX + 12f, nitroBoxY + 38f),
            size = Size((nitroBoxW - 24f) * nitroRatio, 8f)
        )
    }

    override fun onDpad(dx: Float, dy: Float) {
        steerInput = dx
        if (dy < -0.3f) isAccelerating = true
        else if (dy > 0.3f) isBraking = true
    }

    override fun onButtonDown(button: GameButton, audio: RetroAudioSynthesizer) {
        when (button) {
            GameButton.A -> isAccelerating = true
            GameButton.B -> isBraking = true
            GameButton.X -> {
                if (nitroRemaining > 15f) {
                    isNitroActive = true
                    audio.playNitro()
                }
            }
            GameButton.Y -> audio.playLaser()
            GameButton.START -> reset()
            else -> {}
        }
    }

    override fun onButtonUp(button: GameButton) {
        when (button) {
            GameButton.A -> isAccelerating = false
            GameButton.B -> isBraking = false
            GameButton.X -> isNitroActive = false
            else -> {}
        }
    }

    override fun getScore(): Int = score

    override fun isGameOver(): Boolean = gameOver
}
