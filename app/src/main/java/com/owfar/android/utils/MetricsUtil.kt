/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.owfar.android.utils

import android.content.Context
import android.graphics.Point
import android.util.TypedValue
import android.view.View
import android.view.WindowManager

class MetricsUtil(private val context: Context) {

    companion object {

        fun dp2px(context: Context, dp: Float) = context.resources?.displayMetrics?.let { metrics ->
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
        }

        fun sp2px(context: Context, sp: Float) = context.resources?.displayMetrics?.let { metrics ->
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics).toInt()
        }

        fun getNavigationBarHeight(context: Context) = context.resources?.let { res ->
            res.getIdentifier("navigation_bar_height", "dimen", "android").let {
                if (it > 0) res.getDimensionPixelSize(it) else null
            }
        } ?: 0

        fun getStatusBarHeight(context: Context) = context.resources?.let { res ->
            res.getIdentifier("status_bar_height", "dimen", "android").let {
                if (it > 0) res.getDimensionPixelSize(it) else null
            }
        } ?: 0

        fun getScreenSize(context: Context)
                = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                ?.defaultDisplay?.let { display ->
            Size(Point().apply { display.getSize(this) })
        }

        fun getRelativeLeft(view: View, rootView: View): Int = view.left +
                if (view.parent !== rootView) getRelativeLeft(view.parent as View, rootView) else 0

        fun getRelativeTop(view: View, rootView: View): Int = view.top +
                if (view.parent !== rootView) getRelativeTop(view.parent as View, rootView) else 0
    }

    fun dp2px(dp: Float) = dp2px(context, dp)
    fun sp2px(sp: Float) = sp2px(context, sp)

    val navigationBarHeight: Int
        get() = getNavigationBarHeight(context)

    val statusBarHeight: Int
        get() = getStatusBarHeight(context)

    val screenSize: Size?
        get() = getScreenSize(context)

    data class Size(val width: Int, val height: Int) {
        constructor(size: Point) : this(size.x, size.y)
    }
}
