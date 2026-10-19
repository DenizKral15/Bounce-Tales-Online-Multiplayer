package com.example.game.model

enum class BallForm(
    val displayName: String,
    val colorHex: Long,
    val outlineHex: Long,
    val mass: Float,
    val radius: Float,
    val maxSpeed: Float,
    val jumpForce: Float,
    val bounceFactor: Float,
    val waterBuoyancy: Float, // Negative = sinks, Positive = floats
    val canBreakRocks: Boolean,
    val description: String
) {
    RED(
        displayName = "Bounce",
        colorHex = 0xFFE53935,
        outlineHex = 0xFFB71C1C,
        mass = 1.0f,
        radius = 20f,
        maxSpeed = 8.5f,
        jumpForce = -13.5f,
        bounceFactor = 0.72f,
        waterBuoyancy = 0.15f, // Floats comfortably
        canBreakRocks = false,
        description = "Balanced, agile & springy. The hero of Sky Bean Land!"
    ),
    ROCK(
        displayName = "Bumpy",
        colorHex = 0xFF78909C,
        outlineHex = 0xFF37474F,
        mass = 2.8f,
        radius = 22f,
        maxSpeed = 6.0f,
        jumpForce = -10.5f,
        bounceFactor = 0.25f,
        waterBuoyancy = -0.55f, // Sinks rapidly to walk underwater
        canBreakRocks = true,
        description = "Heavy stone body. Sinks in water and crushes cracked blocks!"
    ),
    BEACH(
        displayName = "Wolly",
        colorHex = 0xFFFFEB3B,
        outlineHex = 0xFFF57F17,
        mass = 0.45f,
        radius = 24f,
        maxSpeed = 10.0f,
        jumpForce = -16.0f,
        bounceFactor = 0.90f,
        waterBuoyancy = 0.95f, // Highly buoyant, leaps out of water
        canBreakRocks = false,
        description = "Ultra light inflatable ball. Super floaty and glides high!"
    )
}
