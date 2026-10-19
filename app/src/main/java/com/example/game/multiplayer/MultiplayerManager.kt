package com.example.game.multiplayer

import com.example.game.model.BallForm
import com.example.game.model.PlayerBallState
import kotlin.math.abs

enum class MultiplayerMode(val title: String, val description: String) {
    SOLO("Single Player", "Classic storybook Bounce adventure through all 15 chapters"),
    CO_OP_LOCAL("Local 2-Player Co-op", "Play together on the same screen! P1 is Bounce, P2 is Bumpy or Wolly"),
    CO_OP_AI("AI Companion Partner", "Play with an intelligent partner who helps you smash rocks and collect eggs"),
    RACE_DUEL("Race & Egg Duel", "Head-to-head speedrun to the Pumpkin House for the highest egg score!"),
    ROOM_ONLINE("4-Digit Room Code", "Host or join a multiplayer room lobby with friends")
}

data class MultiplayerSession(
    val mode: MultiplayerMode = MultiplayerMode.SOLO,
    var roomCode: String = "2008",
    var player2Form: BallForm = BallForm.ROCK,
    var p1Score: Int = 0,
    var p2Score: Int = 0,
    var p1Finished: Boolean = false,
    var p2Finished: Boolean = false,
    var winnerText: String? = null
)

object MultiplayerManager {

    fun initPlayer2(p1: PlayerBallState, form: BallForm): PlayerBallState {
        return PlayerBallState(
            id = 2,
            x = p1.x - 45f,
            y = p1.y,
            form = form,
            checkpointX = p1.checkpointX - 45f,
            checkpointY = p1.checkpointY
        )
    }

    // AI Companion update routine ("Tails" logic)
    fun updateCompanionAI(
        ai: PlayerBallState,
        leader: PlayerBallState,
        nearestBreakableRockX: Float?,
        jumpObstacleAhead: Boolean
    ): Pair<Float, Boolean> {
        val dx = leader.x - ai.x
        var moveInput = 0f
        var jumpPressed = false

        // If rock nearby and AI can break rocks, go crush it
        if (nearestBreakableRockX != null && ai.form.canBreakRocks && abs(nearestBreakableRockX - ai.x) < 200f) {
            moveInput = if (nearestBreakableRockX > ai.x) 1f else -1f
            if (ai.isGrounded && abs(ai.vx) < 1.5f) {
                jumpPressed = true
            }
        } else {
            // Follow leader
            if (abs(dx) > 60f) {
                moveInput = if (dx > 0) 1f else -1f
            }

            // Jump if stuck or leader is higher
            if (ai.isGrounded) {
                if (leader.y < ai.y - 45f && abs(dx) < 150f) {
                    jumpPressed = true
                } else if (jumpObstacleAhead || (abs(ai.vx) < 0.2f && abs(dx) > 80f)) {
                    jumpPressed = true
                }
            }
        }

        // Catch up teleport if fell too far behind
        if (abs(dx) > 1200f || abs(leader.y - ai.y) > 700f) {
            ai.x = leader.x - 50f
            ai.y = leader.y
            ai.vx = 0f
            ai.vy = 0f
        }

        return Pair(moveInput, jumpPressed)
    }
}
