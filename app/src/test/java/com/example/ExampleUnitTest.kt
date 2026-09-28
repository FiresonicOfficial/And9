package com.example

import com.example.data.local.entity.InstalledAppEntity
import com.example.engine.Arm32CompatibilityLayer
import com.example.engine.HardwareAccelerationEngine
import com.example.games.CustomApkGame
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testArm32CompatibilityInspection() {
    val armLayer = Arm32CompatibilityLayer()
    val inspection = armLayer.inspectApkPackage("super_mario_32.apk", "com.retro.mario32")
    assertEquals("armeabi-v7a (32-bit)", inspection.abiType)
    assertTrue(inspection.is32BitCompatible)
    assertEquals(28, inspection.targetSdk)
    assertTrue(inspection.dynamicLibraries.isNotEmpty())
    assertEquals(4096, armLayer.maxAddressableSpaceMb)
  }

  @Test
  fun testHardwareAccelerationFps() {
    val hwEngine = HardwareAccelerationEngine()
    assertTrue(hwEngine.isHwAccelerated)
    assertEquals(60, hwEngine.targetFps)
    hwEngine.onFrameStart(16_666_666L, 12)
    assertTrue(hwEngine.currentFps > 0)
    assertTrue(hwEngine.gpuLoadPercentage in 0..100)
  }

  @Test
  fun testCustomApkGameEngine() {
    val app = InstalledAppEntity(
        id = "test_custom_apk",
        name = "Retro 32-Bit Action Game",
        packageName = "com.retro.testaction",
        versionName = "1.0.0",
        architecture = "armeabi-v7a (32-bit)",
        isGame = true,
        iconType = "BRICK",
        fileSizeMb = 14.5f,
        summary = "Custom sideloaded 32-bit game"
    )
    val game = CustomApkGame(app)
    game.init(800f, 600f)
    assertEquals(0, game.getScore())
    assertFalse(game.isGameOver())
    game.onDpad(1f, 0f)
    assertEquals("test_custom_apk", game.id)
    assertEquals("Retro 32-Bit Action Game", game.title)
  }
}
