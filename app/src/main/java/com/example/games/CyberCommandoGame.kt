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
import kotlin.math.max
import kotlin.math.min

class CyberCommandoGame : VirtualGame {

    override val id: String = "game.commando32"
    override val title: String = "Cyber Commando"

    private var playerX = 120f
    private var playerY = 0f
    private var vy = 0f
    private var isGrounded = false
    private var moveDir = 0f
    private var isFacingRight = true
    private var health = 100
    private var score = 0
    private var gameOver = false

    private val gravity = 1400f
    private val jumpVelocity = -620f
    private val moveSpeed = 240f

    // Bullets
    data class Bullet(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val isPlayer: Boolean,
        val color: Color
    )

    private val bullets = mutableListOf<Bullet>()
    private var shootCooldown = 0f

    // Enemies
    data class Enemy(
        var x: Float,
        var y: Float,
        var vx: Float,
        var health: Int,
        val type: Int, // 0 = Drone, 1 = Ground Mech
        var shootTimer: Float
    )

    private val enemies = mutableListOf<Enemy>()
    private var enemySpawnTimer = 0f

    // Particles
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        val color: Color
    )

    private val particles = mutableListOf<Particle>()
    private val random = Random()

    override fun init(width: Float, height: Float) {
        reset()
    }

    override fun reset() {
        playerX = 120f
        playerY = 320f
        vy = 0f
        isGrounded = false
        moveDir = 0f
        isFacingRight = true
        health = 100
        score = 0
        gameOver = false
        bullets.clear()
        enemies.clear()
        particles.clear()
        shootCooldown = 0f
        enemySpawnTimer = 0f
    }

    override fun update(dt: Float, audio: RetroAudioSynthesizer) {
        if (gameOver) return

        shootCooldown -= dt
        enemySpawnTimer += dt

        // Player physics
        playerX += moveDir * moveSpeed * dt
        playerX = playerX.coerceIn(40f, 960f)

        vy += gravity * dt
        playerY += vy * dt

        val groundY = 380f
        if (playerY >= groundY) {
            playerY = groundY
            vy = 0f
            isGrounded = true
        } else {
            isGrounded = false
        }

        // Spawn enemies
        if (enemySpawnTimer > 2.2f) {
            enemySpawnTimer = 0f
            val type = if (random.nextBoolean()) 0 else 1
            val spawnY = if (type == 0) 180f + random.nextFloat() * 80f else groundY
            enemies.add(
                Enemy(
                    x = 900f,
                    y = spawnY,
                    vx = -(70f + random.nextFloat() * 60f),
                    health = if (type == 0) 2 else 5,
                    type = type,
                    shootTimer = 1.5f + random.nextFloat() * 1.5f
                )
            )
        }

        // Update enemies
        val enemyIter = enemies.iterator()
        while (enemyIter.hasNext()) {
            val e = enemyIter.next()
            e.x += e.vx * dt
            e.shootTimer -= dt

            // Enemy shoot
            if (e.shootTimer <= 0f) {
                e.shootTimer = 2.0f + random.nextFloat() * 1.5f
                bullets.add(
                    Bullet(
                        x = e.x - 15f,
                        y = e.y - 15f,
                        vx = -220f,
                        vy = 0f,
                        isPlayer = false,
                        color = Color(0xFFEF4444)
                    )
                )
            }

            if (e.x < -60f) {
                enemyIter.remove()
            }
        }

        // Update bullets
        val bulletIter = bullets.iterator()
        while (bulletIter.hasNext()) {
            val b = bulletIter.next()
            b.x += b.vx * dt
            b.y += b.vy * dt

            // Check off-screen
            if (b.x < 0f || b.x > 1000f || b.y < 0f || b.y > 600f) {
                bulletIter.remove()
                continue
            }

            // Player bullet hit enemy
            if (b.isPlayer) {
                var hit = false
                for (e in enemies) {
                    val hitDist = if (e.type == 0) 28f else 40f
                    if (abs(b.x - e.x) < hitDist && abs(b.y - e.y) < hitDist) {
                        e.health--
                        hit = true
                        audio.playHit()
                        // Bullet spark
                        for (i in 0..4) {
                            particles.add(
                                Particle(
                                    x = b.x,
                                    y = b.y,
                                    vx = (random.nextFloat() * 2f - 1f) * 120f,
                                    vy = (random.nextFloat() * 2f - 1f) * 120f,
                                    life = 0.25f,
                                    color = Color(0xFFFBBF24)
                                )
                            )
                        }
                        if (e.health <= 0) {
                            score += if (e.type == 0) 150 else 300
                            audio.playExplosion()
                            // Explosion particles
                            for (i in 0..12) {
                                particles.add(
                                    Particle(
                                        x = e.x,
                                        y = e.y,
                                        vx = (random.nextFloat() * 2f - 1f) * 220f,
                                        vy = (random.nextFloat() * 2f - 1f) * 220f,
                                        life = 0.5f,
                                        color = if (random.nextBoolean()) Color(0xFFFF5722) else Color(0xFFFFEB3B)
                                    )
                                )
                            }
                        }
                        break
                    }
                }
                if (hit) {
                    bulletIter.remove()
                    continue
                }
            } else {
                // Enemy bullet hit player
                if (abs(b.x - playerX) < 25f && abs(b.y - (playerY - 25f)) < 30f) {
                    health -= 12
                    audio.playHit()
                    bulletIter.remove()
                    if (health <= 0) {
                        health = 0
                        gameOver = true
                        audio.playExplosion()
                    }
                    continue
                }
            }
        }

        // Remove dead enemies
        enemies.removeAll { it.health <= 0 }

        // Update particles
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
        val groundY = height * 0.72f

        // 1. Futuristic Cyberpunk City Background (Parallax layers)
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF030712), Color(0xFF1E1B4B), Color(0xFF311042)),
                startY = 0f,
                endY = groundY
            ),
            size = Size(width, groundY)
        )

        // Neon City Skyline Buildings
        val bldgWidth = width * 0.12f
        for (i in 0..8) {
            val bx = i * bldgWidth
            val bh = 120f + (i * 37 % 90f)
            drawScope.drawRect(
                color = Color(0xFF111827),
                topLeft = Offset(bx, groundY - bh),
                size = Size(bldgWidth * 0.85f, bh)
            )
            // Windows
            val winColor = if (i % 2 == 0) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFFF43F5E).copy(alpha = 0.4f)
            for (wy in 1..4) {
                drawScope.drawRect(
                    color = winColor,
                    topLeft = Offset(bx + 12f, groundY - bh + (wy * 24f)),
                    size = Size(10f, 10f)
                )
            }
        }

        // 2. High-Tech Industrial Floor Platform
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
                startY = groundY,
                endY = height
            ),
            topLeft = Offset(0f, groundY),
            size = Size(width, height - groundY)
        )
        // Neon Hazard Edge Line
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF00F0FF), Color(0xFFFBBF24), Color(0xFF00F0FF))
            ),
            topLeft = Offset(0f, groundY - 4f),
            size = Size(width, 4f)
        )

        // 3. Render Player Commando
        val px = (playerX / 1000f) * width
        val py = (playerY / 600f) * height
        val pW = 38f
        val pH = 56f

        // Body Armor
        drawScope.drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1))
            ),
            topLeft = Offset(px - pW / 2, py - pH),
            size = Size(pW, pH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        // Helmet Visor
        val visorColor = Color(0xFF38BDF8)
        val visorX = if (isFacingRight) px + 2f else px - pW * 0.35f
        drawScope.drawRoundRect(
            color = visorColor,
            topLeft = Offset(visorX, py - pH + 8f),
            size = Size(14f, 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
        )
        // Weapon Rifle
        val gunW = 26f
        val gunH = 9f
        val gunX = if (isFacingRight) px + 8f else px - 8f - gunW
        drawScope.drawRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(gunX, py - pH * 0.55f),
            size = Size(gunW, gunH)
        )

        // 4. Render Enemies
        for (e in enemies) {
            val ex = (e.x / 1000f) * width
            val ey = (e.y / 600f) * height

            if (e.type == 0) {
                // Drone: Floating round cyber bot
                drawScope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                    ),
                    radius = 18f,
                    center = Offset(ex, ey)
                )
                // Red glowing scanner eye
                drawScope.drawCircle(
                    color = Color(0xFFFF0055),
                    radius = 6f,
                    center = Offset(ex - 6f, ey)
                )
            } else {
                // Ground Heavy Mech
                drawScope.drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF475569), Color(0xFF1E293B))
                    ),
                    topLeft = Offset(ex - 22f, ey - 48f),
                    size = Size(44f, 48f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )
                // Mech red glowing visor
                drawScope.drawRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(ex - 16f, ey - 40f),
                    size = Size(18f, 6f)
                )
            }
        }

        // 5. Render Bullets
        for (b in bullets) {
            val bx = (b.x / 1000f) * width
            val by = (b.y / 600f) * height
            drawScope.drawCircle(
                color = b.color,
                radius = if (b.isPlayer) 5f else 4f,
                center = Offset(bx, by)
            )
        }

        // 6. Render Particles
        for (p in particles) {
            val pxPos = (p.x / 1000f) * width
            val pyPos = (p.y / 600f) * height
            drawScope.drawCircle(
                color = p.color.copy(alpha = p.life * 2f.coerceAtMost(1f)),
                radius = 6f * p.life,
                center = Offset(pxPos, pyPos)
            )
        }

        // 7. HUD: Health Bar & Score
        drawScope.drawRoundRect(
            color = Color(0xCC0B0F19),
            topLeft = Offset(16f, 16f),
            size = Size(width - 32f, 42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        // Health bar
        val hpW = 120f
        drawScope.drawRect(
            color = Color(0xFF1F2937),
            topLeft = Offset(24f, 24f),
            size = Size(hpW, 14f)
        )
        drawScope.drawRect(
            color = if (health > 30) Color(0xFF10B981) else Color(0xFFEF4444),
            topLeft = Offset(24f, 24f),
            size = Size(hpW * (health / 100f), 14f)
        )
    }

    override fun onDpad(dx: Float, dy: Float) {
        moveDir = when {
            dx > 0.2f -> {
                isFacingRight = true
                1f
            }
            dx < -0.2f -> {
                isFacingRight = false
                -1f
            }
            else -> 0f
        }
        if (dy < -0.4f && isGrounded) {
            vy = jumpVelocity
            isGrounded = false
        }
    }

    override fun onButtonDown(button: GameButton, audio: RetroAudioSynthesizer) {
        when (button) {
            GameButton.A -> {
                // Jump
                if (isGrounded) {
                    vy = jumpVelocity
                    isGrounded = false
                    audio.playJump()
                }
            }
            GameButton.B -> {
                // Shoot
                if (shootCooldown <= 0f) {
                    shootCooldown = 0.18f
                    val spawnX = if (isFacingRight) playerX + 30f else playerX - 30f
                    val bVx = if (isFacingRight) 580f else -580f
                    bullets.add(
                        Bullet(
                            x = spawnX,
                            y = playerY - 30f,
                            vx = bVx,
                            vy = 0f,
                            isPlayer = true,
                            color = Color(0xFF38BDF8)
                        )
                    )
                    audio.playLaser()
                }
            }
            GameButton.X -> {
                // Grenade
                bullets.add(
                    Bullet(
                        x = playerX,
                        y = playerY - 30f,
                        vx = if (isFacingRight) 280f else -280f,
                        vy = -200f,
                        isPlayer = true,
                        color = Color(0xFFFBBF24)
                    )
                )
                audio.playExplosion()
            }
            GameButton.START -> reset()
            else -> {}
        }
    }

    override fun onButtonUp(button: GameButton) {}

    override fun getScore(): Int = score

    override fun isGameOver(): Boolean = gameOver
}
