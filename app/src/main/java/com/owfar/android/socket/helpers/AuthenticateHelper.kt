package com.owfar.android.socket.helpers

import com.owfar.android.models.socket.AuthenticateData
import com.owfar.android.settings.CurrentUserSettings
import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener
import io.socket.client.Socket

object AuthenticateHelper : BaseSocketHelper() {

    //region constants
    const val EMITTER_EVENT = "authenticate"
    const val LISTENER_EVENT_FAILED = "$EMITTER_EVENT.failed"
    const val LISTENER_EVENT_SUCCESS = "$EMITTER_EVENT.success"
    //endregion

    //region Requests
    fun authenticate(socket: Socket) = AuthenticateData().apply {
        token = CurrentUserSettings.accessToken
        deviceToken = null
    }.apply {
        socket.emit(EMITTER_EVENT, convertToJSONObject(this))
        logArg(EMITTER_EVENT, this)
    }
    //endregion

    //region Listeners
     class FailedListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_FAILED, args)
            callback.onAuthenticateFailed()
        }
    }

    class SuccessListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_SUCCESS, args)
            callback.onAuthenticateSuccess()
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onAuthenticateFailed()
        fun onAuthenticateSuccess()
    }
    //endregion
}
