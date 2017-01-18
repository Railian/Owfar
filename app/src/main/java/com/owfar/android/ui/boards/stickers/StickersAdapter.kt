package com.owfar.android.ui.boards.stickers

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.owfar.android.R
import com.owfar.android.models.api.classes.Sticker

class StickersAdapter(private val selectable: Boolean) : RecyclerView.Adapter<StickerViewHolder>(),
        StickerViewHolder.OnStickerClickListener {

    //region fields
    private var selectedStickerPosition: Int? = null
    //endregion

    //region DataSource Methods
    var stickers: MutableList<Sticker>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun getSticker(adapterPosition: Int)
            = stickers?.getOrNull(adapterPosition)
    //endregion

    //region Adapter Methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_board_sticker, parent, false)
        return StickerViewHolder(itemView).apply { setOnStickerClickListener(this@StickersAdapter) }
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        getSticker(position)?.let { holder.configureWith(it, position == selectedStickerPosition) }
    }

    override fun getItemCount() = stickers?.size ?: 0
    //endregion

    //region Public Tools
    fun clearSelection() = selectedStickerPosition?.let {
        selectedStickerPosition = null
        notifyItemChanged(it)
    } ?: Unit
    //endregion

    //region OnStickerClickListener
    private var listener: OnStickerClickListener? = null

    fun setOnStickerClickListener(listener: OnStickerClickListener) {
        this.listener = listener
    }

    interface OnStickerClickListener {
        fun onStickerClick(adapterPosition: Int, sticker: Sticker)
    }

    override fun onStickerClick(adapterPosition: Int) {
        if (!selectable || selectedStickerPosition == adapterPosition) {
            getSticker(adapterPosition)?.let { listener?.onStickerClick(adapterPosition, it) }
            selectedStickerPosition = null
        } else {
            val oldSelectedStickerPosition = selectedStickerPosition
            selectedStickerPosition = adapterPosition
            oldSelectedStickerPosition?.let { notifyItemChanged(it) }
        }
        notifyItemChanged(adapterPosition)
    }
    //endregion
}