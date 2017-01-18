package com.owfar.android.ui.boards.stickers

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.owfar.android.R
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType

class StickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    //region widgets
    private val ivSticker: ImageView
    //endregion

    //region fields
    private var isLoaded = false
    //endregion

    //region Initialization
    init {
        ivSticker = itemView.findViewById(R.id.item_board_sticker_ivSticker) as ImageView
    }
    //endregion

    //region View Configuration
    fun configureWith(sticker: Sticker, selected: Boolean) {
        with(itemView) {
            isSelected = selected
            setOnClickListener(this@StickerViewHolder)
        }
        isLoaded = false
        MediaHelper
                .load(sticker.mediaFile)
                .withOptions(MediaStorageType.STICKERS, MediaSize._2X)
                .into(ivSticker) { result -> isLoaded = result }
    }
    //endregion

    //region OnStickerClickListener
    private var listener: OnStickerClickListener? = null

    fun setOnStickerClickListener(listener: OnStickerClickListener?) {
        this.listener = listener
    }

    override fun onClick(v: View) {
        if (isLoaded) listener?.onStickerClick(adapterPosition)
    }

    interface OnStickerClickListener {
        fun onStickerClick(adapterPosition: Int)
    }
    //endregion
}
