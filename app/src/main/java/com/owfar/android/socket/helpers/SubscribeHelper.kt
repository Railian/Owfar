package com.owfar.android.socket.helpers

import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.socket.SubscribeData
import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener
import io.socket.client.Socket

object SubscribeHelper : BaseSocketHelper() {

    //region constants
    const val EMITTER_EVENT = "subscribe"
    const val LISTENER_EVENT = EMITTER_EVENT
    //endregion

    //region Requests
    fun subscribe(socket: Socket, streamType: StreamType, streamId: Long?) = SubscribeData().apply {
        this.streamType = streamType.jsonName
        this.streamId = streamId
    }.let { subscribe(socket, it) }

    fun subscribe(socket: Socket, data: SubscribeData) = data.apply {
        socket.emit(EMITTER_EVENT, convertToJSONObject(this))
        logArg(EMITTER_EVENT, this)
    }
    //endregion

    //region Listeners
    class Listener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT, args)
            convertFirstArgFromJson(SubscribeData::class.java, args)?.let {
                callback.onSubscribed(it)
            }
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onSubscribed(data: SubscribeData)
    }
    //endregion
}
