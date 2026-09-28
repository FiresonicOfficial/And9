package com.example.games

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.engine.audio.RetroAudioSynthesizer

enum class GameButton {
    A, B, X, Y, L1, R1, SELECT, START
}

interface VirtualGame {
    val id: String
    val title: String

    fun init(width: Float, height: Float)
    fun update(dt: Float, audio: RetroAudioSynthesizer)
    fun render(drawScope: DrawScope, width: Float, height: Float)
    fun onDpad(dx: Float, dy: Float)
    fun onButtonDown(button: GameButton, audio: RetroAudioSynthesizer)
    fun onButtonUp(button: GameButton)
    fun getScore(): Int
    fun isGameOver(): Boolean
    fun reset()
}
