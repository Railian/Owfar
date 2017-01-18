package com.owfar.android.socket.helpers

import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener

object ChargeHelper : BaseSocketHelper() {

    //region constants
    const private val EVENT_PREFIX = "charge"
    const val LISTENER_EVENT_FAILED = "$EVENT_PREFIX.failed"
    const val LISTENER_EVENT_SUCCEEDED = "$EVENT_PREFIX.succeeded"
    //endregion

    //region Listeners
    class FailedListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_FAILED, args)
            callback.onChargeFailed()
        }
    }

    class SucceededListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_SUCCEEDED, args)
            callback.onChargeSucceeded()
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onChargeFailed()
        fun onChargeSucceeded()
    }
    //endregion
}
