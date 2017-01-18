package com.owfar.android.socket.helpers

import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener

object StickersGroupsHelper : BaseSocketHelper() {

    //region constants
    const private val EVENT_PREFIX = "stickers_groups"
    const val LISTENER_EVENT_CREATE = "$EVENT_PREFIX.create"
    const val LISTENER_EVENT_UPDATE = "$EVENT_PREFIX.update"
    const val LISTENER_EVENT_DELETE = "$EVENT_PREFIX.delete"
    //endregion

    //region Listeners
    class CreateListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_CREATE, args)
            callback.onStickersGroupCreated()
        }
    }

    class UpdateListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_UPDATE, args)
            callback.onStickersGroupUpdated()
        }
    }

    class DeleteListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_DELETE, args)
            callback.onStickersGroupDeleted()
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onStickersGroupCreated()
        fun onStickersGroupUpdated()
        fun onStickersGroupDeleted()
    }
    //endregion
}
