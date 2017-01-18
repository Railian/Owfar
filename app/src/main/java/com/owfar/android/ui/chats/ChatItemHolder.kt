package com.owfar.android.ui.chats

import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.media.CircleTransform
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.StreamType


class ChatItemHolder(itemView: View)
    : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val vContent: View
    val removableView: View
    private val ivAvatar: ImageView
    private val tvContactName: TextView
    private val tvUnreadCount: TextView
    private val tvTicket: TextView
    private val tvLastUpdatedAt: TextView

    init {
        vContent = itemView.findViewById(R.id.item_chat_vContent)
        removableView = itemView.findViewById(R.id.item_chat_vRemovable)
        ivAvatar = itemView.findViewById(R.id.item_chat_ivAvatar) as ImageView
        tvContactName = itemView.findViewById(R.id.item_chat_tvContactName) as TextView
        tvUnreadCount = itemView.findViewById(R.id.item_chat_tvUnreadCount) as TextView
        tvTicket = itemView.findViewById(R.id.item_chat_tvTicket) as TextView
        tvLastUpdatedAt = itemView.findViewById(R.id.item_chat_tvLastMessageDate) as TextView
    }

    fun configureWith(stream: Stream?) {
        alpha(1)

        stream?.apply {
            MediaHelper
                    .load(image)
                    .withOptions(imageMediaType, MediaSize._1X)
                    .transform(CircleTransform())
                    .placeholder(android.R.color.transparent)
                    .error(android.R.color.transparent)
                    .into(ivAvatar)

            tvContactName.text = displayName
            if (unreadCount > 0) {
                tvUnreadCount.text = unreadCount.toString()
                tvUnreadCount.visibility = View.VISIBLE
                tvUnreadCount.setBackgroundResource(StreamType.find(type)
                        ?.notificationRes ?: android.R.color.transparent)
                tvTicket.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            } else {
                tvUnreadCount.visibility = View.GONE
                tvTicket.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
            }
            tvTicket.text = ticket
            tvLastUpdatedAt.text = formattedLastUpdatedAt
        }
    }

    fun alpha(alpha: Int) {
        vContent.alpha = alpha.toFloat()
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
        itemView.setOnClickListener(listener?.let { this })
    }

    override fun onClick(v: View) {
        if (v === itemView) listener?.onItemClick(v, adapterPosition)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, adapterPosition: Int)
    }
}
