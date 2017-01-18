package com.owfar.android.ui.boards.stickers

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewCompat.setNestedScrollingEnabled
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.owfar.android.R
import com.owfar.android.models.api.StickerList
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.classes.StickersGroup
import java.util.*

class StickersBoardPageAdapter(private val context: Context) : PagerAdapter(),
        OnStickerSelectedListener, StickersAdapter.OnStickerClickListener {

    //region DataSource
    private var recent: MutableList<Sticker>? = null
    private var myStickers: MutableList<StickersGroup>? = null
    private var myAnimations: MutableList<StickersGroup>? = null
    private var premium: MutableList<StickersGroup>? = null

    fun setStickerList(stickerList: StickerList) {
        recent = stickerList.recent
        stickerList.groups?.groupBy { it.isFree == true || it.isBought == true }?.let {
            myStickers = it[true]?.toMutableList()?.apply {
                myAnimations = filter { it.id == 1L }.toMutableList()
                removeAll(filter { it.id == 1L })
            }
            premium = it[false]?.toMutableList()
        }
//        premium = stickerList.groups?.filterNot { it.name?.contains("Free") == true }?.toMutableList()
        notifyDataSetChanged()
    }
    //endregion

    //region Adapter Methods
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val stickersBoardPageEnum = StickersBoardPageEnum.values()[position]
        val inflater = LayoutInflater.from(container.context)
        val layout = inflater.inflate(stickersBoardPageEnum.layoutResId, container, false) as ViewGroup
        with(layout) {
            when (stickersBoardPageEnum) {
                StickersBoardPageEnum.RECENT -> {
                    val rvStickers = findViewById(R.id.page_stickers_board_recent_rvStickers) as RecyclerView
                    rvStickers.apply {
                        setNestedScrollingEnabled(this, false)
                        layoutManager = GridLayoutManager(container.context, 3)
                        adapter = StickersAdapter(selectable = true).apply {
                            stickers = recent
                            setOnStickerClickListener(this@StickersBoardPageAdapter)
                        }
                    }
                }
                StickersBoardPageEnum.MY_STICKERS -> {
                    val tlTabs = findViewById(R.id.page_stickers_board_my_stickers_tlTabs) as TabLayout
                    val vpStickersGroups = findViewById(R.id.page_stickers_board_my_stickers_vpStickersGroups) as ViewPager
                    tlTabs.apply {
                        visibility = if (myStickers?.size ?: 0 > 1) View.VISIBLE else View.GONE
                        setupWithViewPager(vpStickersGroups)
                    }
                    vpStickersGroups.apply {
                        adapter = StickersPageAdapter().apply {
                            stickersGroups = myStickers
                            setOnStickerSelectedListener(this@StickersBoardPageAdapter)
                        }
                    }
                }
                StickersBoardPageEnum.MY_ANIMATIONS -> {
                    val tlTabs = layout.findViewById(R.id.page_stickers_board_my_animations_tlTabs) as TabLayout
                    val vpStickersGroups = layout.findViewById(R.id.page_stickers_board_my_animations_vpStickersGroups) as ViewPager
                    tlTabs.apply {
                        visibility = if (myAnimations?.size ?: 0 > 1) View.VISIBLE else View.GONE
                        setupWithViewPager(vpStickersGroups)
                    }
                    vpStickersGroups.apply {
                        adapter = StickersPageAdapter().apply {
                            stickersGroups = myAnimations
                            setOnStickerSelectedListener(this@StickersBoardPageAdapter)
                        }
                    }
                }
                StickersBoardPageEnum.PREMIUM -> {
                    val rvStickersSet = layout.findViewById(R.id.page_stickers_board_premium_rvStickersSet) as RecyclerView
                    val rvStickers = layout.findViewById(R.id.page_stickers_board_premium_rvStickers) as RecyclerView

                    setNestedScrollingEnabled(rvStickersSet, false)
                    setNestedScrollingEnabled(rvStickers, false)

                    rvStickersSet.apply {
                        layoutManager = GridLayoutManager(container.context, 1)
                        adapter = StickersSetAdapter().apply {
                            stickersGroups = premium
                            setOnStickersSetClickListener(object : StickersSetAdapter.OnStickersSetClickListener {
                                override fun onStickersSetClick(adapterPosition: Int, stickersGroup: StickersGroup) {
                                    (rvStickers.adapter as? StickersAdapter)?.stickers = stickersGroup.stickers
                                }
                            })
                        }
                    }

                    rvStickers.apply {
                        layoutManager = GridLayoutManager(container.context, 3)
                        adapter = StickersAdapter(selectable = false).apply {
                            stickers = premium?.firstOrNull()?.stickers?.let { ArrayList(it) }
                            setOnStickerClickListener(object : StickersAdapter.OnStickerClickListener {
                                override fun onStickerClick(adapterPosition: Int, sticker: Sticker) {
                                    AlertDialog.Builder(container.context)
                                            .setTitle("You have already purchased this set")
                                            .setNeutralButton("Ok", null)
                                            .create()
                                            .show()
                                }
                            })
                        }
                    }
                }
            }
            container.addView(this)
            return this
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any)
            = container.removeView(view as View)

    override fun getCount()
            = StickersBoardPageEnum.values().size

    override fun isViewFromObject(view: View, `object`: Any)
            = view === `object`

    override fun getPageTitle(position: Int)
            = context.getString(StickersBoardPageEnum.values()[position].titleResId) ?: ""
    //endregion

    //region OnStickerSelectedListener
    private var listener: OnStickerSelectedListener? = null

    fun setOnStickerSelectedListener(listener: OnStickerSelectedListener) {
        this.listener = listener
    }

    override fun onStickerSelected(sticker: Sticker)
            = listener?.onStickerSelected(sticker) ?: Unit

    override fun onStickerClick(adapterPosition: Int, sticker: Sticker)
            = onStickerSelected(sticker)
    //endregion
}