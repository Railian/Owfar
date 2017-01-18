package com.owfar.android.ui.messenger.holders

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MessageClassType
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import io.realm.RealmList
import java.io.File
import java.util.*

class MessengerVideoViewHolder(view: View) : MessengerItemViewHolder(view) {

    private val tvHeader: TextView?
    private val ivVideo: ImageView?
    private val tvContent: TextView?
    private val ivDownloadVideo: ImageView?
    private val ivPlayVideo: ImageView?

    init {
        tvHeader = view.findViewById(R.id.item_messenger_tvHeader) as? TextView
        ivVideo = view.findViewById(R.id.item_messenger_ivVideo) as? ImageView
        tvContent = view.findViewById(R.id.item_messenger_tvContent) as? TextView
        ivDownloadVideo = view.findViewById(R.id.item_messenger_ivDownloadVideo) as? ImageView
        ivPlayVideo = view.findViewById(R.id.item_messenger_ivPlayVideo) as? ImageView

        ivVideo?.setOnClickListener { onVideoClick() }
        ivDownloadVideo?.setOnClickListener { onDownloadClick() }
        ivPlayVideo?.setOnClickListener { onPlayClick() }
    }

    //region Configuration Of View
    override fun configureWithMessage(streamType: StreamType?, message: Message?, showStatus: Boolean, users: RealmList<User>?) {
        super.configureWithMessage(streamType, message, showStatus, users)
        when (message?.messageClassType()) {
            MessageClassType.SENT -> {
            }
            MessageClassType.RECEIVED -> {
                message?.asReceived()?.media?.let { video ->
                    tvHeader?.apply {
                        video.header?.let { header ->
                            visibility = if (header.isNotBlank()) View.VISIBLE else View.GONE
                            text = header
                        } ?: let { visibility = View.GONE }
                    }
                    video.getPath(MediaSize._DEFAULT)?.let {
                        if (File(it).exists()) ivDownloadVideo?.visibility = View.GONE
                        LoadVideoThumbnail().execute(it)
                    }
                    tvContent?.apply {
                        video.content?.let { content ->
                            visibility = if (content.isNotBlank()) View.VISIBLE else View.GONE
                            text = content
                        } ?: let { visibility = View.GONE }
                    }
                }
            }
        }
    }
    //endregion

    //region UI Listeners
    private fun onVideoClick() {
        TODO()
    }

    private fun onDownloadClick() {
        TODO()
    }

    private fun onPlayClick() {
        TODO()
    }
    //endregion

    inner class LoadVideoThumbnail : AsyncTask<String, Any, Bitmap?>() {

        override fun doInBackground(vararg objectURL: String): Bitmap? {
            val videoPath = objectURL[0]
            val bitmap: Bitmap? = null
            var mediaMetadataRetriever: MediaMetadataRetriever? = null
            try {
                mediaMetadataRetriever = MediaMetadataRetriever()
                if (Build.VERSION.SDK_INT < 14)
                    mediaMetadataRetriever.setDataSource(videoPath)
                else
                    mediaMetadataRetriever.setDataSource(videoPath, HashMap<String, String>())
                var b: Bitmap? = mediaMetadataRetriever.frameAtTime
                if (b != null) {
                    val b2 = mediaMetadataRetriever.getFrameAtTime(4000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    if (b2 != null) b = b2
                }
                if (b != null) {
                    Log.i("Thumbnail", "Extracted frame")
                    return b
                } else
                    Log.e("Thumbnail", "Failed to extract frame")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (mediaMetadataRetriever != null) mediaMetadataRetriever.release()
            }
            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            ivVideo?.apply {
                result?.let { setImageBitmap(result) }
                        ?: setImageResource(R.drawable.bg_messenger_video_placeholder)
            }
        }
    }
}