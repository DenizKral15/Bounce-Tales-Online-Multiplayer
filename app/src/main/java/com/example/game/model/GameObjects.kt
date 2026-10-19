package com.example.game.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

data class TerrainSegment(
    val start: Offset,
    val end: Offset,
    val isBouncy: Boolean = false,
    val isSpike: Boolean = false
) {
    val length: Float get() = (end - start).getDistance()
    val normal: Offset get() {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val len = length
        return if (len > 0f) Offset(-dy / len, dx / len) else Offset(0f, -1f)
    }
}

data class GoldenEgg(
    val id: Int,
    val x: Float,
    val y: Float,
    var isCollected: Boolean = false,
    val value: Int = 100
)

data class ColorMachine(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float = 70f,
    val height: Float = 55f,
    var isDestroyed: Boolean = false,
    var smokeProgress: Float = 0f
)

data class BouncyFlower(
    val x: Float,
    val y: Float,
    val boostForce: Float = -19f,
    var springSquish: Float = 1.0f
)

data class BouncyMushroom(
    val x: Float,
    val y: Float,
    val capRadius: Float = 32f,
    val isPurple: Boolean = false,
    var bounceAnim: Float = 0f
)

data class BreakableRock(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float = 46f,
    val height: Float = 70f,
    var isBroken: Boolean = false
)

data class SpikeHazard(
    val x: Float,
    val y: Float,
    val width: Float = 40f,
    val height: Float = 24f,
    val isCeiling: Boolean = false
)

data class MovingPlatform(
    val id: Int,
    var currentX: Float,
    var currentY: Float,
    val width: Float = 80f,
    val height: Float = 18f,
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val speedX: Float = 0f,
    val speedY: Float = 0f,
    var dirX: Float = 1f,
    var dirY: Float = 1f
)

data class CannonLauncher(
    val x: Float,
    val y: Float,
    val angleDegrees: Float = -45f,
    val launchSpeed: Float = 22f,
    var cooldown: Float = 0f
)

data class WaterZone(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun contains(x: Float, y: Float): Boolean =
        x in left..right && y in top..bottom
}

data class LevelSwitch(
    val id: Int,
    val x: Float,
    val y: Float,
    var isActivated: Boolean = false
)

data class BarrierGate(
    val switchId: Int,
    val x: Float,
    val y: Float,
    val width: Float = 20f,
    val height: Float = 100f,
    var openProgress: Float = 0f
)

data class PumpkinGoal(
    val x: Float,
    val y: Float,
    val width: Float = 90f,
    val height: Float = 80f
)

data class CheckpointFlag(
    val id: Int,
    val x: Float,
    val y: Float,
    var isReached: Boolean = false
)

data class EnemyCreature(
    val id: Int,
    var x: Float,
    var y: Float,
    val minX: Float,
    val maxX: Float,
    val speed: Float = 2f,
    var dir: Float = 1f,
    val type: EnemyType = EnemyType.MUSHROOM_CREATURE,
    var isDefeated: Boolean = false
)

enum class EnemyType {
    MUSHROOM_CREATURE,
    HYPNO_DRONE,
    SPIDER_MINE
}

data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Long,
    val size: Float,
    var life: Float,
    val maxLife: Float
)

data class PlayerBallState(
    var id: Int = 1,
    var x: Float = 100f,
    var y: Float = 200f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var rotation: Float = 0f,
    var form: BallForm = BallForm.RED,
    var isGrounded: Boolean = false,
    var isInWater: Boolean = false,
    var squashX: Float = 1.0f,
    var squashY: Float = 1.0f,
    var isDead: Boolean = false,
    var eggsCollected: Int = 0,
    var isInsideCannon: Boolean = false,
    var cannonTimer: Float = 0f,
    var checkpointX: Float = 100f,
    var checkpointY: Float = 200f
)
