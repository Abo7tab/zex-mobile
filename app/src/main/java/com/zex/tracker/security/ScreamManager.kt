package com.zex.tracker.security

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.zex.tracker.core.logging.ZexLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreamManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun startScream() {
        try {
            com.zex.tracker.utils.AudioPlayerHelper.playScream(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
            } else {
                vibrator.vibrate(longArrayOf(0, 1000, 1000), 0)
            }
            ZexLogger.i("ScreamManager", "Scream started")
        } catch (e: Exception) {
            ZexLogger.e("ScreamManager", "Failed to start scream", e)
        }
    }

    fun stopScream() {
        try {
            com.zex.tracker.utils.AudioPlayerHelper.stopScream()
            vibrator.cancel()
            ZexLogger.i("ScreamManager", "Scream stopped")
        } catch (e: Exception) {
            ZexLogger.e("ScreamManager", "Failed to stop scream", e)
        }
    }
}
