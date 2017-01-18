package com.owfar.android.socket.helpers

import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener

object RoleHelper : BaseSocketHelper() {

    //region constants
    const private val EVENT_PREFIX = "role"
    const val LISTENER_EVENT = "$EVENT_PREFIX.change"
    //endregion

    //region Listeners
    class Listener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT, args)
            callback.onRoleChanged()
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onRoleChanged()
    }
    //endregion
}
