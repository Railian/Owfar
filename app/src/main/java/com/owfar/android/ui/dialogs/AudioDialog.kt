package com.owfar.android.ui.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.extensions.orNullIfBlank
import java.io.FileInputStream
import java.io.IOException

class AudioDialog : DialogFragment(), DialogInterface.OnClickListener, SeekBar.OnSeekBarChangeListener {

    companion object {

        val TAG: String? = AudioDialog::class.java.simpleName

        private val ARG_AUDIO_FILE_PATH = TAG + ".ARG_AUDIO_FILE_PATH"
        val EXTRA_AUDIO_FILE_PATH = TAG + ".EXTRA_AUDIO_FILE_PATH"
        val EXTRA_HEADER = TAG + ".EXTRA_HEADER"
        val EXTRA_CONTENT = TAG + ".EXTRA_CONTENT"

        fun newInstance(targetFragment: Fragment, requestCode: Int, audioFilePath: String) = AudioDialog().apply {
            setTargetFragment(targetFragment, requestCode)
            arguments = Bundle().apply { putString(ARG_AUDIO_FILE_PATH, audioFilePath) }
        }
    }

    //region widgets
    private var etHeader: EditText? = null
    private var ivButton: ImageView? = null
    private var tvDescription: TextView? = null
    private var tvDuration: TextView? = null
    private var sbProgress: SeekBar? = null
    private var etContent: EditText? = null
    //endregion

    //region fields
    private var mediaPlayer: MediaPlayer? = null

    private val handler = Handler()
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            sbProgress?.apply { mediaPlayer?.currentPosition?.let { progress = it } }
            handler.postDelayed(this, 10)
        }
    }
    //endregion

    //region Life-Cycle Methods
    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(activity)
            .setView(LayoutInflater.from(activity).inflate(R.layout.dialog_audio_file, null).apply {
                etHeader = findViewById(R.id.dialog_audio_file_etHeader) as EditText
                ivButton = findViewById(R.id.dialog_audio_file_ivButton) as ImageView
                tvDescription = findViewById(R.id.dialog_audio_file_tvDescription) as TextView
                tvDuration = findViewById(R.id.dialog_audio_file_tvDuration) as TextView
                sbProgress = findViewById(R.id.dialog_audio_file_sbProgress) as SeekBar
                etContent = findViewById(R.id.dialog_audio_file_etContent) as EditText

                arguments?.getString(ARG_AUDIO_FILE_PATH)?.let { audioFilePath ->
                    ivButton?.apply {
                        try {
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(FileInputStream(audioFilePath).fd)
                                prepare()
                                setImageResource(R.drawable.ic_play_green)
                                setOnClickListener { onPlayClick() }
                                tvDescription?.text = formattedDuration(0)
                                tvDuration?.text = formattedDuration(duration)
                                sbProgress?.apply {
                                    max = duration
                                    progress = 0
                                    setOnSeekBarChangeListener(this@AudioDialog)
                                }
                                setOnCompletionListener {
                                    seekTo(0)
                                    setImageResource(R.drawable.ic_play_green)
                                    setOnClickListener { onPlayClick() }
                                    tvDescription?.text = formattedDuration(0)
                                    sbProgress?.progress = 0
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
            .setNegativeButton("Cancel", this)
            .setPositiveButton("Send", this)
            .create()!!
    //endregion

    //region Private Tools
    private fun onPlayClick() {
        handler.post(updateTimeRunnable)
        mediaPlayer?.start()
        ivButton?.apply {
            setImageResource(R.drawable.ic_stop_green)
            setOnClickListener { onStopClick() }
        }
    }

    private fun onStopClick() {
        handler.removeCallbacks(updateTimeRunnable)
        mediaPlayer?.apply {
            pause()
            seekTo(0)
            sbProgress?.progress = 0
        }
        ivButton?.apply {
            setImageResource(R.drawable.ic_play_green)
            setOnClickListener { onPlayClick() }
        }
    }

    private fun formattedDuration(durationInMillis: Int) = (durationInMillis / 1000).let {
        val sec: Int = it % 60
        val min: Int = it / 60
        String.format("%02d:%02d", min, sec)
    }
    //endregion

    //region OnClickListener Implementation
    override fun onClick(dialog: DialogInterface, which: Int) {
        val data = Intent().apply {
            putExtra(EXTRA_AUDIO_FILE_PATH, arguments.getString(ARG_AUDIO_FILE_PATH))
            if (which == DialogInterface.BUTTON_POSITIVE) {
                putExtra(EXTRA_HEADER, etHeader?.text?.toString()?.orNullIfBlank())
                putExtra(EXTRA_CONTENT, etContent?.text?.toString()?.orNullIfBlank())
            }
        }
        targetFragment.onActivityResult(targetRequestCode, which, data)
    }
    //endregion

    //region OnSeekBarChangeListener Implementation
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) mediaPlayer?.seekTo(progress)
        tvDescription?.text = formattedDuration(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    //endregion
}