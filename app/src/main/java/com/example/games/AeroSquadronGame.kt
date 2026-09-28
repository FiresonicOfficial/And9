package com.example.games

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.engine.audio.RetroAudioSynthesizer
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class AeroSquadronGame : VirtualGame {

    override val id: String = "game.squadron32"
    override val title: String = "Aero Squadron 32"

    private var playerX = 400f
    private var playerY = 650f
    private var moveX = 0f
    private var moveY = 0f
    private var health = 100
    private var bombCount = 3
    private var score = 0
    private var gameOver = false

    private val playerSpeed = 380f
    private var shootCooldown = 0f

    // Starfield particles
    data class Star(var x: Float, var y: Float, val speed: Float, val size: Float, val color: Color)
    private val stars = mutableListOf<Star>()

    // Bullets
    data class ShmupBullet(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val isPlayer: Boolean,
        val color: Color
    )
    private val bullets = mutableListOf<ShmupBullet>()

    // Enemies
    data class AlienFighter(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var health: Int,
        val isBoss: Boolean,
        var shootTimer: Float
    )
    private val enemies = mutableListOf<AlienFighter>()
    private var spawnTimer = 0f

    // Mega Bomb shockwave
    private var bombActive = false
    private var bombRadius = 0f

    // Particles
    data class ShmupParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        val color: Color
    )
    private val particles = mutableListOf<ShmupParticle>()
    private val random = Random()

    override fun init(width: Float, height: Float) {
        reset()
        // Initialize starfield
        stars.clear()
        for (i in 0..60) {
            stars.add(
                Star(
                    x = random.nextFloat() * 800f,
                    y = random.nextFloat() * 800f,
                    speed = 40f + random.nextFloat() * 140f,
                    size = 1f + random.nextFloat() * 2.5f,
                    color = if (random.nextBoolean()) Color(0xFF67E8F9) else Color(0xFFE2E8F0)
                )
            )
        }
    }

    override fun reset() {
        playerX = 400f
        playerY = 650f
        moveX = 0f
        moveY = 0f
        health = 100
        bombCount = 3
        score = 0
        gameOver = false
        shootCooldown = 0f
        spawnTimer = 0f
        bombActive = false
        bombRadius = 0f
        bullets.clear()
        enemies.clear()
        particles.clear()
    }

    override fun update(dt: Float, audio: RetroAudioSynthesizer) {
        if (gameOver) return

        shootCooldown -= dt
        spawnTimer += dt

        // Player movement
        playerX = (playerX + moveX * playerSpeed * dt).coerceIn(40f, 760f)
        playerY = (playerY + moveY * playerSpeed * dt).coerceIn(100f, 750f)

        // Update Starfield
        for (star in stars) {
            star.y += star.speed * dt
            if (star.y > 800f) {
                star.y = 0f
                star.x = random.nextFloat() * 800f
            }
        }

        // Bomb shockwave expansion
        if (bombActive) {
            bombRadius += 1200f * dt
            // Clear bullets & damage enemies
            bullets.removeAll { !it.isPlayer }
            for (e in enemies) e.health -= 5
            if (bombRadius > 900f) {
                bombActive = false
                bombRadius = 0f
            }
        }

        // Spawn alien fighters
        if (spawnTimer > 1.4f) {
            spawnTimer = 0f
            val isBoss = score > 1500 && random.nextFloat() < 0.15f && enemies.none { it.isBoss }
            enemies.add(
                AlienFighter(
                    x = 80f + random.nextFloat() * 640f,
                    y = -40f,
                    vx = (random.nextFloat() * 2f - 1f) * 60f,
                    vy = if (isBoss) 30f else 110f + random.nextFloat() * 70f,
                    health = if (isBoss) 25 else 2,
                    isBoss = isBoss,
                    shootTimer = 1.0f + random.nextFloat()
                )
            )
        }

        // Update enemies
        val eIter = enemies.iterator()
        while (eIter.hasNext()) {
            val e = eIter.next()
            e.x += e.vx * dt
            e.y += e.vy * dt
            e.shootTimer -= dt

            if (e.isBoss && e.y > 180f) {
                e.vy = 0f // Boss stops in upper field
                if (e.x < 100f || e.x > 700f) e.vx = -e.vx
            }

            if (e.shootTimer <= 0f) {
                e.shootTimer = if (e.isBoss) 0.6f else 1.8f
                if (e.isBoss) {
                    // Spread boss bullets
                    for (angle in listOf(-0.4, 0.0, 0.4)) {
                        bullets.add(
                            ShmupBullet(
                                x = e.x,
                                y = e.y + 20f,
                                vx = (sin(angle) * 240.0).toFloat(),
                                vy = (cos(angle) * 240.0).toFloat(),
                                isPlayer = false,
                                color = Color(0xFFF43F5E)
                            )
                        )
                    }
                } else {
                    bullets.add(
                        ShmupBullet(
                            x = e.x,
                            y = e.y + 15f,
                            vx = 0f,
                            vy = 260f,
                            isPlayer = false,
                            color = Color(0xFFFB923C)
                        )
                    )
                }
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
                    val hitRadius = if (e.isBoss) 55f else 25f
                    if (abs(b.x - e.x) < hitRadius && abs(b.y - e.y) < hitRadius) {
                        e.health--
                        hit = true
                        audio.playHit()
                        if (e.health <= 0) {
                            score += if (e.isBoss) 1000 else 120
                            audio.playExplosion()
                            for (p in 0..(if (e.isBoss) 25 else 8)) {
                                particles.add(
                                    ShmupParticle(
                                        x = e.x,
                                        y = e.y,
                                        vx = (random.nextFloat() * 2f - 1f) * 250f,
                                        vy = (random.nextFloat() * 2f - 1f) * 250f,
                                        life = 0.5f,
                                        color = if (random.nextBoolean()) Color(0xFFFACC15) else Color(0xFFEF4444)
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
                if (abs(b.x - playerX) < 22f && abs(b.y - playerY) < 22f) {
                    health -= 15
                    audio.playHit()
                    bIter.remove()
                    if (health <= 0) {
                        health = 0
                        gameOver = true
                        audio.playExplosion()
                    }
                    continue
                }
            }
        }

        // Clean dead enemies
        enemies.removeAll { it.health <= 0 }

        // Particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.life -= dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.life <= 0f) pIter.remove()
        }
    }

    override fun render(drawScope: DrawScope, width: Float, height: Float) {
        // Deep Space Background
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF030712), Color(0xFF0C0A1D), Color(0xFF050816))
            ),
            size = Size(width, height)
        )

        // Starfield
        for (star in stars) {
            val sx = (star.x / 800f) * width
            val sy = (star.y / 800f) * height
            drawScope.drawCircle(
                color = star.color,
                radius = star.size,
                center = Offset(sx, sy)
            )
        }

        // Mega Bomb Shockwave
        if (bombActive) {
            val sRadius = (bombRadius / 800f) * width
            drawScope.drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                radius = sRadius,
                center = Offset((playerX / 800f) * width, (playerY / 800f) * height)
            )
        }

        // Render Enemies
        for (e in enemies) {
            val ex = (e.x / 800f) * width
            val ey = (e.y / 800f) * height

            if (e.isBoss) {
                // Massive Alien Dreadnought
                drawScope.drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF9333EA), Color(0xFF581C87), Color(0xFF3B0764))
                    ),
                    topLeft = Offset(ex - 50f, ey - 30f),
                    size = Size(100f, 60f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f)
                )
                // Glowing Core
                drawScope.drawCircle(
                    color = Color(0xFFF43F5E),
                    radius = 16f,
                    center = Offset(ex, ey)
                )
            } else {
                // Alien Fighter
                val shipPath = Path().apply {
                    moveTo(ex, ey + 18f)
                    lineTo(ex - 18f, ey - 14f)
                    lineTo(ex, ey - 6f)
                    lineTo(ex + 18f, ey - 14f)
                    close()
                }
                drawScope.drawPath(shipPath, color = Color(0xFFE11D48))
            }
        }

        // Render Player Fighter Jet
        val px = (playerX / 800f) * width
        val py = (playerY / 800f) * height

        // Thruster glow
        drawScope.drawCircle(
            color = Color(0xFF38BDF8),
            radius = 10f,
            center = Offset(px, py + 18f)
        )

        // Jet Jet Wings & Fuselage
        val jetPath = Path().apply {
            moveTo(px, py - 24f) // Nose
            lineTo(px + 22f, py + 16f) // Right wing tip
            lineTo(px + 8f, py + 10f) // Right wing inset
            lineTo(px, py + 14f) // Tail
            lineTo(px - 8f, py + 10f) // Left wing inset
            lineTo(px - 22f, py + 16f) // Left wing tip
            close()
        }
        drawScope.drawPath(
            jetPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0C4A6E))
            )
        )
        // Cockpit
        drawScope.drawOval(
            color = Color(0xFFF0FDF4),
            topLeft = Offset(px - 4f, py - 12f),
            size = Size(8f, 14f)
        )

        // Bullets
        for (b in bullets) {
            val bx = (b.x / 800f) * width
            val by = (b.y / 800f) * height
            drawScope.drawCircle(
                color = b.color,
                radius = if (b.isPlayer) 5f else 4f,
                center = Offset(bx, by)
            )
        }

        // Particles
        for (p in particles) {
            val pxPos = (p.x / 800f) * width
            val pyPos = (p.y / 800f) * height
            drawScope.drawCircle(
                color = p.color.copy(alpha = p.life * 2f.coerceAtMost(1f)),
                radius = 5f * p.life,
                center = Offset(pxPos, pyPos)
            )
        }

        // Top HUD
        drawScope.drawRoundRect(
            color = Color(0xCC0B0F19),
            topLeft = Offset(16f, 16f),
            size = Size(width - 32f, 40f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        // HP
        val hpW = 100f
        drawScope.drawRect(
            color = Color(0xFF1F2937),
            topLeft = Offset(24f, 24f),
            size = Size(hpW, 12f)
        )
        drawScope.drawRect(
            color = Color(0xFF10B981),
            topLeft = Offset(24f, 24f),
            size = Size(hpW * (health / 100f), 12f)
        )
    }

    override fun onDpad(dx: Float, dy: Float) {
        moveX = dx
        moveY = dy
    }

    override fun onButtonDown(button: GameButton, audio: RetroAudioSynthesizer) {
        when (button) {
            GameButton.A, GameButton.B -> {
                // Fire twin plasma
                if (shootCooldown <= 0f) {
                    shootCooldown = 0.14f
                    bullets.add(ShmupBullet(playerX - 12f, playerY - 20f, 0f, -650f, true, Color(0xFF38BDF8)))
                    bullets.add(ShmupBullet(playerX + 12f, playerY - 20f, 0f, -650f, true, Color(0xFF38BDF8)))
                    audio.playLaser()
                }
            }
            GameButton.X, GameButton.Y -> {
                // Mega Bomb
                if (bombCount > 0 && !bombActive) {
                    bombCount--
                    bombActive = true
                    bombRadius = 10f
                    audio.playExplosion()
                }
            }
            GameButton.START -> reset()
            else -> {}
        }
    }

    override fun onButtonUp(button: GameButton) {}

    override fun getScore(): Int = score

    override fun isGameOver(): Boolean = gameOver
}
