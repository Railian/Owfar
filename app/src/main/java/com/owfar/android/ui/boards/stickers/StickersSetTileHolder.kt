package com.owfar.android.ui.boards.stickers

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.StickersGroup
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType

class StickersSetTileHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val cvCard: CardView
    private val ivCover: ImageView
    private val tvSetName: TextView
    private val tvCount: TextView

    init {
        cvCard = itemView.findViewById(R.id.item_stickers_board_page_premium_tile_cvCard) as CardView
        ivCover = itemView.findViewById(R.id.item_stickers_board_page_premium_tile_ivCover) as ImageView
        tvSetName = itemView.findViewById(R.id.item_stickers_board_page_premium_tile_tvSetName) as TextView
        tvCount = itemView.findViewById(R.id.item_stickers_board_page_premium_tile_tvCount) as TextView
    }

    fun configureWith(stickersGroup: StickersGroup, selected: Boolean) {
        cvCard.setCardBackgroundColor(itemView.resources
                .getColor(if (selected) R.color.colorPrimary else R.color.colorLightGray))
        with(stickersGroup) {
            MediaHelper
                    .load(stickers?.firstOrNull()?.mediaFile ?: photo)
                    .withOptions(MediaStorageType.STICKERS, MediaSize._2X)
                    .into(ivCover)
            tvSetName.text = name
            tvCount.text = "${stickersGroup.stickers?.size ?: 0} stickers"
        }
    }

    private var listener: OnStickersSetClickListener? = null

    fun setOnStickersSetClickListener(listener: OnStickersSetClickListener?) {
        cvCard.setOnClickListener(listener?.let { this })
        this.listener = listener
    }

    override fun onClick(v: View)
            = listener?.onStickersSetClick(adapterPosition) ?: Unit

    interface OnStickersSetClickListener {
        fun onStickersSetClick(adapterPosition: Int)
    }
}