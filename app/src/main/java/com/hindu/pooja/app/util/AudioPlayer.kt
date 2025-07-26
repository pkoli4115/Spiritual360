package com.hindu.pooja.util

import android.content.Context
import android.media.MediaPlayer

object AudioPlayer {
    private var backgroundPlayer: MediaPlayer? = null
    private var soundEffectPlayer: MediaPlayer? = null

    fun playBackground(context: Context, resId: Int) {
        stopBackground()
        backgroundPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true
            start()
        }
    }

    fun stopBackground() {
        backgroundPlayer?.stop()
        backgroundPlayer?.release()
        backgroundPlayer = null
    }

    fun playSoundEffect(context: Context, resId: Int) {
        soundEffectPlayer?.release()
        soundEffectPlayer = MediaPlayer.create(context, resId).apply {
            setOnCompletionListener {
                it.release()
            }
            start()
        }
    }
}
