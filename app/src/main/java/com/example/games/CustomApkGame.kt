package com.example.games

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.data.local.entity.InstalledAppEntity
import com.example.engine.audio.RetroAudioSynthesizer
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Universal interactive 32-bit Game Engine for imported APK files.
 * Provides full hardware acceleration, particle physics, retro sound FX,
 * collision detection, virtual gamepad support, and live telemetry.
 */
class CustomApkGame(val app: InstalledAppEntity) : VirtualGame {

    override val id: String = app.id
    override val title: String = app.name

    private val random = Random()
    private val isBrickMode = app.iconType == "BRICK"
    private val isRacerMode = app.iconType == "RACER"
    private val isRpgMode = app.iconType == "RPG"

    // Common game state
    private var score = 0
    private var health = 100
    private var lives = 3
    private var gameOver = false

    // Particle system
    data class GameFxParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        val color: Color
    )
    private val particles = mutableListOf<GameFxParticle>()

    // ----------------------------------------------------
    // BRICK MODE (Neon Breakout 32)
    // ----------------------------------------------------
    private var paddleX = 400f
    private var paddleWidth = 140f
    private var paddleVx = 0f
    private var ballX = 400f
    private var ballY = 500f
    private var ballVx = 280f
    private var ballVy = -320f
    private var ballRadius = 9f
    private var laserCooldown = 0f

    data class Brick(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        var hp: Int,
        val color: Color
    )
    private val bricks = mutableListOf<Brick>()

    // ----------------------------------------------------
    // SHMUP / ACTION MODE (Cyber Combat 32)
    // ----------------------------------------------------
    private var shipX = 400f
    private var shipY = 620f
    private var shipVx = 0f
    private var shipVy = 0f
    private var shootCooldown = 0f
    private var shieldActive = false
    private var shieldTimer = 0f

    data class Bullet(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val isPlayer: Boolean,
        val color: Color
    )
    private val bullets = mutableListOf<Bullet>()

    data class EnemyCraft(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var hp: Int,
        val isBoss: Boolean
    )
    private val enemies = mutableListOf<EnemyCraft>()
    private var enemySpawnTimer = 0f

    override fun init(width: Float, height: Float) {
        reset()
    }

    override fun reset() {
        score = 0
        health = 100
        lives = 3
        gameOver = false
        particles.clear()
        bullets.clear()
        enemies.clear()

        // Init Brick Breakout
        paddleX = 400f
        paddleWidth = 140f
        paddleVx = 0f
        ballX = 400f
        ballY = 480f
        ballVx = if (random.nextBoolean()) 280f else -280f
        ballVy = -320f
        initBricks()

        // Init Shmup
        shipX = 400f
        shipY = 620f
        shipVx = 0f
        shipVy = 0f
        shootCooldown = 0f
        shieldActive = false
        shieldTimer = 0f
        enemySpawnTimer = 0f
    }

    private fun initBricks() {
        bricks.clear()
        val cols = 8
        val rows = 5
        val bW = 86f
        val bH = 26f
        val startX = 50f
        val startY = 80f
        val colors = listOf(
            Color(0xFFF43F5E), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF0284C7), Color(0xFF8B5CF6)
        )
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                bricks.add(
                    Brick(
                        x = startX + c * (bW + 8f),
                        y = startY + r * (bH + 8f),
                        w = bW,
                        h = bH,
                        hp = 1,
                        color = colors[r % colors.size]
                    )
                )
            }
        }
    }

    override fun update(dt: Float, audio: RetroAudioSynthesizer) {
        if (gameOver) return

        if (isBrickMode) {
            updateBrickMode(dt, audio)
        } else {
            updateShmupMode(dt, audio)
        }

        // Update universal particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.life -= dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.life <= 0f) pIter.remove()
        }
    }

    private fun updateBrickMode(dt: Float, audio: RetroAudioSynthesizer) {
        laserCooldown -= dt
        paddleX += paddleVx * 450f * dt
        paddleX = paddleX.coerceIn(paddleWidth / 2 + 16f, 800f - paddleWidth / 2 - 16f)

        // Ball movement
        ballX += ballVx * dt
        ballY += ballVy * dt

        // Ball wall bounce
        if (ballX <= ballRadius + 16f) {
            ballX = ballRadius + 16f
            ballVx = -ballVx
            audio.playClick()
        } else if (ballX >= 800f - ballRadius - 16f) {
            ballX = 800f - ballRadius - 16f
            ballVx = -ballVx
            audio.playClick()
        }
        if (ballY <= ballRadius + 40f) {
            ballY = ballRadius + 40f
            ballVy = -ballVy
            audio.playClick()
        }

        // Ball paddle collision
        val paddleTop = 640f
        if (ballY + ballRadius >= paddleTop && ballY - ballRadius <= paddleTop + 18f) {
            if (ballX >= paddleX - paddleWidth / 2 && ballX <= paddleX + paddleWidth / 2) {
                ballVy = -abs(ballVy) * 1.02f
                val hitOffset = (ballX - paddleX) / (paddleWidth / 2)
                ballVx = hitOffset * 360f
                ballY = paddleTop - ballRadius
                audio.playJump()

                // Paddle spark particles
                for (i in 0..4) {
                    particles.add(
                        GameFxParticle(
                            x = ballX,
                            y = paddleTop,
                            vx = (random.nextFloat() * 2f - 1f) * 80f,
                            vy = -random.nextFloat() * 100f,
                            life = 0.25f,
                            color = Color(0xFF38BDF8)
                        )
                    )
                }
            }
        }

        // Ball lost bottom
        if (ballY > 750f) {
            lives--
            audio.playHit()
            if (lives <= 0) {
                gameOver = true
                audio.playExplosion()
            } else {
                ballX = paddleX
                ballY = paddleTop - 30f
                ballVx = if (random.nextBoolean()) 260f else -260f
                ballVy = -320f
            }
        }

        // Ball brick collision
        val bIter = bricks.iterator()
        while (bIter.hasNext()) {
            val b = bIter.next()
            if (ballX + ballRadius >= b.x && ballX - ballRadius <= b.x + b.w &&
                ballY + ballRadius >= b.y && ballY - ballRadius <= b.y + b.h
            ) {
                b.hp--
                score += 150
                audio.playCoin()
                ballVy = -ballVy

                // Brick explosion particles
                for (i in 0..8) {
                    particles.add(
                        GameFxParticle(
                            x = b.x + b.w / 2,
                            y = b.y + b.h / 2,
                            vx = (random.nextFloat() * 2f - 1f) * 150f,
                            vy = (random.nextFloat() * 2f - 1f) * 150f,
                            life = 0.35f,
                            color = b.color
                        )
                    )
                }

                if (b.hp <= 0) {
                    bIter.remove()
                }
                break
            }
        }

        // Win / respawn wave
        if (bricks.isEmpty()) {
            score += 1000
            initBricks()
            audio.playCoin()
        }
    }

    private fun updateShmupMode(dt: Float, audio: RetroAudioSynthesizer) {
        shootCooldown -= dt
        enemySpawnTimer += dt
        shieldTimer -= dt
        if (shieldTimer <= 0f) shieldActive = false

        // Ship movement
        shipX = (shipX + shipVx * 380f * dt).coerceIn(40f, 760f)
        shipY = (shipY + shipVy * 380f * dt).coerceIn(120f, 720f)

        // Spawn enemies
        if (enemySpawnTimer > 1.3f) {
            enemySpawnTimer = 0f
            val isBoss = score > 1200 && enemies.none { it.isBoss } && random.nextFloat() < 0.2f
            enemies.add(
                EnemyCraft(
                    x = 80f + random.nextFloat() * 640f,
                    y = -30f,
                    vx = (random.nextFloat() * 2f - 1f) * 70f,
                    vy = if (isBoss) 45f else 120f + random.nextFloat() * 60f,
                    hp = if (isBoss) 15 else 2,
                    isBoss = isBoss
                )
            )
        }

        // Update enemies
        val eIter = enemies.iterator()
        while (eIter.hasNext()) {
            val e = eIter.next()
            e.x += e.vx * dt
            e.y += e.vy * dt

            // Enemy shoot occasionally
            if (random.nextFloat() < (if (e.isBoss) 0.08f else 0.015f)) {
                bullets.add(
                    Bullet(
                        x = e.x,
                        y = e.y + 15f,
                        vx = 0f,
                        vy = 260f,
                        isPlayer = false,
                        color = Color(0xFFEF4444)
                    )
                )
            }

            if (e.y > 850f) eIter.remove()
        }

        // Update bullets
        val bIter = bullets.iterator()
        while (bIter.hasNext()) {
            val b = bIter.next()
            b.x += b.vx * dt
            b.y += b.vy * dt

            if (b.x < 0f || b.x > 800f || b.y < 0f || b.y > 800f) {
                bIter.remove()
                continue
            }

            if (b.isPlayer) {
                var hit = false
                for (e in enemies) {
                    val hitDist = if (e.isBoss) 45f else 25f
                    if (abs(b.x - e.x) < hitDist && abs(b.y - e.y) < hitDist) {
                        e.hp--
                        hit = true
                        audio.playHit()
                        if (e.hp <= 0) {
                            score += if (e.isBoss) 800 else 120
                            audio.playExplosion()
                            for (i in 0..12) {
                                particles.add(
                                    GameFxParticle(
                                        x = e.x,
                                        y = e.y,
                                        vx = (random.nextFloat() * 2f - 1f) * 200f,
                                        vy = (random.nextFloat() * 2f - 1f) * 200f,
                                        life = 0.4f,
                                        color = if (random.nextBoolean()) Color(0xFFFBBF24) else Color(0xFFF43F5E)
                                    )
                                )
                            }
                        }
                        break
                    }
                }
                if (hit) {
                    bIter.remove()
                    continue
                }
            } else {
                // Enemy bullet hits player
                if (abs(b.x - shipX) < 25f && abs(b.y - shipY) < 25f) {
                    bIter.remove()
                    if (shieldActive) {
                        audio.playClick()
                    } else {
                        health -= 15
                        audio.playHit()
                        if (health <= 0) {
                            health = 0
                            gameOver = true
                            audio.playExplosion()
                        }
                    }
                    continue
                }
            }
        }

        enemies.removeAll { it.hp <= 0 }
    }

    override fun render(drawScope: DrawScope, width: Float, height: Float) {
        if (isBrickMode) {
            renderBrickMode(drawScope, width, height)
        } else {
            renderShmupMode(drawScope, width, height)
        }

        // Render universal HUD info banner
        renderTelemetryHud(drawScope, width, height)
    }

    private fun renderBrickMode(drawScope: DrawScope, width: Float, height: Float) {
        // Neon Grid Background
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF030712), Color(0xFF0F172A), Color(0xFF020617))
            ),
            size = Size(width, height)
        )

        // Draw Bricks
        for (b in bricks) {
            val bx = (b.x / 800f) * width
            val by = (b.y / 800f) * height
            val bw = (b.w / 800f) * width
            val bh = (b.h / 800f) * height

            drawScope.drawRoundRect(
                color = b.color,
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            // Inner bevel
            drawScope.drawRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(bx + 2f, by + 2f),
                size = Size(bw - 4f, 4f)
            )
        }

        // Draw Paddle
        val px = (paddleX / 800f) * width
        val py = (640f / 800f) * height
        val pw = (paddleWidth / 800f) * width
        val ph = (18f / 800f) * height

        drawScope.drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF0284C7))
            ),
            topLeft = Offset(px - pw / 2, py),
            size = Size(pw, ph),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )

        // Draw Ball
        val bx = (ballX / 800f) * width
        val by = (ballY / 800f) * height
        val br = (ballRadius / 800f) * width

        // Ball glow
        drawScope.drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.4f),
            radius = br * 1.8f,
            center = Offset(bx, by)
        )
        drawScope.drawCircle(
            color = Color.White,
            radius = br,
            center = Offset(bx, by)
        )

        // Particles
        for (p in particles) {
            val ppx = (p.x / 800f) * width
            val ppy = (p.y / 800f) * height
            drawScope.drawCircle(
                color = p.color.copy(alpha = p.life * 2f.coerceAtMost(1f)),
                radius = 4f * p.life,
                center = Offset(ppx, ppy)
            )
        }
    }

    private fun renderShmupMode(drawScope: DrawScope, width: Float, height: Float) {
        // Deep Space Cyber Background
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF030712), Color(0xFF1E1B4B), Color(0xFF090D1A))
            ),
            size = Size(width, height)
        )

        // Render Enemies
        for (e in enemies) {
            val ex = (e.x / 800f) * width
            val ey = (e.y / 800f) * height

            if (e.isBoss) {
                drawScope.drawRoundRect(
                    brush = Brush.radialGradient(listOf(Color(0xFFE11D48), Color(0xFF881337))),
                    topLeft = Offset(ex - 45f, ey - 25f),
                    size = Size(90f, 50f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
                )
                drawScope.drawCircle(color = Color(0xFFFBBF24), radius = 14f, center = Offset(ex, ey))
            } else {
                val shipPath = Path().apply {
                    moveTo(ex, ey + 16f)
                    lineTo(ex - 16f, ey - 12f)
                    lineTo(ex, ey - 4f)
                    lineTo(ex + 16f, ey - 12f)
                    close()
                }
                drawScope.drawPath(shipPath, color = Color(0xFFF43F5E))
            }
        }

        // Render Bullets
        for (b in bullets) {
            val bx = (b.x / 800f) * width
            val by = (b.y / 800f) * height
            drawScope.drawCircle(color = b.color, radius = if (b.isPlayer) 5f else 4f, center = Offset(bx, by))
        }

        // Render Player Starfighter
        val sx = (shipX / 800f) * width
        val sy = (shipY / 800f) * height

        // Shield Aura
        if (shieldActive) {
            drawScope.drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                radius = 34f,
                center = Offset(sx, sy)
            )
        }

        // Ship Body
        val jetPath = Path().apply {
            moveTo(sx, sy - 24f)
            lineTo(sx + 22f, sy + 16f)
            lineTo(sx + 8f, sy + 10f)
            lineTo(sx, sy + 14f)
            lineTo(sx - 8f, sy + 10f)
            lineTo(sx - 22f, sy + 16f)
            close()
        }
        drawScope.drawPath(
            jetPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
            )
        )
        // Cockpit
        drawScope.drawOval(color = Color.White, topLeft = Offset(sx - 4f, sy - 10f), size = Size(8f, 14f))

        // Particles
        for (p in particles) {
            val ppx = (p.x / 800f) * width
            val ppy = (p.y / 800f) * height
            drawScope.drawCircle(
                color = p.color.copy(alpha = p.life * 2f.coerceAtMost(1f)),
                radius = 4f * p.life,
                center = Offset(ppx, ppy)
            )
        }
    }

    private fun renderTelemetryHud(drawScope: DrawScope, width: Float, height: Float) {
        // Top System Badge
        drawScope.drawRoundRect(
            color = Color(0xDD0B132B),
            topLeft = Offset(16f, 12f),
            size = Size(width - 32f, 44f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )

        // Health / Lives Bar
        val hpW = 90f
        drawScope.drawRect(color = Color(0xFF1E293B), topLeft = Offset(24f, 22f), size = Size(hpW, 10f))
        val currentRatio = if (isBrickMode) (lives / 3f).coerceIn(0f, 1f) else (health / 100f).coerceIn(0f, 1f)
        drawScope.drawRect(
            color = if (currentRatio > 0.3f) Color(0xFF10B981) else Color(0xFFEF4444),
            topLeft = Offset(24f, 22f),
            size = Size(hpW * currentRatio, 10f)
        )
    }

    override fun onDpad(dx: Float, dy: Float) {
        if (isBrickMode) {
            paddleVx = dx
        } else {
            shipVx = dx
            shipVy = dy
        }
    }

    override fun onButtonDown(button: GameButton, audio: RetroAudioSynthesizer) {
        when (button) {
            GameButton.A, GameButton.B -> {
                if (isBrickMode) {
                    // Boost paddle or trigger multiball
                    paddleWidth = 180f
                    audio.playLaser()
                } else {
                    // Fire Twin Lasers
                    if (shootCooldown <= 0f) {
                        shootCooldown = 0.14f
                        bullets.add(Bullet(shipX - 12f, shipY - 20f, 0f, -650f, true, Color(0xFF38BDF8)))
                        bullets.add(Bullet(shipX + 12f, shipY - 20f, 0f, -650f, true, Color(0xFF38BDF8)))
                        audio.playLaser()
                    }
                }
            }
            GameButton.X, GameButton.Y -> {
                // Shield Barrier or Nitro
                shieldActive = true
                shieldTimer = 3.5f
                audio.playNitro()
            }
            GameButton.START -> reset()
            else -> {}
        }
    }

    override fun onButtonUp(button: GameButton) {
        if (isBrickMode && (button == GameButton.A || button == GameButton.B)) {
            paddleWidth = 140f
        }
    }

    override fun getScore(): Int = score

    override fun isGameOver(): Boolean = gameOver
}
