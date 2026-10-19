package com.example

import com.example.game.model.BallForm
import com.example.game.model.LevelRepository
import com.example.game.multiplayer.MultiplayerManager
import com.example.game.multiplayer.MultiplayerMode
import com.example.game.multiplayer.MultiplayerSession
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAll15LevelsLoaded() {
        val levels = LevelRepository.allLevels
        assertEquals("Should have all 15 original chapters", 15, levels.size)

        // Verify key chapters
        val ch1 = levels.find { it.chapterNumber == 1 }
        assertNotNull(ch1)
        assertEquals("Misty morning", ch1?.title)
        assertEquals(30, ch1?.eggs?.size)

        val ch4 = levels.find { it.chapterNumber == 4 }
        assertNotNull(ch4)
        assertTrue("Chapter 4 should unlock Rock form Bumpy", ch4?.unlockRock == true)

        val ch8 = levels.find { it.chapterNumber == 8 }
        assertNotNull(ch8)
        assertTrue("Chapter 8 should unlock Beach form Wolly", ch8?.unlockBeach == true)

        val ch12 = levels.find { it.chapterNumber == 12 }
        assertNotNull(ch12)
        assertEquals("Final ride", ch12?.title)
    }

    @Test
    fun testBallFormsPhysics() {
        // Red form
        assertEquals("Bounce", BallForm.RED.displayName)
        assertFalse(BallForm.RED.canBreakRocks)

        // Rock form
        assertEquals("Bumpy", BallForm.ROCK.displayName)
        assertTrue(BallForm.ROCK.canBreakRocks)
        assertTrue("Rock form should sink in water", BallForm.ROCK.waterBuoyancy < 0f)

        // Beach form
        assertEquals("Wolly", BallForm.BEACH.displayName)
        assertTrue("Beach form should float high in water", BallForm.BEACH.waterBuoyancy > 0.5f)
    }

    @Test
    fun testMultiplayerSession() {
        val session = MultiplayerSession(mode = MultiplayerMode.CO_OP_LOCAL)
        assertEquals(MultiplayerMode.CO_OP_LOCAL, session.mode)
        assertEquals(BallForm.ROCK, session.player2Form)
    }
}
