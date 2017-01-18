package com.owfar.android.ui.messenger

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View

open class VerticalViewPager(
        context: Context,
        attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        // The majority of the magic happens here
        setPageTransformer(true, VerticalPageTransformer())
        // The easiest way to get rid of the overScroll drawing that happens on the left and right
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    private inner class VerticalPageTransformer : ViewPager.PageTransformer {

        override fun transformPage(view: View, position: Float) {
            when (position) {
                in -1..1 -> {
                    view.alpha = 1f
                    // Counteract the default slide transition
                    view.translationX = view.width * -position
                    //set Y position to swipe in from top
                    val yPosition = position * view.height
                    view.translationY = yPosition
                }
                else -> view.alpha = 0f
            }
        }
    }
}