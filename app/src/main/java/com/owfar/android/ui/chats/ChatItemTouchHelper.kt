package com.owfar.android.ui.chats

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

class ChatItemTouchHelper : ItemTouchHelper.Callback() {

    private var listener: OnSwipeListener? = null

    fun setOnSwipeListener(listener: OnSwipeListener) {
        this.listener = listener
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
            ItemTouchHelper.Callback.makeMovementFlags(0, when (viewHolder) {
                is ChatItemHolder -> ItemTouchHelper.START
                else -> 0
            })

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is ChatItemHolder)
            listener?.onSwiped(viewHolder.adapterPosition)
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder) {
        ItemTouchHelper.Callback.getDefaultUIUtil()
                .clearView((viewHolder as? ChatItemHolder)?.removableView)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        ItemTouchHelper.Callback.getDefaultUIUtil()
                .onSelected((viewHolder as? ChatItemHolder)?.removableView)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                             actionState: Int, isCurrentlyActive: Boolean) {
        ItemTouchHelper.Callback.getDefaultUIUtil()
                .onDraw(c, recyclerView, (viewHolder as ChatItemHolder).removableView, dX, dY,
                        actionState, isCurrentlyActive)
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView,
                                 viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                                 actionState: Int, isCurrentlyActive: Boolean) {
        ItemTouchHelper.Callback.getDefaultUIUtil()
                .onDrawOver(c, recyclerView, (viewHolder as ChatItemHolder).removableView, dX, dY,
                        actionState, isCurrentlyActive)
    }
}