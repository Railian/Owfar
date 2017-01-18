package com.owfar.android.ui.chats

import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.owfar.android.R
import com.owfar.android.models.api.classes.Stream

class ChatItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ChatItemHolder.OnItemClickListener {

    //    region Data-Source Methods
    var streams: MutableList<Stream>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var removedStream: Stream? = null
    private var removedStreamAdapterPosition: Int = 0

    fun getStream(adapterPosition: Int) = removedStream?.let {
        streams?.get(adapterPosition - if (adapterPosition > removedStreamAdapterPosition) 1 else 0)
    } ?: streams?.get(adapterPosition)

    fun remove(adapterPosition: Int) = let {
        removedStream = streams?.get(adapterPosition)
        removedStreamAdapterPosition = adapterPosition
        notifyItemRemoved(adapterPosition)
        removedStream
    }

    fun undoRemoving() {
        removedStream?.let {
            removedStream = null
            notifyItemInserted(removedStreamAdapterPosition)
        }
    }

    fun onRemovingConfirmed() = removedStream?.let {
        removedStream = null
        streams?.remove(it)
        it
    }
    //endregion

    //region Adapter Methods
    enum class ViewType {ITEM, FOOTER }

    override fun getItemViewType(position: Int) = when (position) {
        itemCount - 1 -> ViewType.FOOTER.ordinal
        else -> ViewType.ITEM.ordinal
    }

    fun getViewType(itemViewType: Int) = ViewType.values()[itemViewType]

    override fun onCreateViewHolder(parent: ViewGroup, itemViewType: Int) = when (getViewType(itemViewType)) {
        ViewType.ITEM -> {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.item_chat, parent, false)
            ChatItemHolder(itemView).apply { setOnItemClickListener(this@ChatItemAdapter) }
        }
        ViewType.FOOTER -> {
            val space = Space(parent.context)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = parent.resources.getDimension(R.dimen.chats_footer_height).toInt()
            ChatFooterHolder(space.apply { layoutParams = RecyclerView.LayoutParams(width, height) })
        }
        else -> null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getViewType(getItemViewType(position))) {
            ViewType.ITEM -> (holder as ChatItemHolder).configureWith(getStream(position))
            ViewType.FOOTER -> Unit
        }
    }

    override fun getItemCount() = streams?.size?.let {
        it + 1/*footer*/ - if (removedStream != null) 1 else 0
    } ?: 0
    //endregion

    //region OnItemClickListener Implementation
    override fun onItemClick(view: View, adapterPosition: Int) {
        getStream(adapterPosition)?.let { listener?.onChatClick(it) }
    }
    //endregion

    //region OnChatClickListener
    private var listener: OnChatClickListener? = null

    fun setOnChatClickListener(listener: OnChatClickListener) {
        this.listener = listener
    }

    interface OnChatClickListener {
        fun onChatClick(stream: Stream)
    }
    //endregion
}
