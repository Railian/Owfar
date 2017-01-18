package com.owfar.android.ui.boards.stickers

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.owfar.android.R
import com.owfar.android.models.api.classes.StickersGroup

class StickersSetAdapter : RecyclerView.Adapter<StickersSetTileHolder>(),
        StickersSetTileHolder.OnStickersSetClickListener {

    //region DataSource Methods
    var stickersGroups: MutableList<StickersGroup>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun getStickersGroup(adapterPosition: Int)
            = stickersGroups?.getOrNull(adapterPosition)

    private var selectedAdapterPosition: Int? = null
        private set(value) {
            field = value
            notifyDataSetChanged()
        }
    //endregion

    //region Adapter Methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickersSetTileHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_stickers_board_page_premium_tile, parent, false)
        return StickersSetTileHolder(itemView).apply {
            setOnStickersSetClickListener(this@StickersSetAdapter)
        }
    }

    override fun onBindViewHolder(holder: StickersSetTileHolder, position: Int) {
        getStickersGroup(position)?.let {
            holder.configureWith(it, position == selectedAdapterPosition)
        }
    }

    override fun getItemCount()
            = stickersGroups?.size ?: 0
    //endregion

    //region OnStickersSetClickListener
    private var listener: OnStickersSetClickListener? = null

    fun setOnStickersSetClickListener(listener: OnStickersSetClickListener) {
        this.listener = listener
    }

    override fun onStickersSetClick(adapterPosition: Int) {
        selectedAdapterPosition = adapterPosition
        getStickersGroup(adapterPosition)?.let { listener?.onStickersSetClick(adapterPosition, it) }
    }

    interface OnStickersSetClickListener {
        fun onStickersSetClick(adapterPosition: Int, stickersGroup: StickersGroup)
    }
    //endregion
}
