package com.owfar.android.socket.helpers

import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.socket.SubscribeData
import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener
import io.socket.client.Socket

object UnsubscribeHelper : BaseSocketHelper() {

    //region constants
    const val EMITTER_EVENT = "unsubscribe"
    const val LISTENER_EVENT = EMITTER_EVENT
    //endregion

    //region Requests
    fun unsubscribe(socket: Socket, streamType: StreamType, streamId: Long?) = SubscribeData().apply {
        this.streamType = streamType.jsonName
        this.streamId = streamId
    }.let { unsubscribe(socket, it) }

    fun unsubscribe(socket: Socket, data: SubscribeData) = data.apply {
        socket.emit(EMITTER_EVENT, convertToJSONObject(this))
        logArg(EMITTER_EVENT, this)
    }
    //endregion

    //region Listeners
    class Listener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT, args)
            convertFirstArgFromJson(SubscribeData::class.java, args)?.let {
                callback.onUnsubscribed(it)
            }
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onUnsubscribed(data: SubscribeData)
    }
    //endregion
}
