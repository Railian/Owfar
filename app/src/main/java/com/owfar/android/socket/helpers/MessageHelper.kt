package com.owfar.android.socket.helpers

import com.google.gson.JsonObject
import com.owfar.android.InvalidTokenException
import com.owfar.android.models.api.classes.ReceivedMessage
import com.owfar.android.models.api.classes.SentMessage
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.enums.MessageBodyType
import com.owfar.android.models.api.enums.MessageStatus
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.socket.DeliveredInfo
import com.owfar.android.models.socket.SeenInfo
import com.owfar.android.settings.CurrentUserSettings
import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener
import io.socket.client.Socket
import java.util.*

object MessageHelper : BaseSocketHelper() {

    //region constants
    const val EMITTER_EVENT = "message"
    const val LISTENER_EVENT_FAILED = "$EMITTER_EVENT.failed"
    const val LISTENER_EVENT_SUCCESS = "$EMITTER_EVENT.success"
    const val LISTENER_EVENT_BROADCAST = "$EMITTER_EVENT.broadcast"
    const val LISTENER_EVENT_DELIVERED = "$EMITTER_EVENT.delivered"
    const val LISTENER_EVENT_SEEN = "$EMITTER_EVENT.seen"
    const val LISTENER_EVENT_DELETE = "delete.$EMITTER_EVENT"
    //endregion

    //region Requests
    @Throws(InvalidTokenException::class)
    fun sendComment(socket: Socket, streamType: StreamType, streamId: Long, comment: String) = SentMessage().apply {
        val userId = CurrentUserSettings.currentUser?.id ?: throw InvalidTokenException()
        val timestamp = System.currentTimeMillis()
        this.sid = "${streamType}_${streamId}_from_${userId}_at_${timestamp}"
        this.streamType = streamType.jsonName
        this.streamId = streamId
        this.bodyType = MessageBodyType.COMMENT.jsonName
        this.content = comment
        this.createdAt = Date(timestamp)
        this.user = CurrentUserSettings.currentUser ?: throw InvalidTokenException()
        this.messageStatus = MessageStatus.SENDING.jsonName
    }.apply {
        socket.emit(EMITTER_EVENT, convertToJSONObject(this))
        logArg(EMITTER_EVENT, this)
    }

    @Throws(InvalidTokenException::class)
    fun sendSticker(socket: Socket, streamType: StreamType, streamId: Long, sticker: Sticker) = SentMessage().apply {
        val userId = CurrentUserSettings.currentUser?.id ?: throw InvalidTokenException()
        val timestamp = System.currentTimeMillis()
        this.sid = "${streamType}_${streamId}_from_${userId}_at_${timestamp}"
        this.streamType = streamType.jsonName
        this.streamId = streamId
        this.bodyType = MessageBodyType.STICKER.jsonName
        this.createdAt = Date(timestamp)
        this.user = CurrentUserSettings.currentUser ?: throw InvalidTokenException()
        this.messageStatus = MessageStatus.SENDING.jsonName
        this.sticker = sticker
    }.apply {
        socket.emit(EMITTER_EVENT, convertToJSONObject(this))
        logArg(EMITTER_EVENT, this)
    }
    //endregion

    //region Listeners
    class FailedListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_FAILED, args)
            callback.onMessageFailed()
        }
    }

    class SuccessListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_SUCCESS, args)
            callback.onMessageSuccess()
        }
    }

    class BroadcastListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_BROADCAST, args)
            convertFirstArgFromJson(ReceivedMessage::class.java, args)?.let {
                callback.onMessageBroadcast(it)
            }
        }
    }

    class DeliveredListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_DELIVERED, args)
            convertFirstArgFromJson(DeliveredInfo::class.java, args)?.let {
                callback.onMessageDelivered(it)
            }
        }
    }

    class SeenListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_SEEN, args)
            convertFirstArgFromJson(SeenInfo::class.java, args)?.let {
                callback.onMessagesSeen(it)
            }
        }
    }

    class DeleteListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_DELETE, args)
            convertFirstArgFromJson(JsonObject::class.java, args)?.let {
                it.get("id")?.asLong?.let { callback.onMessageDeleted(it) }
            }
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onMessageFailed()
        fun onMessageSuccess()
        fun onMessageBroadcast(message: ReceivedMessage)
        fun onMessageDelivered(info: DeliveredInfo)
        fun onMessagesSeen(info: SeenInfo)
        fun onMessageDeleted(messageId: Long)
    }
    //endregion
}