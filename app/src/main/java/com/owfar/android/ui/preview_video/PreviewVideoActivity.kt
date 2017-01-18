package com.owfar.android.ui.preview_video

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.MediaController
import android.widget.VideoView

import com.owfar.android.R


class PreviewVideoActivity : AppCompatActivity() {

    companion object {
        private val TAG = PreviewVideoActivity::class.java.simpleName
        val EXTRA_CONTENT_PATH = TAG + ".EXTRA_CONTENT_PATH"
        private val STATE_PLAYING = TAG + ".STATE_PLAYING"
    }

    private var vvVideo: VideoView? = null
    private var playing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_video)

        vvVideo = findViewById(R.id.activity_preview_video_vvVideo) as VideoView

        playing = savedInstanceState?.getBoolean(STATE_PLAYING) ?: true

        vvVideo?.apply {
            setVideoPath(intent.getStringExtra(EXTRA_CONTENT_PATH))
            val controller = MediaController(this@PreviewVideoActivity)
            controller.setAnchorView(this)
            setMediaController(controller)
        }
    }

    override fun onResume() {
        super.onResume()
        if (playing) vvVideo?.start()
    }

    override fun onPause() {
        vvVideo?.apply {
            playing = isPlaying
            pause()
        }
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean(STATE_PLAYING, playing)
    }
}
