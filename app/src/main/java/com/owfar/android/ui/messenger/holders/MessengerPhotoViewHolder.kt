package com.owfar.android.ui.messenger.holders

import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.api.file.FileManager
import com.owfar.android.api.users.ProgressListener
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType
import com.owfar.android.models.api.enums.MessageClassType
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.settings.CurrentUserSettings
import com.owfar.android.ui.preview_image.PreviewImageActivity
import io.realm.RealmList

class MessengerPhotoViewHolder(view: View) : MessengerItemViewHolder(view), View.OnLongClickListener {

    private val tvHeader: TextView?
    private val ivPhoto: ImageView?
    private val tvContent: TextView?
    private val vProgress: View?
    private val tvProgress: TextView?

    private var message: Message? = null
    private val mediaSize = MediaSize._3X

    private val handler = Handler()

    init {
        tvHeader = view.findViewById(R.id.item_messenger_tvHeader) as? TextView
        ivPhoto = view.findViewById(R.id.item_messenger_ivPhoto) as? ImageView
        tvContent = view.findViewById(R.id.item_messenger_tvContent) as? TextView
        vProgress = view.findViewById(R.id.item_messenger_vProgress)
        tvProgress = view.findViewById(R.id.item_messenger_tvProgress) as? TextView

        ivPhoto?.apply {
            setOnClickListener { onPhotoClick() }
            setOnLongClickListener(this@MessengerPhotoViewHolder)
        }}

    //region Configuration Of View
    override fun configureWithMessage(streamType: StreamType?, message: Message?, showStatus: Boolean, users: RealmList<User>?) {
        super.configureWithMessage(streamType, message, showStatus, users)
        this.message = message
        message?.let {
            when (it.messageClassType()) {
                MessageClassType.SENT -> Unit
                MessageClassType.RECEIVED -> {
                    it.asReceived()?.media?.let { photo ->
                        FileManager.get().downloadDelegatesSet.removeDelegate(progressListener)
                        FileManager.get().downloadDelegatesSet.addDelegate("${photo.mediaFileId}$mediaSize", progressListener)
                        tvHeader?.apply {
                            photo.header?.let { header ->
                                visibility = if (header.isNotBlank()) View.VISIBLE else View.GONE
                                text = header
                            } ?: let { visibility = View.GONE }
                        }
                        ivPhoto?.apply {
                            MediaHelper
                                    .load(photo)
                                    .withOptions(MediaStorageType.IMAGES, mediaSize)
                                    .placeholder(R.drawable.bg_messenger_photo_placeholder)
                                    .error(R.drawable.bg_messenger_photo_placeholder)
                                    .into(this)
                        }
                        tvContent?.apply {
                            photo.content?.let { content ->
                                visibility = if (content.isNotBlank()) View.VISIBLE else View.GONE
                                text = content
                            } ?: let { visibility = View.GONE }
                        }
                    } ?: let {
                        tvHeader?.visibility = View.GONE
                        ivPhoto?.apply {
                            MediaHelper
                                    .load(R.drawable.bg_messenger_photo_placeholder)
                                    .placeholder(R.drawable.bg_messenger_photo_placeholder)
                                    .error(R.drawable.bg_messenger_photo_placeholder)
                                    .into(this)
                        }
                        tvContent?.visibility = View.GONE
                    }
                }
                else -> Unit
            }
        }
    }
    //endregion

    private fun onPhotoClick() {
        when (message?.messageClassType()) {
            MessageClassType.SENT ->
                Unit
            MessageClassType.RECEIVED ->
                PreviewImageActivity.start(itemView.context, message?.asReceived()?.media)
        }
    }

    override fun onLongClick(p0: View?): Boolean {
        return if (message?.user?.id == CurrentUserSettings.currentUser?.id)
            itemView.performLongClick()
        else false
    }

    private val progressListener = object : ProgressListener.Simple() {

        override fun onStarted() {
            handler.post {
                tvProgress?.text = "Image loading..."
                vProgress?.visibility = View.VISIBLE
            }
        }

        override fun onUpdated(bytesRead: Long, contentLength: Long) {
            handler.post {
                tvProgress?.text = "Image loading...${bytesRead * 100 / contentLength}%"
            }
        }

        override fun onFinished() {
            handler.post {
                tvProgress?.text = "Image is loaded!"
                vProgress?.visibility = View.GONE
            }
        }
    }
}