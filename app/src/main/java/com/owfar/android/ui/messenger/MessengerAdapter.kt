package com.owfar.android.ui.messenger

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.owfar.android.R
import com.owfar.android.extensions.simplifyToDay
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.ui.messenger.holders.*
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import io.realm.RealmList
import java.util.*

class MessengerAdapter(
        private val streamType: StreamType,
        private var users: RealmList<User>?
) : RecyclerView.Adapter<MessengerItemViewHolder>(),
        StickyRecyclerHeadersAdapter<MessengerHeaderViewHolder> {

    //region DataSource Methods
    var messages: MutableList<Message>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun addOldMessages(oldMessages: MutableList<Message>?) {
        oldMessages?.let { old ->
            messages?.apply {
                val startPosition = size
                addAll(old)
                notifyItemRangeInserted(startPosition, old.size)
            } ?: let {
                messages = RealmList<Message>().apply {
                    addAll(old)
                    notifyItemRangeInserted(0, size)
                }
            }
        }
    }

    fun addNewMessage(newMessage: Message?) {
        newMessage?.let { new ->
            messages?.apply {
                find { new.sid == it.sid }?.let { remove(it) }
                add(0, new)
                notifyDataSetChanged()
//                notifyItemInserted(0)
//                if (size > 1) notifyItemChanged(1)
            } ?: let {
                messages = RealmList<Message>().apply {
                    add(new)
                    notifyDataSetChanged()
//                    notifyItemInserted(0)
                }
            }
        }
    }

    fun getMessage(adapterPosition: Int) = messages?.get(adapterPosition)
    //endregion

    //region Adapter Methods
    override fun getItemId(position: Int)
            = getMessage(position)?.asReceived()?.id ?: -1

    override fun getItemViewType(position: Int)
            = MessageViewType.find(getMessage(position))?.ordinal ?: -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessengerItemViewHolder? {
        val inflater = LayoutInflater.from(parent.context)
        val messageViewType = MessageViewType.values()[viewType]
        val itemView = inflater.inflate(messageViewType.layoutRes, parent, false)
        return when (messageViewType) {
            MessageViewType.LEFT_COMMENT, MessageViewType.RIGHT_COMMENT -> MessengerCommentViewHolder(itemView)
            MessageViewType.LEFT_STICKER, MessageViewType.RIGHT_STICKER -> MessengerStickerViewHolder(itemView)
            MessageViewType.LEFT_PHOTO, MessageViewType.RIGHT_PHOTO -> MessengerPhotoViewHolder(itemView)
            MessageViewType.LEFT_AUDIO, MessageViewType.RIGHT_AUDIO -> MessengerAudioViewHolder(itemView)
            MessageViewType.LEFT_VIDEO, MessageViewType.RIGHT_VIDEO -> MessengerVideoViewHolder(itemView)
            MessageViewType.LEFT_DELETED, MessageViewType.RIGHT_DELETED -> MessengerItemViewHolder(itemView)
            MessageViewType.SYSTEM -> MessengerCommentViewHolder(itemView)
            else -> null
        }
    }

    override fun onBindViewHolder(holder: MessengerItemViewHolder, position: Int) {
        holder.configureWithMessage(streamType, getMessage(position), position == 0, users)
    }

    override fun getItemCount() = messages?.size ?: 0
    //endregion

    //region Headers
    override fun getHeaderId(position: Int)
            = getMessage(position)?.lastUpdatedAt()?.simplifyToDay?.time ?: 0

    override fun onCreateHeaderViewHolder(parent: ViewGroup): MessengerHeaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_messanger_header_section, parent, false)
        return MessengerHeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: MessengerHeaderViewHolder, position: Int) {
        val sentAtDayInMillis = getHeaderId(position)
        val sentAtDay = Date(sentAtDayInMillis)
        holder.configureWith(sentAtDay)
    }
    //endregion
}