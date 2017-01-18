package com.owfar.android.ui.messenger.holders

import android.media.MediaPlayer
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType
import com.owfar.android.models.api.enums.MessageClassType
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import io.realm.RealmList
import org.jetbrains.anko.imageResource
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class MessengerAudioViewHolder(view: View) : MessengerItemViewHolder(view), SeekBar.OnSeekBarChangeListener {

    //region widgets
    private var tvHeader: TextView? = null
    private var ivButton: ImageView? = null
    private var tvDescription: TextView? = null
    private var tvDuration: TextView? = null
    private var pbProgress: ProgressBar? = null
    private var sbProgress: SeekBar? = null
    private var tvContent: TextView? = null
    //endregion

    //region fields
    private var message: Message? = null

    private var mediaPlayer: MediaPlayer? = null

    private val handler = Handler()
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            sbProgress?.apply { mediaPlayer?.currentPosition?.let { progress = it } }
            handler.postDelayed(this, 10)
        }
    }
    //endregion

    //region Initialization
    init {
        tvHeader = view.findViewById(R.id.item_messenger_tvHeader) as? TextView
        ivButton = view.findViewById(R.id.item_messenger_ivButton) as? ImageView
        tvDescription = view.findViewById(R.id.item_messenger_tvDescription) as? TextView
        tvDuration = view.findViewById(R.id.item_messenger_tvDuration) as? TextView
        pbProgress = view.findViewById(R.id.item_messenger_pbProgress) as? ProgressBar
        sbProgress = view.findViewById(R.id.item_messenger_sbProgress) as? SeekBar
        tvContent = view.findViewById(R.id.item_messenger_tvContent) as? TextView
    }
    //endregion

    //region Configuration Of View
    override fun configureWithMessage(streamType: StreamType?, message: Message?,
                                      showStatus: Boolean, users: RealmList<User>?) {
        super.configureWithMessage(streamType, message, showStatus, users)
        this.message = message
        message?.let {
            when (it.messageClassType()) {
                MessageClassType.SENT -> Unit
                MessageClassType.RECEIVED -> {
                    it.asReceived()?.media?.let { audio ->
                        tvHeader?.apply {
                            audio.header?.let { header ->
                                visibility = if (header.isNotBlank()) View.VISIBLE else View.GONE
                                text = header
                            } ?: let { visibility = View.GONE }
                        }
                        if (MediaHelper.isDefaultLocalFileExists(MediaStorageType.AUDIOS, audio))
                            configureWithAudioFile(MediaHelper.getDefaultLocalFile(MediaStorageType.AUDIOS, audio))
                        else {
                            ivButton?.apply {
                                imageResource = R.drawable.ic_download_green
                                setOnClickListener { onDownloadClick() }
                            }
                            tvDescription?.text = "Audio file"
                            tvDuration?.text = null
                            pbProgress?.apply {
                                isIndeterminate = false
                                visibility = View.VISIBLE
                            }
                            sbProgress?.visibility = View.GONE
                        }
                        tvContent?.apply {
                            audio.content?.let { content ->
                                visibility = if (content.isNotBlank()) View.VISIBLE else View.GONE
                                text = content
                            } ?: let { visibility = View.GONE }
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    private fun configureWithAudioFile(audioFile: File) = try {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(FileInputStream(audioFile).fd)
            prepare()
            ivButton?.apply {
                imageResource = R.drawable.ic_play_green
                setOnClickListener { onPlayClick() }
                setOnCompletionListener {
                    seekTo(0)
                    imageResource = R.drawable.ic_play_green
                    setOnClickListener { onPlayClick() }
                    tvDescription?.text = formattedDuration(0)
                    sbProgress?.progress = 0
                }
            }
            tvDescription?.text = formattedDuration(0)
            tvDuration?.text = formattedDuration(duration)
            pbProgress?.visibility = View.GONE
            sbProgress?.apply {
                max = duration
                progress = 0
                visibility = View.VISIBLE
                setOnSeekBarChangeListener(this@MessengerAudioViewHolder)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    //endregion

    //region Private Tools
    private fun onDownloadClick() {
        pbProgress?.isIndeterminate = true
        MediaHelper
                .load(message?.asReceived()?.media)
                .withOptions(MediaStorageType.AUDIOS, MediaSize._DEFAULT)
                .forResult { configureWithAudioFile(it) }
    }

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

    //region OnSeekBarChangeListener Implementation
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) mediaPlayer?.seekTo(progress)
        tvDescription?.text = formattedDuration(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    //endregion
}