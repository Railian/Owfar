package com.owfar.android.ui.boards.stickers

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import com.owfar.android.ui.messenger.VerticalViewPager

class ViewPagerNotScrollable(context: Context, attrs: AttributeSet?)
    : VerticalViewPager(context, attrs) {

    constructor(context: Context) : this(context, null)

    override fun onTouchEvent(ev: MotionEvent) = false
    override fun onInterceptTouchEvent(event: MotionEvent) = false
}