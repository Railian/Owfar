package com.owfar.android.socket.helpers

import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener

object ConnectHelper : BaseSocketHelper() {

    //region constants
    const val LISTENER_EVENT = "connect"
    //endregion

    //region Listeners
    class Listener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT, args)
            callback.onSocketConnected()
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onSocketConnected()
    }
    //endregion
}
