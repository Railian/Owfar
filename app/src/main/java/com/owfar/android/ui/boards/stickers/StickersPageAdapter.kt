package com.owfar.android.ui.boards.stickers

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewCompat.setNestedScrollingEnabled
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.owfar.android.R
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.classes.StickersGroup

class StickersPageAdapter : PagerAdapter(), StickersAdapter.OnStickerClickListener {

    //region Data-Source Methods
    var stickersGroups: MutableList<StickersGroup>? = null

    fun getStickersGroup(adapterPosition: Int)
            = stickersGroups?.getOrNull(adapterPosition)
    //endregion

    //region Adapter Methods
    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(collection.context)
        val itemView = inflater.inflate(R.layout.fragment_stickers_page, collection, false) as ViewGroup
        return itemView.apply {
            val rvStickers = findViewById(R.id.fragment_stickers_page_rvStickers) as RecyclerView
            setNestedScrollingEnabled(rvStickers, false)
            rvStickers.apply {
                layoutManager = GridLayoutManager(collection.context, 3)
                adapter = StickersAdapter(selectable = true).apply {
                    stickers = getStickersGroup(position)?.stickers
                    setOnStickerClickListener(this@StickersPageAdapter)
                }
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                        (adapter as? StickersAdapter)?.clearSelection()
                    }
                })
            }
            collection.addView(this)
        }
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any)
            = collection.removeView(view as View)

    override fun getCount()
            = stickersGroups?.size ?: 0

    override fun isViewFromObject(view: View, `object`: Any)
            = view === `object`

    override fun getPageTitle(position: Int)
            = getStickersGroup(position)?.name
    //endregion

    //region OnStickerClickListener
    private var listener: OnStickerSelectedListener? = null

    fun setOnStickerSelectedListener(listener: OnStickerSelectedListener) {
        this.listener = listener
    }

    override fun onStickerClick(adapterPosition: Int, sticker: Sticker)
            = listener?.onStickerSelected(sticker) ?: Unit
    //endregion
}