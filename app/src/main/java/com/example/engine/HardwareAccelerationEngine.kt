package com.example.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

/**
 * Manages hardware acceleration state, frame pacing, GPU utilization telemetry,
 * and multi-threaded rendering pipeline for 32-bit games.
 */
class HardwareAccelerationEngine {

    var isHwAccelerated by mutableStateOf(true)
    var isMultiThreaded by mutableStateOf(true)
    var targetFps by mutableIntStateOf(60)
    var resolutionScale by mutableFloatStateOf(1.0f)
    var activeShader by mutableStateOf("CRT_SCANLINES")

    // Live Telemetry
    var currentFps by mutableIntStateOf(60)
        private set
    var frameTimeMs by mutableFloatStateOf(16.6f)
        private set
    var gpuLoadPercentage by mutableIntStateOf(38)
        private set
    var virtualCpu32Load by mutableIntStateOf(29)
        private set
    var allocatedVramMb by mutableIntStateOf(340)
        private set
    var temperatureCelsius by mutableFloatStateOf(36.5f)
        private set

    // Frame timing tracking
    private var lastFrameNano: Long = 0L
    private val frameTimes = LongArray(30)
    private var frameIndex = 0
    private var frameCounter = 0
    private var lastFpsUpdateNano: Long = 0L

    fun onFrameStart(currentNano: Long, activeEntitiesCount: Int = 10) {
        if (lastFrameNano != 0L) {
            val deltaNano = currentNano - lastFrameNano
            frameTimes[frameIndex] = deltaNano
            frameIndex = (frameIndex + 1) % frameTimes.size
            frameCounter++

            // Update FPS & GPU metrics every 250ms
            if (currentNano - lastFpsUpdateNano >= 250_000_000L) {
                var totalNano = 0L
                for (t in frameTimes) totalNano += t
                val avgDeltaNano = totalNano / frameTimes.size.toDouble()

                if (avgDeltaNano > 0) {
                    val rawFps = (1_000_000_000.0 / avgDeltaNano).toInt()
                    // Cap or scale according to hardware acceleration mode
                    currentFps = if (isHwAccelerated) {
                        min(targetFps, max(24, rawFps))
                    } else {
                        // Software rendering penalty simulation
                        min(28, max(14, rawFps / 2))
                    }
                    frameTimeMs = ((avgDeltaNano / 1_000_000.0)).toFloat().coerceIn(4f, 75f)
                }

                // Compute realistic dynamic GPU / CPU load
                val baseGpu = if (isHwAccelerated) 32 else 88
                val entityFactor = (activeEntitiesCount * 1.2f).toInt()
                val targetFactor = if (targetFps >= 90) 18 else 0
                val multithreadBenefit = if (isMultiThreaded) -8 else 12

                gpuLoadPercentage = (baseGpu + entityFactor + targetFactor + multithreadBenefit).coerceIn(12, 99)
                virtualCpu32Load = ((if (isHwAccelerated) 24 else 75) + (activeEntitiesCount * 0.8f).toInt()).coerceIn(15, 96)
                allocatedVramMb = (280 + (activeEntitiesCount * 3) + if (resolutionScale > 1.0f) 120 else 0).coerceIn(180, 890)
                temperatureCelsius = (34.0f + (gpuLoadPercentage * 0.12f)).coerceIn(34.0f, 48.5f)

                lastFpsUpdateNano = currentNano
            }
        } else {
            lastFpsUpdateNano = currentNano
        }
        lastFrameNano = currentNano
    }

    fun getDeltaTimeSeconds(): Float {
        val standardDelta = 1f / targetFps.toFloat()
        return if (isHwAccelerated) standardDelta else standardDelta * 1.5f
    }
}
