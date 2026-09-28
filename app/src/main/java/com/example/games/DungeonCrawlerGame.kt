package com.example.games

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.engine.audio.RetroAudioSynthesizer
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class DungeonCrawlerGame : VirtualGame {

    override val id: String = "game.dungeon32"
    override val title: String = "Dungeon Crawler 32"

    private var playerX = 400f
    private var playerY = 400f
    private var moveX = 0f
    private var moveY = 0f
    private var facingAngle = 0f
    private var health = 100
    private var mana = 100
    private var keys = 0
    private var score = 0
    private var dungeonFloor = 1
    private var gameOver = false

    private var swordSlashTimer = 0f
    private var slashAngle = 0f

    // Fireball projectiles
    data class Spell(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float)
    private val spells = mutableListOf<Spell>()

    // Dungeon Monsters
    data class Monster(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var health: Int,
        val type: Int // 0 = Skeleton, 1 = Bat
    )
    private val monsters = mutableListOf<Monster>()

    // Treasure chests & gems
    data class Chest(var x: Float, var y: Float, var isOpened: Boolean)
    private val chests = mutableListOf<Chest>()

    // Particles
    data class RpgParticle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val color: Color)
    private val particles = mutableListOf<RpgParticle>()
    private val random = Random()

    override fun init(width: Float, height: Float) {
        reset()
    }

    override fun reset() {
        playerX = 400f
        playerY = 400f
        moveX = 0f
        moveY = 0f
        facingAngle = 0f
        health = 100
        mana = 100
        keys = 0
        score = 0
        dungeonFloor = 1
        gameOver = false
        swordSlashTimer = 0f
        spells.clear()
        monsters.clear()
        chests.clear()
        particles.clear()

        // Populate initial monsters & chests
        monsters.add(Monster(220f, 200f, 40f, 0f, 3, 0))
        monsters.add(Monster(580f, 220f, -40f, 0f, 3, 0))
        monsters.add(Monster(300f, 550f, 50f, 50f, 2, 1))
        monsters.add(Monster(500f, 520f, -50f, 50f, 2, 1))

        chests.add(Chest(180f, 160f, false))
        chests.add(Chest(620f, 160f, false))
        chests.add(Chest(400f, 620f, false))
    }

    override fun update(dt: Float, audio: RetroAudioSynthesizer) {
        if (gameOver) return

        swordSlashTimer -= dt
        if (mana < 100) mana = (mana + 15 * dt).toInt().coerceAtMost(100)

        // Player move
        if (moveX != 0f || moveY != 0f) {
            playerX = (playerX + moveX * 220f * dt).coerceIn(80f, 720f)
            playerY = (playerY + moveY * 220f * dt).coerceIn(120f, 660f)
            facingAngle = kotlin.math.atan2(moveY.toDouble(), moveX.toDouble()).toFloat()
        }

        // Update spells
        val sIter = spells.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.x += s.vx * dt
            s.y += s.vy * dt
            s.life -= dt

            var hit = false
            for (m in monsters) {
                if (abs(s.x - m.x) < 30f && abs(s.y - m.y) < 30f) {
                    m.health -= 2
                    hit = true
                    audio.playHit()
                    break
                }
            }
            if (hit || s.life <= 0f) {
                sIter.remove()
                for (p in 0..6) {
                    particles.add(
                        RpgParticle(
                            s.x, s.y,
                            (random.nextFloat() * 2f - 1f) * 90f,
                            (random.nextFloat() * 2f - 1f) * 90f,
                            0.3f,
                            Color(0xFFF97316)
                        )
                    )
                }
            }
        }

        // Update monsters
        val mIter = monsters.iterator()
        while (mIter.hasNext()) {
            val m = mIter.next()
            // Move towards player
            val dx = playerX - m.x
            val dy = playerY - m.y
            val dist = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()

            if (dist > 15f) {
                val speed = if (m.type == 0) 65f else 95f
                m.x += (dx / dist) * speed * dt
                m.y += (dy / dist) * speed * dt
            }

            // Attack player on contact
            if (dist < 26f) {
                health -= (18 * dt).toInt()
                if (health <= 0) {
                    health = 0
                    gameOver = true
                    audio.playExplosion()
                }
            }

            if (m.health <= 0) {
                score += if (m.type == 0) 250 else 150
                audio.playCoin()
                mIter.remove()
            }
        }

        // Check chests
        for (c in chests) {
            if (!c.isOpened && abs(playerX - c.x) < 38f && abs(playerY - c.y) < 38f) {
                c.isOpened = true
                score += 500
                keys++
                audio.playCoin()
            }
        }

        // If all chests opened & monsters cleared, advance floor
        if (monsters.isEmpty() && chests.all { it.isOpened }) {
            dungeonFloor++
            health = (health + 40).coerceAtMost(100)
            score += 1000
            // Respawn new floor
            monsters.add(Monster(220f, 200f, 40f, 0f, 4, 0))
            monsters.add(Monster(580f, 220f, -40f, 0f, 4, 0))
            monsters.add(Monster(400f, 200f, 40f, 0f, 5, 0))
            for (c in chests) c.isOpened = false
        }

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
        // Dungeon Stone Floor
        drawScope.drawRect(
            color = Color(0xFF1E1E24),
            size = Size(width, height)
        )

        // Cobblestone Grid Tiles
        val tileW = width / 10f
        val tileH = height / 10f
        for (tx in 0..9) {
            for (ty in 0..9) {
                drawScope.drawRect(
                    color = Color(0xFF2A2A32),
                    topLeft = Offset(tx * tileW + 2f, ty * tileH + 2f),
                    size = Size(tileW - 4f, tileH - 4f)
                )
            }
        }

        // Dungeon Walls Border
        drawScope.drawRect(
            color = Color(0xFF3F3F46),
            topLeft = Offset(0f, 0f),
            size = Size(width, 40f)
        )
        drawScope.drawRect(
            color = Color(0xFF3F3F46),
            topLeft = Offset(0f, height - 40f),
            size = Size(width, 40f)
        )

        // Chests
        for (c in chests) {
            val cx = (c.x / 800f) * width
            val cy = (c.y / 800f) * height
            drawScope.drawRoundRect(
                color = if (c.isOpened) Color(0xFF94A3B8) else Color(0xFFF59E0B),
                topLeft = Offset(cx - 16f, cy - 14f),
                size = Size(32f, 28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
        }

        // Monsters
        for (m in monsters) {
            val mx = (m.x / 800f) * width
            val my = (m.y / 800f) * height

            if (m.type == 0) {
                // Skeleton Warrior
                drawScope.drawCircle(
                    color = Color(0xFFE2E8F0),
                    radius = 16f,
                    center = Offset(mx, my)
                )
                // Red glowing eye sockets
                drawScope.drawCircle(color = Color(0xFFEF4444), radius = 3f, center = Offset(mx - 4f, my - 2f))
                drawScope.drawCircle(color = Color(0xFFEF4444), radius = 3f, center = Offset(mx + 4f, my - 2f))
            } else {
                // Dungeon Bat
                drawScope.drawCircle(
                    color = Color(0xFF78716C),
                    radius = 12f,
                    center = Offset(mx, my)
                )
            }
        }

        // Spells (Fireballs)
        for (s in spells) {
            val sx = (s.x / 800f) * width
            val sy = (s.y / 800f) * height
            drawScope.drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFFFDE047), Color(0xFFEA580C))),
                radius = 12f,
                center = Offset(sx, sy)
            )
        }

        // Player Hero Knight
        val px = (playerX / 800f) * width
        val py = (playerY / 800f) * height

        // Knight Body
        drawScope.drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF38BDF8), Color(0xFF0369A1))),
            radius = 18f,
            center = Offset(px, py)
        )
        // Shield
        val shieldX = px + (cos(facingAngle + 1.2) * 20.0).toFloat()
        val shieldY = py + (sin(facingAngle + 1.2) * 20.0).toFloat()
        drawScope.drawCircle(color = Color(0xFF0284C7), radius = 8f, center = Offset(shieldX, shieldY))

        // Sword Slash Arc
        if (swordSlashTimer > 0f) {
            val swordTipX = px + (cos(slashAngle.toDouble()) * 45.0).toFloat()
            val swordTipY = py + (sin(slashAngle.toDouble()) * 45.0).toFloat()
            drawScope.drawLine(
                color = Color(0xFFF8FAFC),
                start = Offset(px, py),
                end = Offset(swordTipX, swordTipY),
                strokeWidth = 6f
            )
        }

        // Particles
        for (p in particles) {
            val pxPos = (p.x / 800f) * width
            val pyPos = (p.y / 800f) * height
            drawScope.drawCircle(color = p.color, radius = 4f, center = Offset(pxPos, pyPos))
        }

        // HUD: HP & Mana
        drawScope.drawRoundRect(
            color = Color(0xDD0B0F19),
            topLeft = Offset(16f, 16f),
            size = Size(width - 32f, 44f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        // Health bar
        drawScope.drawRect(
            color = Color(0xFFEF4444),
            topLeft = Offset(24f, 24f),
            size = Size(80f * (health / 100f), 10f)
        )
        // Mana bar
        drawScope.drawRect(
            color = Color(0xFF3B82F6),
            topLeft = Offset(24f, 40f),
            size = Size(80f * (mana / 100f), 10f)
        )
    }

    override fun onDpad(dx: Float, dy: Float) {
        moveX = dx
        moveY = dy
    }

    override fun onButtonDown(button: GameButton, audio: RetroAudioSynthesizer) {
        when (button) {
            GameButton.A -> {
                // Sword slash
                swordSlashTimer = 0.2f
                slashAngle = facingAngle
                audio.playHit()
                // Damage monsters in front
                for (m in monsters) {
                    val dx = m.x - playerX
                    val dy = m.y - playerY
                    val dist = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    if (dist < 70f) {
                        m.health -= 2
                        audio.playHit()
                    }
                }
            }
            GameButton.B -> {
                // Fireball spell
                if (mana >= 25) {
                    mana -= 25
                    val spVx = (cos(facingAngle.toDouble()) * 480.0).toFloat()
                    val spVy = (sin(facingAngle.toDouble()) * 480.0).toFloat()
                    spells.add(Spell(playerX, playerY, spVx, spVy, 1.2f))
                    audio.playLaser()
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
