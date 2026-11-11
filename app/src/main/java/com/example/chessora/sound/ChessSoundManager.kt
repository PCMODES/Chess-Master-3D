package com.example.chessora.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class ChessSoundManager(private val context: Context) {

  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  var isSoundEnabled: Boolean = true
  var isMusicEnabled: Boolean = true
  var isHapticsEnabled: Boolean = true
  var masterVolume: Float = 0.85f

  private var ambientMusicJob: Job? = null

  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun playMoveSound() {
    if (isSoundEnabled) {
      playTone(frequency = 520.0, durationMs = 65, decay = 35.0, volume = 0.85f * masterVolume)
    }
    vibrate(28)
  }

  fun playCaptureSound() {
    if (isSoundEnabled) {
      // Deeper resonant wood thud
      playTone(frequency = 260.0, durationMs = 120, decay = 18.0, volume = 1.0f * masterVolume)
    }
    vibrate(55)
  }

  fun playPieceSelectSound() {
    if (isSoundEnabled) {
      playTone(frequency = 880.0, durationMs = 20, decay = 50.0, volume = 0.35f * masterVolume)
    }
    vibrate(12)
  }

  fun playCheckSound() {
    if (isSoundEnabled) {
      coroutineScope.launch {
        playTone(frequency = 784.0, durationMs = 100, decay = 15.0, volume = 0.9f * masterVolume) // G5
        delay(70)
        playTone(frequency = 1046.5, durationMs = 150, decay = 12.0, volume = 1.0f * masterVolume) // C6
      }
    }
    vibratePattern(longArrayOf(0, 40, 50, 60))
  }

  fun playVictorySound() {
    if (isSoundEnabled) {
      coroutineScope.launch {
        val notes = listOf(523.25, 659.25, 783.99, 1046.5) // C-E-G-C Fanfare
        for (note in notes) {
          playTone(frequency = note, durationMs = 140, decay = 10.0, volume = 0.9f * masterVolume)
          delay(120)
        }
      }
    }
    vibratePattern(longArrayOf(0, 80, 80, 150))
  }

  fun playDefeatSound() {
    if (isSoundEnabled) {
      coroutineScope.launch {
        val notes = listOf(440.0, 392.0, 349.23, 293.66)
        for (note in notes) {
          playTone(frequency = note, durationMs = 180, decay = 8.0, volume = 0.8f * masterVolume)
          delay(160)
        }
      }
    }
  }

  fun playClickSound() {
    if (isSoundEnabled) {
      playTone(frequency = 800.0, durationMs = 25, decay = 60.0, volume = 0.45f * masterVolume)
    }
    vibrate(15)
  }

  fun playScanSuccessSound() {
    if (isSoundEnabled) {
      coroutineScope.launch {
        playTone(frequency = 1174.66, durationMs = 60, decay = 25.0, volume = 0.8f * masterVolume) // D6
        delay(70)
        playTone(frequency = 1760.0, durationMs = 120, decay = 20.0, volume = 0.9f * masterVolume) // A6
      }
    }
    vibratePattern(longArrayOf(0, 30, 40, 50))
  }

  fun startAmbientMusic() {
    if (!isMusicEnabled || ambientMusicJob?.isActive == true) return
    ambientMusicJob = coroutineScope.launch {
      // Gentle relaxing ambient chords for chess focus
      val chords = listOf(
        doubleArrayOf(146.83, 220.00, 261.63), // Dm7 (D3, A3, C4)
        doubleArrayOf(174.61, 261.63, 329.63), // Fmaj7 (F3, C4, E4)
        doubleArrayOf(130.81, 196.00, 246.94), // Cmaj7 (C3, G3, B3)
        doubleArrayOf(164.81, 246.94, 293.66)  // Em7 (E3, B3, D4)
      )
      var index = 0
      while (isActive && isMusicEnabled) {
        val chord = chords[index % chords.size]
        playChordPad(chord, durationMs = 4500, volume = 0.10f * masterVolume)
        delay(4800)
        index++
      }
    }
  }

  fun stopAmbientMusic() {
    ambientMusicJob?.cancel()
    ambientMusicJob = null
  }

  private fun vibrate(durationMs: Long) {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(durationMs)
      }
    } catch (_: Exception) {}
  }

  private fun vibratePattern(pattern: LongArray) {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(pattern, -1)
      }
    } catch (_: Exception) {}
  }

  private fun playTone(frequency: Double, durationMs: Int, decay: Double, volume: Float) {
    coroutineScope.launch {
      try {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return@launch

        val buffer = ShortArray(numSamples)
        val maxAmp = (Short.MAX_VALUE * volume.coerceIn(0f, 1f)).toInt()

        for (i in 0 until numSamples) {
          val time = i.toDouble() / sampleRate
          val envelope = exp(-decay * time)
          val sample = (sin(2.0 * PI * frequency * time) * envelope * maxAmp).toInt()
          buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

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

        delay(durationMs.toLong() + 40)
        track.stop()
        track.release()
      } catch (_: Exception) {}
    }
  }

  private fun playChordPad(frequencies: DoubleArray, durationMs: Int, volume: Float) {
    coroutineScope.launch {
      try {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return@launch

        val buffer = ShortArray(numSamples)
        val maxAmp = (Short.MAX_VALUE * volume.coerceIn(0f, 1f) / frequencies.size.toFloat()).toInt()

        for (i in 0 until numSamples) {
          val time = i.toDouble() / sampleRate
          // Smooth fade in and fade out envelope for ambient pad
          val progress = i.toDouble() / numSamples
          val envelope = sin(PI * progress) // bell curve shape: 0 -> 1 -> 0

          var combinedSample = 0.0
          for (freq in frequencies) {
            combinedSample += sin(2.0 * PI * freq * time)
          }

          val sample = (combinedSample * envelope * maxAmp).toInt()
          buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val track = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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

        delay(durationMs.toLong() + 50)
        track.stop()
        track.release()
      } catch (_: Exception) {}
    }
  }
}
