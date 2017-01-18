package com.owfar.android.ui.messenger

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View

class ContextMenuRecyclerView(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
) : RecyclerView(context, attrs, defStyle) {

    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var contextMenuInfo: RecyclerContextMenuInfo? = null

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return contextMenuInfo
    }

    override fun showContextMenuForChild(originalView: View): Boolean {
        try {
            getChildAdapterPosition(originalView).let { longPressPosition ->
                if (longPressPosition >= 0) {
                    val longPressId = adapter.getItemId(longPressPosition)
                    contextMenuInfo = RecyclerContextMenuInfo(longPressPosition, longPressId)
                    return super.showContextMenuForChild(originalView)
                } else return false
            }
        } catch (e: Exception) {
            return false
        }
    }

    class RecyclerContextMenuInfo(val position: Int, val id: Long) : ContextMenu.ContextMenuInfo
}