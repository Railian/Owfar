package com.owfar.android.socket.helpers

import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener

object StickersHelper : BaseSocketHelper() {

    //region constants
    const private val EVENT_PREFIX = "stickers"
    const val LISTENER_EVENT_CREATE = "$EVENT_PREFIX.create"
    const val LISTENER_EVENT_DELETE = "$EVENT_PREFIX.delete"
    //endregion

    //region Listeners
    class CreateListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_CREATE, args)
            callback.onStickerCreated()
        }
    }

    class DeleteListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_DELETE, args)
            callback.onStickerDeleted()
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onStickerCreated()
        fun onStickerDeleted()
    }
    //endregion
}