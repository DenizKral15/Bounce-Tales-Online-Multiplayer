package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object BounceAudioEngine {
    private const val SAMPLE_RATE = 22050
    private var isMuted = false
    private var isSfxMuted = false
    private var currentTrack: String? = null
    private var musicJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            stopMusic()
        } else {
            currentTrack?.let { playMusic(it) }
        }
        return isMuted
    }

    fun isMusicMuted(): Boolean = isMuted

    fun playMusic(trackName: String) {
        currentTrack = trackName
        if (isMuted) return

        musicJob?.cancel()
        musicJob = scope.launch {
            val melody = getMelodyForTrack(trackName)
            while (isActive) {
                for (note in melody) {
                    if (!isActive || isMuted) break
                    if (note.freq > 0f) {
                        playTone(note.freq, note.durationMs, isSquare = true, volume = 0.28f)
                    } else {
                        delay(note.durationMs.toLong())
                    }
                }
                delay(120)
            }
        }
    }

    fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun playSfx(sfxType: SfxType) {
        if (isSfxMuted) return
        scope.launch {
            when (sfxType) {
                SfxType.JUMP -> {
                    // Springy boing: pitch sweeps up
                    playSweep(280f, 620f, 130, volume = 0.45f)
                }
                SfxType.EGG_COLLECT -> {
                    // Cheerful arpeggio chime
                    playTone(659.25f, 60, volume = 0.4f)
                    delay(30)
                    playTone(880.00f, 90, volume = 0.45f)
                }
                SfxType.MACHINE_STOMP -> {
                    // Impact explosion + fanfares
                    playNoiseBurst(140)
                    delay(20)
                    playTone(523.25f, 90, volume = 0.5f)
                    delay(20)
                    playTone(783.99f, 180, volume = 0.5f)
                }
                SfxType.ROCK_BREAK -> {
                    // Heavy gravel crunch
                    playNoiseBurst(200)
                    playTone(110f, 140, volume = 0.6f)
                }
                SfxType.WATER_SPLASH -> {
                    // Bubbles
                    playSweep(420f, 260f, 120, volume = 0.35f)
                }
                SfxType.CANNON_FIRE -> {
                    // Deep boom
                    playTone(95f, 220, volume = 0.7f)
                    playNoiseBurst(160)
                }
                SfxType.CHECKPOINT -> {
                    // Ascending fanfare
                    playTone(440f, 70, volume = 0.4f)
                    delay(20)
                    playTone(554.37f, 70, volume = 0.4f)
                    delay(20)
                    playTone(659.25f, 140, volume = 0.45f)
                }
                SfxType.DEATH -> {
                    // Sad descending arpeggio
                    playTone(392.00f, 120, volume = 0.5f)
                    delay(30)
                    playTone(329.63f, 140, volume = 0.5f)
                    delay(30)
                    playTone(261.63f, 240, volume = 0.5f)
                }
                SfxType.VICTORY -> {
                    // Celebratory fanfare
                    playTone(523.25f, 100, volume = 0.5f)
                    delay(20)
                    playTone(659.25f, 100, volume = 0.5f)
                    delay(20)
                    playTone(783.99f, 120, volume = 0.5f)
                    delay(20)
                    playTone(1046.50f, 300, volume = 0.55f)
                }
                SfxType.TRANSFORM -> {
                    // Whoosh / morph
                    playSweep(300f, 750f, 110, volume = 0.4f)
                }
            }
        }
    }

    private fun playTone(freq: Float, durationMs: Int, isSquare: Boolean = true, volume: Float = 0.35f) {
        val count = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        if (count <= 0) return
        val samples = ShortArray(count)
        val period = SAMPLE_RATE / freq

        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toDouble()
            val sample = if (isSquare) {
                // Square wave with harmonic softening
                if ((i % period.toInt()) < period / 2) 1.0 else -1.0
            } else {
                sin(2.0 * PI * freq * t)
            }
            // Simple decay envelope
            val env = 1.0 - (i.toDouble() / count) * 0.4
            samples[i] = (sample * 32767.0 * volume * env).toInt().toShort()
        }

        writeAudio(samples)
    }

    private fun playSweep(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float = 0.35f) {
        val count = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        if (count <= 0) return
        val samples = ShortArray(count)

        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * currentFreq / SAMPLE_RATE
            val sample = sin(phase)
            val env = 1.0 - progress * 0.5
            samples[i] = (sample * 32767.0 * volume * env).toInt().toShort()
        }
        writeAudio(samples)
    }

    private fun playNoiseBurst(durationMs: Int, volume: Float = 0.45f) {
        val count = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        if (count <= 0) return
        val samples = ShortArray(count)
        var last = 0.0

        for (i in 0 until count) {
            val white = (Math.random() * 2.0 - 1.0)
            // Low-pass filtered noise
            last = last * 0.7 + white * 0.3
            val progress = i.toDouble() / count
            val env = (1.0 - progress) * (1.0 - progress)
            samples[i] = (last * 32767.0 * volume * env).toInt().toShort()
        }
        writeAudio(samples)
    }

    private fun writeAudio(samples: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            scope.launch {
                delay((samples.size * 1000L / SAMPLE_RATE) + 40L)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    private data class Note(val freq: Float, val durationMs: Int)

    private fun getMelodyForTrack(track: String): List<Note> {
        val C4 = 261.63f; val D4 = 293.66f; val E4 = 329.63f; val F4 = 349.23f
        val G4 = 392.00f; val A4 = 440.00f; val B4 = 493.88f
        val C5 = 523.25f; val D5 = 587.33f; val E5 = 659.25f; val F5 = 698.46f
        val G5 = 783.99f; val REST = 0f

        return when (track.lowercase()) {
            "aw" -> {
                // Menu Theme: Gentle, nostalgic, bouncy
                listOf(
                    Note(C4, 180), Note(E4, 180), Note(G4, 200), Note(C5, 300),
                    Note(B4, 180), Note(G4, 180), Note(A4, 200), Note(F4, 280),
                    Note(G4, 180), Note(E4, 180), Note(F4, 180), Note(D4, 260),
                    Note(C4, 380), Note(REST, 150)
                )
            }
            "ay" -> {
                // Sky Bean Land (Ch 1, 2, 3): Lighthearted, cheerful, upbeat bouncy rhythm
                listOf(
                    Note(G4, 140), Note(C5, 140), Note(E5, 140), Note(G5, 240),
                    Note(E5, 140), Note(C5, 220), Note(D5, 140), Note(E5, 140),
                    Note(F5, 140), Note(D5, 220), Note(B4, 140), Note(G4, 260),
                    Note(C5, 140), Note(E5, 140), Note(G5, 280), Note(REST, 120)
                )
            }
            "ax" -> {
                // Mushroom Mines (Ch 5, 6, 7): Mysterious, subterranean pulse, deep notes
                listOf(
                    Note(E4, 180), Note(G4, 180), Note(B4, 240), Note(A4, 180),
                    Note(G4, 180), Note(F4, 240), Note(E4, 300), Note(REST, 100),
                    Note(D4, 180), Note(F4, 180), Note(A4, 240), Note(G4, 180),
                    Note(E4, 340), Note(REST, 120)
                )
            }
            "az" -> {
                // Hypnoland (Ch 9, 10, 11): Circus carnival waltz, hypnotic and eccentric
                listOf(
                    Note(A4, 150), Note(C5, 150), Note(E5, 150), Note(D5, 220),
                    Note(B4, 150), Note(G4, 220), Note(A4, 150), Note(C5, 150),
                    Note(F5, 240), Note(E5, 220), Note(D5, 150), Note(C5, 260),
                    Note(REST, 120)
                )
            }
            "ba" -> {
                // Boss / Climax Action (Ch 4, 8, 12): Fast driving adrenaline rhythm
                listOf(
                    Note(D5, 110), Note(D5, 110), Note(F5, 130), Note(D5, 110),
                    Note(G5, 130), Note(F5, 110), Note(E5, 130), Note(C5, 200),
                    Note(D5, 110), Note(D5, 110), Note(A4, 130), Note(C5, 130),
                    Note(D5, 260), Note(REST, 80)
                )
            }
            "bd" -> {
                // Bonus Scoreland: Jubilant, celebratory, egg hunting fun
                listOf(
                    Note(C5, 120), Note(E5, 120), Note(G5, 120), Note(C5, 120),
                    Note(D5, 120), Note(F5, 120), Note(A4, 120), Note(B4, 200),
                    Note(C5, 280), Note(REST, 100)
                )
            }
            "bb" -> {
                listOf(Note(E4, 160), Note(D4, 160), Note(C4, 320))
            }
            "bc" -> {
                listOf(Note(G4, 120), Note(C5, 120), Note(E5, 120), Note(G5, 340))
            }
            else -> listOf(Note(C4, 200), Note(E4, 200), Note(G4, 200))
        }
    }
}

enum class SfxType {
    JUMP,
    EGG_COLLECT,
    MACHINE_STOMP,
    ROCK_BREAK,
    WATER_SPLASH,
    CANNON_FIRE,
    CHECKPOINT,
    DEATH,
    VICTORY,
    TRANSFORM
}
