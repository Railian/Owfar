package com.owfar.android.ui.boards.stickers

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.owfar.android.R
import com.owfar.android.models.api.StickerList
import com.owfar.android.models.api.classes.Sticker

class StickersBoard(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : LinearLayout(context, attrs, defStyleAttr), OnStickerSelectedListener {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    companion object {
        const private val STATE_SUPER = "STATE_SUPER"
        const private val STATE_CURRENT_ITEM = "STATE_CURRENT_ITEM"
    }

    //region widgets
    private val vpnsStickersBoardPager: ViewPagerNotScrollable
    private val pageButtons: Array<ImageView>
    private val adapter: StickersBoardPageAdapter
        get() = vpnsStickersBoardPager.adapter as StickersBoardPageAdapter
    //endregion

    //region Initialization
    init {
        LayoutInflater.from(context).inflate(R.layout.board_stickers, this, true)

        vpnsStickersBoardPager = findViewById(R.id.board_stickers_vpnsStickersBoardPager) as ViewPagerNotScrollable
        pageButtons = arrayOf(
                findViewById(R.id.board_stickers_ivRecent) as ImageView,
                findViewById(R.id.board_stickers_ivMyStickers) as ImageView,
                findViewById(R.id.board_stickers_ivMyAnimations) as ImageView,
                findViewById(R.id.board_stickers_ivPremium) as ImageView,
                findViewById(R.id.board_stickers_iv5thTab) as ImageView
        )
        vpnsStickersBoardPager.adapter = StickersBoardPageAdapter(context).apply {
            setOnStickerSelectedListener(this@StickersBoard)
        }
        pageButtons.forEachIndexed { i, button ->
            button.setOnClickListener {
                vpnsStickersBoardPager.setCurrentItem(i, true)
                updatePageButtons()
            }
        }
        updatePageButtons()
    }
    //endregion

    //region Saving & Restoring
    override fun onSaveInstanceState() = Bundle().apply {
        putParcelable(STATE_SUPER, super.onSaveInstanceState())
        putInt(STATE_CURRENT_ITEM, vpnsStickersBoardPager.currentItem)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState((state as Bundle).getParcelable<Parcelable>(STATE_SUPER))
        vpnsStickersBoardPager.setCurrentItem(state.getInt(STATE_CURRENT_ITEM), false)
        updatePageButtons()
    }
    //endregion

    //region DataSource Methods
    fun setStickerList(stickerList: StickerList)
            = adapter.setStickerList(stickerList)
    //endregion

    //region OnStickerSelectedListener
    private var listener: OnStickerSelectedListener? = null

    fun setOnStickerSelectedListener(listener: OnStickerSelectedListener) {
        this.listener = listener
    }

    override fun onStickerSelected(sticker: Sticker)
            = listener?.onStickerSelected(sticker) ?: Unit
    //endregion

    //region Private Tools
    private fun updatePageButtons() = pageButtons.forEachIndexed { i, button ->
        button.alpha = if (i == vpnsStickersBoardPager.currentItem) 1f else .5f
    }
    //endregion
}
