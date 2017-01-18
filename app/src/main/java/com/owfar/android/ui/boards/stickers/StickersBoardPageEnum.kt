package com.owfar.android.ui.boards.stickers

import com.owfar.android.R

enum class StickersBoardPageEnum(val iconResId: Int, val titleResId: Int, val layoutResId: Int) {

    RECENT(
            R.drawable.ic_stickers_board_page_recent,
            R.string.stickers_board_page_recent,
            R.layout.page_stickers_board_recent
    ),
    MY_STICKERS(
            R.drawable.ic_stickers_board_page_my_stickers,
            R.string.stickers_board_page_my_stickers,
            R.layout.page_stickers_board_my_stickers
    ),
    MY_ANIMATIONS(
            R.drawable.ic_stickers_board_page_my_animations,
            R.string.stickers_board_page_my_animations,
            R.layout.page_stickers_board_my_animations
    ),
    PREMIUM(
            R.drawable.ic_stickers_board_page_premium,
            R.string.stickers_board_page_premium,
            R.layout.page_stickers_board_premium
    ),
    _5TH_TAB(
            R.drawable.ic_stickers_board_page_5th_tab,
            R.string.stickers_board_page_5th_tab,
            R.layout.page_stickers_board_5th_tab
    )
}