package com.example.engine.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

/**
 * Real-time synthesis of retro 32-bit audio effects using native PCM AudioTrack.
 */
class RetroAudioSynthesizer {

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()
    var isEnabled: Boolean = true

    fun playLaser() {
        if (!isEnabled) return
        scope.launch {
            val durationMs = 120
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            var currentFreq = 950.0
            val endFreq = 220.0
            var phase = 0.0

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                currentFreq = 950.0 * (1.0 - t) + endFreq * t
                val delta = 2.0 * PI * currentFreq / sampleRate
                phase += delta
                val sample = (sin(phase) * 0.7 * (1.0 - t) * Short.MAX_VALUE).toInt().toShort()
                buffer[i] = sample
            }
            writeAndPlay(buffer)
        }
    }

    fun playExplosion() {
        if (!isEnabled) return
        scope.launch {
            val durationMs = 280
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            var filter = 0.0

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val rawNoise = (random.nextDouble() * 2.0 - 1.0)
                // Low-pass filter for thunderous explosion
                filter = filter * 0.85 + rawNoise * 0.15
                val amp = (1.0 - t) * (1.0 - t) * 0.8
                buffer[i] = (filter * amp * Short.MAX_VALUE).toInt().toShort()
            }
            writeAndPlay(buffer)
        }
    }

    fun playCoin() {
        if (!isEnabled) return
        scope.launch {
            val durationMs = 150
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            val half = numSamples / 2
            var phase = 0.0

            for (i in 0 until numSamples) {
                val freq = if (i < half) 987.77 else 1318.51 // B5 then E6
                phase += 2.0 * PI * freq / sampleRate
                val t = (i % half).toDouble() / half
                val sample = (sin(phase) * 0.6 * (1.0 - t * 0.5) * Short.MAX_VALUE).toInt().toShort()
                buffer[i] = sample
            }
            writeAndPlay(buffer)
        }
    }

    fun playJump() {
        if (!isEnabled) return
        scope.launch {
            val durationMs = 130
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            var phase = 0.0

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val freq = 200.0 + 500.0 * (t * t)
                phase += 2.0 * PI * freq / sampleRate
                buffer[i] = (sin(phase) * 0.6 * (1.0 - t) * Short.MAX_VALUE).toInt().toShort()
            }
            writeAndPlay(buffer)
        }
    }

    fun playNitro() {
        if (!isEnabled) return
        scope.launch {
            val durationMs = 320
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            var phase = 0.0

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val noise = (random.nextDouble() * 2.0 - 1.0) * 0.4
                val tone = sin(phase) * 0.3
                phase += 2.0 * PI * 180.0 / sampleRate
                val sample = ((noise + tone) * (1.0 - t * 0.4) * Short.MAX_VALUE).toInt().toShort()
                buffer[i] = sample
            }
            writeAndPlay(buffer)
        }
    }

    fun playClick() {
        if (!isEnabled) return
        scope.launch {
            val durationMs = 30
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                phase += 2.0 * PI * 1400.0 / sampleRate
                buffer[i] = (sin(phase) * 0.5 * (1.0 - t) * Short.MAX_VALUE).toInt().toShort()
            }
            writeAndPlay(buffer)
        }
    }

    fun playHit() {
        if (!isEnabled) return
        scope.launch {
            val durationMs = 80
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                phase += 2.0 * PI * (120.0 * (1.0 - t)) / sampleRate
                buffer[i] = (sin(phase) * 0.7 * (1.0 - t) * Short.MAX_VALUE).toInt().toShort()
            }
            writeAndPlay(buffer)
        }
    }

    private fun writeAndPlay(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            // Release when playback completes
            scope.launch {
                val delayMs = (buffer.size * 1000L / sampleRate) + 50L
                kotlinx.coroutines.delay(delayMs)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
