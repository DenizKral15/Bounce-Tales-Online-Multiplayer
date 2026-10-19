package com.example.game.model

import androidx.compose.ui.geometry.Offset

enum class WorldTheme(
    val worldName: String,
    val skyColorTop: Long,
    val skyColorBottom: Long,
    val groundColor: Long,
    val groundAccent: Long,
    val bgElementsColor: Long
) {
    SKY_BEAN_LAND(
        worldName = "Sky Bean Land",
        skyColorTop = 0xFF5C9CE6,
        skyColorBottom = 0xFF99CCFF,
        groundColor = 0xFF4CAF50,
        groundAccent = 0xFF2E7D32,
        bgElementsColor = 0xFF81C784
    ),
    MUSHROOM_MINES(
        worldName = "Mushroom Mines",
        skyColorTop = 0xFF19323C,
        skyColorBottom = 0xFF2E4B56,
        groundColor = 0xFF00796B,
        groundAccent = 0xFF004D40,
        bgElementsColor = 0xFF4DB6AC
    ),
    HYPNOLAND(
        worldName = "Hypnoland",
        skyColorTop = 0xFF4A148C,
        skyColorBottom = 0xFF880E4F,
        groundColor = 0xFF6A1B9A,
        groundAccent = 0xFF4A148C,
        bgElementsColor = 0xFFCE93D8
    )
}

data class LevelDefinition(
    val chapterNumber: Int,
    val title: String,
    val world: WorldTheme,
    val isBonus: Boolean,
    val unlockRock: Boolean = false,
    val unlockBeach: Boolean = false,
    val musicTrack: String, // aw, ay, ax, az, ba, bb, bc, bd
    val introDialogMole: String? = null,
    val levelWidth: Float = 3600f,
    val levelHeight: Float = 1400f,
    val startX: Float = 80f,
    val startY: Float = 320f,
    val segments: List<TerrainSegment>,
    val eggs: List<GoldenEgg>,
    val colorMachines: List<ColorMachine>,
    val flowers: List<BouncyFlower> = emptyList(),
    val mushrooms: List<BouncyMushroom> = emptyList(),
    val rocks: List<BreakableRock> = emptyList(),
    val spikes: List<SpikeHazard> = emptyList(),
    val movingPlatforms: List<MovingPlatform> = emptyList(),
    val cannons: List<CannonLauncher> = emptyList(),
    val waterZones: List<WaterZone> = emptyList(),
    val switches: List<LevelSwitch> = emptyList(),
    val gates: List<BarrierGate> = emptyList(),
    val checkpoints: List<CheckpointFlag> = emptyList(),
    val enemies: List<EnemyCreature> = emptyList(),
    val goal: PumpkinGoal
)

object LevelRepository {

    val allLevels: List<LevelDefinition> by lazy {
        listOf(
            createChapter1(),
            createChapter2(),
            createChapter3(),
            createChapter4(),
            createBonus1(),
            createChapter5(),
            createChapter6(),
            createChapter7(),
            createChapter8(),
            createBonus2(),
            createChapter9(),
            createChapter10(),
            createChapter11(),
            createChapter12(),
            createBonus3()
        )
    }

    // Helper to generate eggs along a path
    private fun createEggs(points: List<Pair<Float, Float>>): List<GoldenEgg> {
        return points.take(30).mapIndexed { idx, (x, y) ->
            GoldenEgg(id = idx + 1, x = x, y = y)
        }
    }

