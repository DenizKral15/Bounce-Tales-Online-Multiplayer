package com.example.game.physics

import androidx.compose.ui.geometry.Offset
import com.example.game.audio.BounceAudioEngine
import com.example.game.audio.SfxType
import com.example.game.model.*
import kotlin.math.*

class GamePhysicsEngine(
    val level: LevelDefinition,
    val onColorMachineDestroyed: (ColorMachine) -> Unit,
    val onEggCollected: (GoldenEgg) -> Unit,
    val onLevelCompleted: () -> Unit,
    val onPlayerDied: () -> Unit
) {
    var particles = mutableListOf<GameParticle>()
    var levelCleared = false

    fun update(
        player: PlayerBallState,
        moveInput: Float, // -1f to +1f
        jumpPressed: Boolean,
        dt: Float = 1f
    ) {
        if (player.isDead || levelCleared) return

        // Cannon handling
        if (player.isInsideCannon) {
            player.cannonTimer -= dt
            player.vx = 0f
            player.vy = 0f
            if (player.cannonTimer <= 0f) {
                player.isInsideCannon = false
                val cannon = level.cannons.find { (Offset(it.x, it.y) - Offset(player.x, player.y)).getDistance() < 60f }
                if (cannon != null) {
                    val rad = cannon.angleDegrees * PI / 180.0
                    player.vx = (cos(rad) * cannon.launchSpeed).toFloat()
                    player.vy = (sin(rad) * cannon.launchSpeed).toFloat()
                    BounceAudioEngine.playSfx(SfxType.CANNON_FIRE)
                    spawnSmoke(player.x, player.y)
                }
            }
            return
        }

        val form = player.form
        val radius = form.radius

        // Check water status
        val inWater = level.waterZones.any { it.contains(player.x, player.y) }
        player.isInWater = inWater

        // Gravity & Buoyancy
        val baseGravity = 0.48f * form.mass
        if (inWater) {
            val buoyancy = form.waterBuoyancy * 0.75f
            player.vy += (baseGravity - buoyancy)
            player.vx *= 0.92f // water drag
            player.vy *= 0.90f
            // Water surface exit boost for Beach ball
            if (form == BallForm.BEACH && player.vy < -3f) {
                player.vy -= 2.0f
            }
        } else {
            player.vy += baseGravity
            if (player.vy > 18f) player.vy = 18f // Terminal velocity
        }

        // Horizontal movement & acceleration
        val accel = if (player.isGrounded) 0.65f else 0.42f
        val friction = if (player.isGrounded) 0.88f else 0.96f

        if (moveInput != 0f) {
            player.vx += moveInput * accel
            val maxSpd = form.maxSpeed
            player.vx = player.vx.coerceIn(-maxSpd, maxSpd)
        } else {
            player.vx *= friction
            if (abs(player.vx) < 0.05f) player.vx = 0f
        }

        // Jump handling
        if (jumpPressed && (player.isGrounded || (inWater && form == BallForm.BEACH))) {
            player.vy = form.jumpForce
            player.isGrounded = false
            player.squashX = 0.82f
            player.squashY = 1.25f
            BounceAudioEngine.playSfx(SfxType.JUMP)
        }

        // Apply velocities
        player.x += player.vx
        player.y += player.vy

        // Ball rolling rotation
        player.rotation += (player.vx / radius) * (180f / PI.toFloat())

        // Squash & Stretch restoration
        player.squashX += (1.0f - player.squashX) * 0.15f
        player.squashY += (1.0f - player.squashY) * 0.15f

        // Collisions
        player.isGrounded = false
        handleTerrainCollisions(player)
        handleMovingPlatforms(player)
        handleBreakableRocks(player)
        handleBouncyObjects(player)
        handleColorMachines(player)
        handleSpikes(player)
        handleCannons(player)
        handleCheckpoints(player)
        handleEggs(player)
        handleGoal(player)

        // Fall out of level bounds check
        if (player.y > level.levelHeight - 40f) {
            killPlayer(player)
        }
    }

    private fun handleTerrainCollisions(player: PlayerBallState) {
        val radius = player.form.radius
        for (seg in level.segments) {
            val distInfo = getDistanceToSegment(Offset(player.x, player.y), seg.start, seg.end)
            if (distInfo.distance < radius) {
                val penetration = radius - distInfo.distance
                val normal = distInfo.normal

                player.x += normal.x * penetration
                player.y += normal.y * penetration

                // Dot product velocity with normal
                val dot = player.vx * normal.x + player.vy * normal.y
                if (dot < 0f) {
                    val restitution = player.form.bounceFactor
                    player.vx -= (1f + restitution) * dot * normal.x
                    player.vy -= (1f + restitution) * dot * normal.y

                    // Landed on floor
                    if (normal.y < -0.4f) {
                        player.isGrounded = true
                        // Squash on hard impact
                        if (dot < -5f) {
                            val factor = min(1.35f, 1.0f + abs(dot) * 0.035f)
                            player.squashX = factor
                            player.squashY = 1.0f / factor
                        }
                    }
                }
            }
        }
    }

    private fun handleMovingPlatforms(player: PlayerBallState) {
        val radius = player.form.radius
        for (plat in level.movingPlatforms) {
            // Update platform position
            plat.currentX += plat.speedX * plat.dirX
            if (plat.currentX >= plat.maxX) plat.dirX = -1f
            if (plat.currentX <= plat.minX) plat.dirX = 1f

            plat.currentY += plat.speedY * plat.dirY
            if (plat.currentY >= plat.maxY) plat.dirY = -1f
            if (plat.currentY <= plat.minY) plat.dirY = 1f

            // Check collision with platform top
            if (player.x in (plat.currentX - radius)..(plat.currentX + plat.width + radius)) {
                if (player.y + radius >= plat.currentY && player.y - radius <= plat.currentY + plat.height) {
                    if (player.vy >= 0f && player.y <= plat.currentY + 12f) {
                        player.y = plat.currentY - radius
                        player.vy = 0f
                        player.x += plat.speedX * plat.dirX
                        player.isGrounded = true
                    }
                }
            }
        }
    }

    private fun handleBreakableRocks(player: PlayerBallState) {
        val radius = player.form.radius
        for (rock in level.rocks) {
            if (rock.isBroken) continue
            val rectLeft = rock.x
            val rectRight = rock.x + rock.width
            val rectTop = rock.y
            val rectBottom = rock.y + rock.height

            val nearestX = player.x.coerceIn(rectLeft, rectRight)
            val nearestY = player.y.coerceIn(rectTop, rectBottom)
            val distSq = (player.x - nearestX) * (player.x - nearestX) + (player.y - nearestY) * (player.y - nearestY)

            if (distSq < radius * radius) {
                // Rock form Bumpy breaks rock!
                if (player.form.canBreakRocks && (abs(player.vx) > 2.0f || abs(player.vy) > 3.0f)) {
                    rock.isBroken = true
                    BounceAudioEngine.playSfx(SfxType.ROCK_BREAK)
                    spawnRockDebris(rock.x + rock.width / 2f, rock.y + rock.height / 2f)
                } else {
                    // Solid barrier
                    val dx = player.x - (rectLeft + rock.width / 2f)
                    if (abs(dx) > rock.width / 4f) {
                        player.x = if (dx > 0) rectRight + radius else rectLeft - radius
                        player.vx = -player.vx * 0.4f
                    } else {
                        player.y = rectTop - radius
                        player.vy = 0f
                        player.isGrounded = true
                    }
                }
            }
        }
    }

    private fun handleBouncyObjects(player: PlayerBallState) {
        val radius = player.form.radius

        // Flowers
        for (flower in level.flowers) {
            val dist = hypot(player.x - flower.x, player.y - flower.y)
            if (dist < radius + 28f && player.vy > -1f) {
                player.vy = flower.boostForce
                player.squashX = 0.7f
                player.squashY = 1.4f
                flower.springSquish = 0.4f
                BounceAudioEngine.playSfx(SfxType.JUMP)
                spawnPetalSparkles(flower.x, flower.y)
            }
            flower.springSquish += (1.0f - flower.springSquish) * 0.12f
        }

        // Mushrooms
        for (shroom in level.mushrooms) {
            val dist = hypot(player.x - shroom.x, player.y - shroom.y)
            if (dist < radius + shroom.capRadius && player.vy > -1f) {
                val boost = if (shroom.isPurple) -21f else -17f
                player.vy = boost
                player.squashX = 0.72f
                player.squashY = 1.35f
                shroom.bounceAnim = 1.0f
                BounceAudioEngine.playSfx(SfxType.JUMP)
                spawnSporeParticles(shroom.x, shroom.y, shroom.isPurple)
            }
            shroom.bounceAnim += (0f - shroom.bounceAnim) * 0.1f
        }
    }

    private fun handleColorMachines(player: PlayerBallState) {
        val radius = player.form.radius
        for (machine in level.colorMachines) {
            if (machine.isDestroyed) continue
            val mLeft = machine.x
            val mRight = machine.x + machine.width
            val mTop = machine.y
            val mBottom = machine.y + machine.height

            if (player.x in (mLeft - radius)..(mRight + radius)) {
                // Landing on top stomps machine
                if (player.y + radius >= mTop && player.y - radius <= mTop + 18f && player.vy > 0f) {
                    machine.isDestroyed = true
                    player.vy = -12f
                    BounceAudioEngine.playSfx(SfxType.MACHINE_STOMP)
                    spawnColorBurst(machine.x + machine.width / 2f, machine.y)
                    onColorMachineDestroyed(machine)
                } else if (player.y in mTop..mBottom) {
                    // Side collision
                    if (player.x < mLeft + machine.width / 2f) {
                        player.x = mLeft - radius
                        player.vx = -abs(player.vx) * 0.5f
                    } else {
                        player.x = mRight + radius
                        player.vx = abs(player.vx) * 0.5f
                    }
                }
            }
        }
    }

    private fun handleSpikes(player: PlayerBallState) {
        val radius = player.form.radius - 4f
        for (spike in level.spikes) {
            if (player.x in (spike.x - radius)..(spike.x + spike.width + radius)) {
                if (player.y in (spike.y - radius)..(spike.y + spike.height + radius)) {
                    killPlayer(player)
                    return
                }
            }
        }
    }

    private fun handleCannons(player: PlayerBallState) {
        for (cannon in level.cannons) {
            val dist = hypot(player.x - cannon.x, player.y - cannon.y)
            if (dist < 32f && !player.isInsideCannon) {
                player.isInsideCannon = true
                player.cannonTimer = 25f // ~0.4s delay
                player.x = cannon.x
                player.y = cannon.y
            }
        }
    }

    private fun handleCheckpoints(player: PlayerBallState) {
        for (cp in level.checkpoints) {
            if (!cp.isReached && hypot(player.x - cp.x, player.y - cp.y) < 45f) {
                cp.isReached = true
                player.checkpointX = cp.x
                player.checkpointY = cp.y - 20f
                BounceAudioEngine.playSfx(SfxType.CHECKPOINT)
                spawnSparkles(cp.x, cp.y - 20f, 0xFFFFD700)
            }
        }
    }

    private fun handleEggs(player: PlayerBallState) {
        for (egg in level.eggs) {
            if (!egg.isCollected) {
                val dist = hypot(player.x - egg.x, player.y - egg.y)
                if (dist < player.form.radius + 18f) {
                    egg.isCollected = true
                    player.eggsCollected++
                    BounceAudioEngine.playSfx(SfxType.EGG_COLLECT)
                    spawnSparkles(egg.x, egg.y, 0xFFFFE082)
                    onEggCollected(egg)
                }
            }
        }
    }

    private fun handleGoal(player: PlayerBallState) {
        val goal = level.goal
        if (hypot(player.x - (goal.x + goal.width / 2f), player.y - (goal.y + goal.height / 2f)) < 55f) {
            levelCleared = true
            BounceAudioEngine.playSfx(SfxType.VICTORY)
            spawnColorBurst(player.x, player.y)
            onLevelCompleted()
        }
    }

    private fun killPlayer(player: PlayerBallState) {
        player.isDead = true
        BounceAudioEngine.playSfx(SfxType.DEATH)
        spawnPopEffect(player.x, player.y, player.form.colorHex)
        onPlayerDied()
    }

    fun respawnPlayer(player: PlayerBallState) {
        player.isDead = false
        player.x = player.checkpointX
        player.y = player.checkpointY
        player.vx = 0f
        player.vy = 0f
        player.isGrounded = false
        player.squashX = 1f
        player.squashY = 1f
    }

    // Particle Emitters
    fun updateParticles() {
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.15f
            p.life -= 1f
            if (p.life <= 0f) {
                it.remove()
            }
        }
    }

    private fun spawnSparkles(x: Float, y: Float, color: Long) {
        for (i in 0 until 10) {
            val ang = Math.random() * 2.0 * PI
            val spd = (Math.random() * 3.5 + 1.0).toFloat()
            particles.add(
                GameParticle(
                    x = x, y = y,
                    vx = (cos(ang) * spd).toFloat(),
                    vy = (sin(ang) * spd).toFloat(),
                    color = color,
                    size = (Math.random() * 5.0 + 3.0).toFloat(),
                    life = 25f, maxLife = 25f
                )
            )
        }
    }

    private fun spawnPopEffect(x: Float, y: Float, color: Long) {
        for (i in 0 until 18) {
            val ang = Math.random() * 2.0 * PI
            val spd = (Math.random() * 5.0 + 2.0).toFloat()
            particles.add(
                GameParticle(
                    x = x, y = y,
                    vx = (cos(ang) * spd).toFloat(),
                    vy = (sin(ang) * spd).toFloat(),
                    color = color,
                    size = (Math.random() * 7.0 + 4.0).toFloat(),
                    life = 35f, maxLife = 35f
                )
            )
        }
    }

    private fun spawnRockDebris(x: Float, y: Float) {
        val colors = listOf(0xFF78909CL, 0xFF455A64L, 0xFF90A4AEL)
        for (i in 0 until 16) {
            val ang = Math.random() * 2.0 * PI
            val spd = (Math.random() * 4.5 + 1.5).toFloat()
            particles.add(
                GameParticle(
                    x = x, y = y,
                    vx = (cos(ang) * spd).toFloat(),
                    vy = (sin(ang) * spd).toFloat() - 2.5f,
                    color = colors.random(),
                    size = (Math.random() * 8.0 + 4.0).toFloat(),
                    life = 40f, maxLife = 40f
                )
            )
        }
    }

    private fun spawnSmoke(x: Float, y: Float) {
        for (i in 0 until 8) {
            val ang = Math.random() * 2.0 * PI
            val spd = (Math.random() * 2.5 + 0.8).toFloat()
            particles.add(
                GameParticle(
                    x = x, y = y,
                    vx = (cos(ang) * spd).toFloat(),
                    vy = (sin(ang) * spd).toFloat() - 1.5f,
                    color = 0xFFCCCCCCL,
                    size = (Math.random() * 9.0 + 5.0).toFloat(),
                    life = 28f, maxLife = 28f
                )
            )
        }
    }

    private fun spawnColorBurst(x: Float, y: Float) {
        val rainbow = listOf(
            0xFFFF1744L, 0xFFFF9100L, 0xFFFFEA00L, 0xFF00E676L, 0xFF00E5FFL, 0xFFD500F9L
        )
        for (i in 0 until 35) {
            val ang = Math.random() * 2.0 * PI
            val spd = (Math.random() * 6.5 + 2.0).toFloat()
            particles.add(
                GameParticle(
                    x = x, y = y,
                    vx = (cos(ang) * spd).toFloat(),
                    vy = (sin(ang) * spd).toFloat() - 2.0f,
                    color = rainbow.random(),
                    size = (Math.random() * 7.0 + 4.0).toFloat(),
                    life = 55f, maxLife = 55f
                )
            )
        }
    }

    private fun spawnPetalSparkles(x: Float, y: Float) {
        val colors = listOf(0xFFFF4081L, 0xFFFF80ABL, 0xFFFFD54FL)
        for (i in 0 until 12) {
            val ang = Math.random() * 2.0 * PI
            val spd = (Math.random() * 3.5 + 1.0).toFloat()
            particles.add(
                GameParticle(
                    x = x, y = y,
                    vx = (cos(ang) * spd).toFloat(),
                    vy = (sin(ang) * spd).toFloat() - 2.0f,
                    color = colors.random(),
                    size = 5f, life = 30f, maxLife = 30f
                )
            )
        }
    }

    private fun spawnSporeParticles(x: Float, y: Float, isPurple: Boolean) {
        val baseColor = if (isPurple) 0xFFAB47BCL else 0xFFFFCA28L
        for (i in 0 until 14) {
            val ang = Math.random() * 2.0 * PI
            val spd = (Math.random() * 3.0 + 1.0).toFloat()
            particles.add(
                GameParticle(
                    x = x, y = y,
                    vx = (cos(ang) * spd).toFloat(),
                    vy = (sin(ang) * spd).toFloat() - 1.5f,
                    color = baseColor,
                    size = 4f, life = 32f, maxLife = 32f
                )
            )
        }
    }

    private data class SegmentDist(val distance: Float, val normal: Offset)

    private fun getDistanceToSegment(p: Offset, a: Offset, b: Offset): SegmentDist {
        val ab = b - a
        val ap = p - a
        val abLenSq = ab.x * ab.x + ab.y * ab.y
        if (abLenSq == 0f) {
            val dist = (p - a).getDistance()
            val norm = if (dist > 0f) (p - a) / dist else Offset(0f, -1f)
            return SegmentDist(dist, norm)
        }

        val t = ((ap.x * ab.x + ap.y * ab.y) / abLenSq).coerceIn(0f, 1f)
        val closest = a + Offset(ab.x * t, ab.y * t)
        val diff = p - closest
        val dist = diff.getDistance()
        val norm = if (dist > 0.001f) diff / dist else Offset(0f, -1f)
        return SegmentDist(dist, norm)
    }
}
