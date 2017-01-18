package com.owfar.android.ui.messenger.holders

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.github.siyamed.shapeimageview.CircularImageView
import com.owfar.android.R
import com.owfar.android.extensions.formattedTime
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.*
import com.owfar.android.models.api.interfaces.Message
import com.squareup.picasso.Picasso
import io.realm.RealmList

open class MessengerItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val TAG = MessengerItemViewHolder::class.java.simpleName

    private val civPhoto: CircularImageView?
    private val tvDisplayName: TextView?
    private val tvTime: TextView?
    private val tvStatus: TextView?

    private val res: Resources

    init {
        itemView.isLongClickable = true
        civPhoto = view.findViewById(R.id.item_messenger_civPhoto) as? CircularImageView
        tvDisplayName = view.findViewById(R.id.item_messenger_tvDisplayName) as? TextView
        tvTime = view.findViewById(R.id.item_messenger_tvTime) as? TextView
        tvStatus = view.findViewById(R.id.item_messenger_tvStatus) as? TextView
        res = itemView.resources
    }

    //region Configuration Of View
    open fun configureWithMessage(streamType: StreamType?, message: Message?, showStatus: Boolean, users: RealmList<User>?) {

        civPhoto?.apply {
            when (streamType) {
                StreamType.CONVERSATIONS, StreamType.CHATS ->
                    message?.user?.profile?.photo?.let {
                        MediaHelper
                                .load(it)
                                .withOptions(MediaStorageType.USERS_PHOTOS, MediaSize._1X)
                                .placeholder(R.drawable.temp_avatar)
                                .into(this)
                    } ?: Picasso.with(itemView.context)
                            .load(R.drawable.temp_avatar)
                            .resize(0, 100)
                            .into(this)
                StreamType.INTERESTS -> visibility = View.GONE
            }
        }

        tvDisplayName?.apply {
            visibility = if (streamType == StreamType.CHATS) View.VISIBLE else View.GONE
            text = message?.user?.displayName ?: "Unknown user"
        }

        tvTime?.apply {
            text = message?.lastUpdatedAt()?.formattedTime
            visibility = when (MessageBodyType.find(message?.bodyType)) {
                MessageBodyType.DELETED -> View.GONE
                else -> View.VISIBLE
            }
        }

        tvStatus?.apply {
            visibility = if (showStatus) View.VISIBLE else View.GONE
            when (message?.messageClassType()) {
                MessageClassType.SENT -> MessageStatus.find(message?.asSent()?.messageStatus)
                MessageClassType.RECEIVED -> message?.asReceived()?.getMessageStatus(users)
                else -> null
            }?.let { status -> text = status.jsonName } ?: let { visibility = View.GONE }
        }
    }
    //endregion
}