    private fun createChapter1(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        // Starting ground
        segments.add(TerrainSegment(Offset(0f, 380f), Offset(400f, 380f)))
        segments.add(TerrainSegment(Offset(400f, 380f), Offset(600f, 480f))) // slope down
        segments.add(TerrainSegment(Offset(600f, 480f), Offset(900f, 480f)))
        segments.add(TerrainSegment(Offset(900f, 480f), Offset(1150f, 380f))) // slope up
        segments.add(TerrainSegment(Offset(1150f, 380f), Offset(1500f, 380f)))
        // Upper platform
        segments.add(TerrainSegment(Offset(450f, 250f), Offset(700f, 250f)))
        // Gap and hill
        segments.add(TerrainSegment(Offset(1650f, 480f), Offset(2100f, 480f)))
        segments.add(TerrainSegment(Offset(2100f, 480f), Offset(2300f, 340f)))
        segments.add(TerrainSegment(Offset(2300f, 340f), Offset(2800f, 340f)))
        segments.add(TerrainSegment(Offset(2800f, 340f), Offset(3200f, 380f)))
        segments.add(TerrainSegment(Offset(3200f, 380f), Offset(3600f, 380f)))
        // Boundary walls
        segments.add(TerrainSegment(Offset(0f, 0f), Offset(0f, 600f)))
        segments.add(TerrainSegment(Offset(3600f, 0f), Offset(3600f, 600f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 6) eggPoints.add(Pair(200f + i * 35f, 350f))
        for (i in 0 until 5) eggPoints.add(Pair(470f + i * 40f, 220f))
        for (i in 0 until 5) eggPoints.add(Pair(700f + i * 35f, 450f))
        for (i in 0 until 5) eggPoints.add(Pair(1200f + i * 40f, 350f))
        for (i in 0 until 5) eggPoints.add(Pair(1750f + i * 40f, 450f))
        for (i in 0 until 4) eggPoints.add(Pair(2400f + i * 40f, 310f))

        return LevelDefinition(
            chapterNumber = 1,
            title = "Misty morning",
            world = WorldTheme.SKY_BEAN_LAND,
            isBonus = false,
            musicTrack = "ay",
            introDialogMole = "Bounce! Hypnotoid has set up color-draining machines! Stomp them to bring color back!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 1350f, y = 325f)
            ),
            flowers = listOf(
                BouncyFlower(x = 420f, y = 380f),
                BouncyFlower(x = 1580f, y = 480f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1180f, y = 380f)
            ),
            goal = PumpkinGoal(x = 3350f, y = 300f)
        )
    }

    private fun createChapter2(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 400f), Offset(500f, 400f)))
        segments.add(TerrainSegment(Offset(500f, 400f), Offset(750f, 520f)))
        segments.add(TerrainSegment(Offset(750f, 520f), Offset(1200f, 520f)))
        segments.add(TerrainSegment(Offset(1200f, 520f), Offset(1450f, 360f)))
        segments.add(TerrainSegment(Offset(1450f, 360f), Offset(1900f, 360f)))
        // Floating platforms over pond
        segments.add(TerrainSegment(Offset(800f, 380f), Offset(1050f, 380f)))
        segments.add(TerrainSegment(Offset(2050f, 420f), Offset(2700f, 420f)))
        segments.add(TerrainSegment(Offset(2700f, 420f), Offset(3500f, 380f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 8) eggPoints.add(Pair(150f + i * 40f, 365f))
        for (i in 0 until 6) eggPoints.add(Pair(820f + i * 40f, 350f))
        for (i in 0 until 6) eggPoints.add(Pair(850f + i * 40f, 485f))
        for (i in 0 until 5) eggPoints.add(Pair(1550f + i * 40f, 325f))
        for (i in 0 until 5) eggPoints.add(Pair(2200f + i * 40f, 385f))

        return LevelDefinition(
            chapterNumber = 2,
            title = "Unfriendly friends",
            world = WorldTheme.SKY_BEAN_LAND,
            isBonus = false,
            musicTrack = "ay",
            introDialogMole = "Be careful! Hypnotoid's minions are wandering around. Jump on their heads!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 1750f, y = 305f)
            ),
            flowers = listOf(
                BouncyFlower(x = 650f, y = 520f),
                BouncyFlower(x = 1950f, y = 420f)
            ),
            enemies = listOf(
                EnemyCreature(id = 1, x = 950f, y = 490f, minX = 800f, maxX = 1150f),
                EnemyCreature(id = 2, x = 2300f, y = 390f, minX = 2100f, maxX = 2500f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1480f, y = 360f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 300f)
        )
    }

    private fun createChapter3(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 350f), Offset(400f, 350f)))
        segments.add(TerrainSegment(Offset(400f, 350f), Offset(700f, 220f))) // high hill
        segments.add(TerrainSegment(Offset(700f, 220f), Offset(1000f, 220f)))
        segments.add(TerrainSegment(Offset(1000f, 220f), Offset(1300f, 480f))) // long slide
        segments.add(TerrainSegment(Offset(1300f, 480f), Offset(1800f, 480f)))
        // Multiple step pillars
        segments.add(TerrainSegment(Offset(1900f, 420f), Offset(2050f, 420f)))
        segments.add(TerrainSegment(Offset(2150f, 350f), Offset(2300f, 350f)))
        segments.add(TerrainSegment(Offset(2400f, 280f), Offset(2550f, 280f)))
        segments.add(TerrainSegment(Offset(2650f, 380f), Offset(3500f, 380f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 5) eggPoints.add(Pair(120f + i * 40f, 315f))
        for (i in 0 until 5) eggPoints.add(Pair(720f + i * 40f, 185f))
        for (i in 0 until 7) eggPoints.add(Pair(1050f + i * 35f, 260f + i * 28f))
        for (i in 0 until 5) eggPoints.add(Pair(1400f + i * 40f, 445f))
        for (i in 0 until 4) eggPoints.add(Pair(2180f + i * 35f, 315f))
        for (i in 0 until 4) eggPoints.add(Pair(2800f + i * 40f, 345f))

        return LevelDefinition(
            chapterNumber = 3,
            title = "Seeking answers",
            world = WorldTheme.SKY_BEAN_LAND,
            isBonus = false,
            musicTrack = "ay",
            introDialogMole = "A deep crack in the earth lies ahead! We need someone heavier to smash through it...",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 1600f, y = 425f)
            ),
            flowers = listOf(
                BouncyFlower(x = 1820f, y = 480f)
            ),
            spikes = listOf(
                SpikeHazard(x = 1500f, y = 456f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1350f, y = 480f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 300f)
        )
    }

    private fun createChapter4(): LevelDefinition {
        // Unlock Rock form Bumpy!
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 380f), Offset(600f, 380f)))
        segments.add(TerrainSegment(Offset(600f, 380f), Offset(800f, 500f)))
        // Underwater floor
        segments.add(TerrainSegment(Offset(800f, 500f), Offset(1800f, 500f)))
        segments.add(TerrainSegment(Offset(1800f, 500f), Offset(2000f, 380f)))
        segments.add(TerrainSegment(Offset(2000f, 380f), Offset(3400f, 380f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 6) eggPoints.add(Pair(150f + i * 40f, 345f))
        for (i in 0 until 12) eggPoints.add(Pair(850f + i * 70f, 465f)) // along seabed
        for (i in 0 until 6) eggPoints.add(Pair(2150f + i * 40f, 345f))
        for (i in 0 until 6) eggPoints.add(Pair(2700f + i * 40f, 345f))

        return LevelDefinition(
            chapterNumber = 4,
            title = "Bumpy cracks",
            world = WorldTheme.SKY_BEAN_LAND,
            isBonus = false,
            unlockRock = true,
            musicTrack = "ba",
            introDialogMole = "You discovered Bumpy the Rock Ball! Transform into Rock form to crush barriers and sink underwater!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 2500f, y = 325f)
            ),
            waterZones = listOf(
                WaterZone(left = 750f, top = 360f, right = 1850f, bottom = 520f)
            ),
            rocks = listOf(
                BreakableRock(id = 1, x = 1400f, y = 430f),
                BreakableRock(id = 2, x = 2300f, y = 310f)
            ),
            flowers = listOf(
                BouncyFlower(x = 1920f, y = 500f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 700f, y = 380f)
            ),
            goal = PumpkinGoal(x = 3150f, y = 300f)
        )
    }

    private fun createBonus1(): LevelDefinition {
        // Bonus Chapter 1: Secret Stalkway
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 500f), Offset(400f, 500f)))
        for (i in 0 until 7) {
            val px = 500f + i * 360f
            val py = 450f - (i % 3) * 90f
            segments.add(TerrainSegment(Offset(px, py), Offset(px + 180f, py)))
        }
        segments.add(TerrainSegment(Offset(3100f, 380f), Offset(3600f, 380f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 30) {
            eggPoints.add(Pair(250f + i * 105f, 250f + (i % 4) * 45f))
        }

        return LevelDefinition(
            chapterNumber = 0,
            title = "Secret stalkway",
            world = WorldTheme.SKY_BEAN_LAND,
            isBonus = true,
            musicTrack = "bd",
            introDialogMole = "Welcome to Scoreland! Collect every golden egg to achieve the Master Gold Trophy!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = emptyList(),
            flowers = listOf(
                BouncyFlower(x = 450f, y = 500f),
                BouncyFlower(x = 1600f, y = 360f),
                BouncyFlower(x = 2400f, y = 360f)
            ),
            goal = PumpkinGoal(x = 3350f, y = 300f)
        )
    }

    private fun createChapter5(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 400f), Offset(550f, 400f)))
        segments.add(TerrainSegment(Offset(550f, 400f), Offset(850f, 550f)))
        segments.add(TerrainSegment(Offset(850f, 550f), Offset(1500f, 550f)))
        segments.add(TerrainSegment(Offset(1500f, 550f), Offset(1800f, 420f)))
        segments.add(TerrainSegment(Offset(1800f, 420f), Offset(2400f, 420f)))
        segments.add(TerrainSegment(Offset(2400f, 420f), Offset(3500f, 380f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 8) eggPoints.add(Pair(120f + i * 40f, 365f))
        for (i in 0 until 8) eggPoints.add(Pair(900f + i * 65f, 515f))
        for (i in 0 until 8) eggPoints.add(Pair(1900f + i * 50f, 385f))
        for (i in 0 until 6) eggPoints.add(Pair(2600f + i * 50f, 345f))

        return LevelDefinition(
            chapterNumber = 5,
            title = "Into the mines",
            world = WorldTheme.MUSHROOM_MINES,
            isBonus = false,
            musicTrack = "ax",
            introDialogMole = "The Mushroom Mines are filled with glowing fungi! Spring on mushrooms to reach high tunnels.",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 2100f, y = 365f)
            ),
            mushrooms = listOf(
                BouncyMushroom(x = 750f, y = 550f),
                BouncyMushroom(x = 1650f, y = 550f, isPurple = true)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1450f, y = 550f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 300f)
        )
    }

    private fun createChapter6(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 420f), Offset(450f, 420f)))
        // Sinuous crystal cavern
        segments.add(TerrainSegment(Offset(450f, 420f), Offset(700f, 320f)))
        segments.add(TerrainSegment(Offset(700f, 320f), Offset(1100f, 320f)))
        segments.add(TerrainSegment(Offset(1100f, 320f), Offset(1400f, 520f)))
        segments.add(TerrainSegment(Offset(1400f, 520f), Offset(2000f, 520f)))
        segments.add(TerrainSegment(Offset(2000f, 520f), Offset(2300f, 380f)))
        segments.add(TerrainSegment(Offset(2300f, 380f), Offset(3500f, 380f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 6) eggPoints.add(Pair(120f + i * 40f, 385f))
        for (i in 0 until 8) eggPoints.add(Pair(720f + i * 45f, 285f))
        for (i in 0 until 8) eggPoints.add(Pair(1450f + i * 60f, 485f))
        for (i in 0 until 8) eggPoints.add(Pair(2450f + i * 45f, 345f))

        return LevelDefinition(
            chapterNumber = 6,
            title = "A gloomy path",
            world = WorldTheme.MUSHROOM_MINES,
            isBonus = false,
            musicTrack = "ax",
            introDialogMole = "Watch out for sharp crystal spikes on the cavern floors!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 1850f, y = 465f)
            ),
            mushrooms = listOf(
                BouncyMushroom(x = 550f, y = 420f),
                BouncyMushroom(x = 2150f, y = 520f, isPurple = true)
            ),
            spikes = listOf(
                SpikeHazard(x = 1200f, y = 496f),
                SpikeHazard(x = 1650f, y = 496f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1050f, y = 320f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 300f)
        )
    }

    private fun createChapter7(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 400f), Offset(600f, 400f)))
        segments.add(TerrainSegment(Offset(600f, 400f), Offset(800f, 540f)))
        segments.add(TerrainSegment(Offset(800f, 540f), Offset(1600f, 540f)))
        segments.add(TerrainSegment(Offset(1600f, 540f), Offset(2400f, 400f)))
        segments.add(TerrainSegment(Offset(2400f, 400f), Offset(3500f, 400f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 30) eggPoints.add(Pair(200f + i * 100f, 360f - (i % 4) * 30f))

        return LevelDefinition(
            chapterNumber = 7,
            title = "Rumbling sounds",
            world = WorldTheme.MUSHROOM_MINES,
            isBonus = false,
            musicTrack = "ax",
            introDialogMole = "The earth trembles! Moving elevators will carry you over the bottomless pits.",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 2000f, y = 345f)
            ),
            movingPlatforms = listOf(
                MovingPlatform(id = 1, currentX = 900f, currentY = 400f, minX = 850f, maxX = 1200f, minY = 400f, maxY = 400f, speedX = 2.5f),
                MovingPlatform(id = 2, currentX = 1350f, currentY = 480f, minX = 1350f, maxX = 1350f, minY = 320f, maxY = 480f, speedY = 2.5f)
            ),
            mushrooms = listOf(
                BouncyMushroom(x = 1750f, y = 540f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1550f, y = 540f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 320f)
        )
    }

    private fun createChapter8(): LevelDefinition {
        // Unlocks Beach Form Wolly!
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 420f), Offset(500f, 420f)))
        // Large subterranean lake
        segments.add(TerrainSegment(Offset(500f, 420f), Offset(800f, 600f)))
        segments.add(TerrainSegment(Offset(800f, 600f), Offset(2000f, 600f)))
        segments.add(TerrainSegment(Offset(2000f, 600f), Offset(2300f, 420f)))
        segments.add(TerrainSegment(Offset(2300f, 420f), Offset(3500f, 420f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 6) eggPoints.add(Pair(120f + i * 40f, 385f))
        for (i in 0 until 16) eggPoints.add(Pair(850f + i * 70f, 430f)) // floating on lake
        for (i in 0 until 8) eggPoints.add(Pair(2450f + i * 50f, 385f))

        return LevelDefinition(
            chapterNumber = 8,
            title = "Trapped in machine",
            world = WorldTheme.MUSHROOM_MINES,
            isBonus = false,
            unlockBeach = true,
            musicTrack = "ba",
            introDialogMole = "You found Wolly the Beach Ball! Super buoyant on water and floaty in the air!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 2600f, y = 365f)
            ),
            waterZones = listOf(
                WaterZone(left = 750f, top = 440f, right = 2050f, bottom = 620f)
            ),
            cannons = listOf(
                CannonLauncher(x = 450f, y = 380f, angleDegrees = -40f, launchSpeed = 24f)
            ),
            mushrooms = listOf(
                BouncyMushroom(x = 2100f, y = 600f, isPurple = true)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 700f, y = 420f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 340f)
        )
    }

    private fun createBonus2(): LevelDefinition {
        // Bonus Chapter 2: Tunnel of Treasures
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 500f), Offset(500f, 500f)))
        for (i in 0 until 8) {
            val px = 600f + i * 320f
            val py = 480f - (i % 2) * 120f
            segments.add(TerrainSegment(Offset(px, py), Offset(px + 140f, py)))
        }
        segments.add(TerrainSegment(Offset(3200f, 450f), Offset(3600f, 450f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 30) eggPoints.add(Pair(250f + i * 100f, 320f + (i % 3) * 40f))

        return LevelDefinition(
            chapterNumber = 0,
            title = "Tunnel of treasures",
            world = WorldTheme.MUSHROOM_MINES,
            isBonus = true,
            musicTrack = "bd",
            introDialogMole = "Gold everywhere in the mines! Grab all 30 eggs for the medal!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = emptyList(),
            mushrooms = listOf(
                BouncyMushroom(x = 550f, y = 500f),
                BouncyMushroom(x = 1450f, y = 480f),
                BouncyMushroom(x = 2250f, y = 480f)
            ),
            goal = PumpkinGoal(x = 3400f, y = 370f)
        )
    }

    private fun createChapter9(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 420f), Offset(500f, 420f)))
        segments.add(TerrainSegment(Offset(500f, 420f), Offset(800f, 280f))) // steep ramp
        segments.add(TerrainSegment(Offset(800f, 280f), Offset(1300f, 280f)))
        segments.add(TerrainSegment(Offset(1300f, 280f), Offset(1700f, 480f)))
        segments.add(TerrainSegment(Offset(1700f, 480f), Offset(2400f, 480f)))
        segments.add(TerrainSegment(Offset(2400f, 480f), Offset(3500f, 400f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 8) eggPoints.add(Pair(120f + i * 40f, 385f))
        for (i in 0 until 8) eggPoints.add(Pair(850f + i * 50f, 245f))
        for (i in 0 until 8) eggPoints.add(Pair(1800f + i * 55f, 445f))
        for (i in 0 until 6) eggPoints.add(Pair(2600f + i * 50f, 365f))

        return LevelDefinition(
            chapterNumber = 9,
            title = "Wicked circus",
            world = WorldTheme.HYPNOLAND,
            isBonus = false,
            musicTrack = "az",
            introDialogMole = "We reached Hypnoland! Hypnotoid's wicked circus is warping everything with hypnotic machinery!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 2050f, y = 425f)
            ),
            cannons = listOf(
                CannonLauncher(x = 1600f, y = 480f, angleDegrees = -55f, launchSpeed = 25f)
            ),
            flowers = listOf(
                BouncyFlower(x = 450f, y = 420f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1250f, y = 280f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 320f)
        )
    }

    private fun createChapter10(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 420f), Offset(600f, 420f)))
        segments.add(TerrainSegment(Offset(600f, 420f), Offset(900f, 540f)))
        segments.add(TerrainSegment(Offset(900f, 540f), Offset(1700f, 540f)))
        segments.add(TerrainSegment(Offset(1700f, 540f), Offset(2100f, 360f)))
        segments.add(TerrainSegment(Offset(2100f, 360f), Offset(3500f, 360f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 30) eggPoints.add(Pair(180f + i * 105f, 380f - (i % 5) * 25f))

        return LevelDefinition(
            chapterNumber = 10,
            title = "Hunting colours",
            world = WorldTheme.HYPNOLAND,
            isBonus = false,
            musicTrack = "az",
            introDialogMole = "Two color machines are draining this zone! Destroy both to restore the carnival lights!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 1200f, y = 485f),
                ColorMachine(id = 2, x = 2500f, y = 305f)
            ),
            rocks = listOf(
                BreakableRock(id = 1, x = 1500f, y = 470f)
            ),
            movingPlatforms = listOf(
                MovingPlatform(id = 1, currentX = 1800f, currentY = 440f, minX = 1750f, maxX = 2050f, minY = 440f, maxY = 440f, speedX = 3f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1650f, y = 540f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 280f)
        )
    }

    private fun createChapter11(): LevelDefinition {
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 400f), Offset(500f, 400f)))
        segments.add(TerrainSegment(Offset(500f, 400f), Offset(800f, 250f)))
        segments.add(TerrainSegment(Offset(800f, 250f), Offset(1200f, 250f)))
        segments.add(TerrainSegment(Offset(1200f, 250f), Offset(1500f, 480f)))
        segments.add(TerrainSegment(Offset(1500f, 480f), Offset(2200f, 480f)))
        segments.add(TerrainSegment(Offset(2200f, 480f), Offset(2600f, 360f)))
        segments.add(TerrainSegment(Offset(2600f, 360f), Offset(3500f, 360f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 30) eggPoints.add(Pair(150f + i * 105f, 330f + (i % 4) * 35f))

        return LevelDefinition(
            chapterNumber = 11,
            title = "Almost there",
            world = WorldTheme.HYPNOLAND,
            isBonus = false,
            musicTrack = "az",
            introDialogMole = "Hypnotoid's fortress is right ahead! Braidel is calling for help from the top tower!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 1800f, y = 425f)
            ),
            cannons = listOf(
                CannonLauncher(x = 1350f, y = 420f, angleDegrees = -45f, launchSpeed = 26f)
            ),
            flowers = listOf(
                BouncyFlower(x = 750f, y = 300f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 1150f, y = 250f)
            ),
            goal = PumpkinGoal(x = 3250f, y = 280f)
        )
    }

    private fun createChapter12(): LevelDefinition {
        // Boss showdown with Hypnotoid
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 450f), Offset(700f, 450f)))
        segments.add(TerrainSegment(Offset(700f, 450f), Offset(900f, 560f)))
        segments.add(TerrainSegment(Offset(900f, 560f), Offset(2200f, 560f)))
        segments.add(TerrainSegment(Offset(2200f, 560f), Offset(2500f, 400f)))
        segments.add(TerrainSegment(Offset(2500f, 400f), Offset(3600f, 400f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 30) eggPoints.add(Pair(200f + i * 100f, 380f - (i % 4) * 40f))

        return LevelDefinition(
            chapterNumber = 12,
            title = "Final ride",
            world = WorldTheme.HYPNOLAND,
            isBonus = false,
            musicTrack = "ba",
            introDialogMole = "This is it! Smash Hypnotoid's mega machine and free Braidel to restore all color forever!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = listOf(
                ColorMachine(id = 1, x = 1400f, y = 505f),
                ColorMachine(id = 2, x = 1850f, y = 505f)
            ),
            enemies = listOf(
                EnemyCreature(id = 1, x = 1200f, y = 530f, minX = 1000f, maxX = 1350f, type = EnemyType.HYPNO_DRONE),
                EnemyCreature(id = 2, x = 1650f, y = 530f, minX = 1500f, maxX = 1800f, type = EnemyType.HYPNO_DRONE)
            ),
            cannons = listOf(
                CannonLauncher(x = 650f, y = 450f, angleDegrees = -40f, launchSpeed = 25f),
                CannonLauncher(x = 2100f, y = 560f, angleDegrees = -60f, launchSpeed = 27f)
            ),
            flowers = listOf(
                BouncyFlower(x = 1000f, y = 560f)
            ),
            checkpoints = listOf(
                CheckpointFlag(id = 1, x = 850f, y = 450f)
            ),
            goal = PumpkinGoal(x = 3300f, y = 320f)
        )
    }

    private fun createBonus3(): LevelDefinition {
        // Bonus Chapter 3: Fantastic Fair
        val segments = mutableListOf<TerrainSegment>()
        segments.add(TerrainSegment(Offset(0f, 450f), Offset(450f, 450f)))
        for (i in 0 until 8) {
            val px = 550f + i * 350f
            val py = 420f - (i % 3) * 90f
            segments.add(TerrainSegment(Offset(px, py), Offset(px + 160f, py)))
        }
        segments.add(TerrainSegment(Offset(3200f, 400f), Offset(3600f, 400f)))

        val eggPoints = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 30) eggPoints.add(Pair(250f + i * 105f, 260f + (i % 4) * 35f))

        return LevelDefinition(
            chapterNumber = 0,
            title = "Fantastic fair",
            world = WorldTheme.HYPNOLAND,
            isBonus = true,
            musicTrack = "bd",
            introDialogMole = "The Fantastic Fair celebration! The ultimate Scoreland challenge for true Bounce masters!",
            segments = segments,
            eggs = createEggs(eggPoints),
            colorMachines = emptyList(),
            cannons = listOf(
                CannonLauncher(x = 420f, y = 450f, angleDegrees = -45f, launchSpeed = 25f),
                CannonLauncher(x = 2100f, y = 330f, angleDegrees = -45f, launchSpeed = 25f)
            ),
            flowers = listOf(
                BouncyFlower(x = 1350f, y = 330f)
            ),
            goal = PumpkinGoal(x = 3350f, y = 320f)
        )
    }
}
