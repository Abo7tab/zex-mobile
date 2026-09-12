package com.zex.tracker.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.zex.tracker.R
import com.zex.tracker.core.logging.ZexLogger

object AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun playScream(context: Context) {
        if (mediaPlayer?.isPlaying == true) {
            ZexLogger.i("AudioPlayerHelper", "Scream already playing. Ignoring start request.")
            return
        }
        stopScream() // Ensure clean state
        
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
            
            val uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            ZexLogger.i("AudioPlayerHelper", "Scream started successfully")
        } catch (e: Exception) {
            ZexLogger.e("AudioPlayerHelper", "Failed to start scream audio", e)
        }
    }

    fun stopScream() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            ZexLogger.e("AudioPlayerHelper", "Error stopping scream", e)
        } finally {
            mediaPlayer = null
        }
    }
}

