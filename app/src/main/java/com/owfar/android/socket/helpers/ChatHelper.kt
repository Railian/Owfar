package com.owfar.android.socket.helpers

import com.google.gson.JsonObject
import com.owfar.android.models.api.classes.Media
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.socket.BaseSocketHelper
import com.owfar.android.socket.BaseSocketListener

object ChatHelper : BaseSocketHelper() {

    //region constants
    const private val EVENT_PREFIX = "chat"
    const val LISTENER_EVENT_CREATE = "$EVENT_PREFIX.create"
    const val LISTENER_EVENT_UPDATE = "$EVENT_PREFIX.update"
    const val LISTENER_EVENT_INVITE = "$EVENT_PREFIX.invite"
    const val LISTENER_EVENT_UPLOAD_PHOTO = "$EVENT_PREFIX.uploadPhoto"
    const val LISTENER_EVENT_LEAVE = "$EVENT_PREFIX.leave"
    const val LISTENER_EVENT_DELETE = "$EVENT_PREFIX.delete"
    //endregion

    //region Listeners
    class CreateListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_CREATE, args)
            convertFirstArgFromJson(Stream::class.java, args)?.apply {
                sid = Stream.generateSid(StreamType.CHATS, id)
                type = StreamType.CHATS.jsonName
                callback.onChatCreated(this)
            }
        }
    }

    class UpdateListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_UPDATE, args)
            convertFirstArgFromJson(JsonObject::class.java, args)?.apply {
                val chatId = get("chatId")?.asLong
                val name = get("name")?.asString
                if (chatId != null && name != null)
                    callback.onChatUpdated(chatId, name)
            }
        }
    }

    class InviteListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_INVITE, args)
            convertFirstArgFromJson(Stream::class.java, args)?.apply {
                sid = Stream.generateSid(StreamType.CHATS, id)
                type = StreamType.CHATS.jsonName
                callback.onInvitedToChat(this)
            }
        }
    }

    class UploadPhotoListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_UPLOAD_PHOTO, args)
            convertFirstArgFromJson(JsonObject::class.java, args)?.apply {
                val chatId = get("chatId")?.asLong
                val mediaFile = get("media_file")?.let {
                    if (!isJsonObject) null
                    else asJsonObject?.let { convertFromJson(it, Media::class.java) }
                }
                if (chatId != null && mediaFile != null)
                    callback.onUploadedPhotoToChat(chatId, mediaFile)
            }
        }
    }

    class LeaveListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_LEAVE, args)
            convertFirstArgFromJson(JsonObject::class.java, args)?.apply {
                val chatId = get("chatId")?.asLong
                val userId = get("userId")?.asLong
                if (chatId != null && userId != null)
                    callback.onLeftChat(chatId, userId)
            }
        }
    }

    class DeleteListener(callback: Callback) : BaseSocketListener<Callback>(callback) {
        override fun call(vararg args: Any) {
            logArgs(LISTENER_EVENT_DELETE, args)
            convertFirstArgFromJson(JsonObject::class.java, args)?.apply {
                get("id")?.asLong?.let { callback.onChatDeleted(it) }
            }
        }
    }
    //endregion

    //region Callback
    interface Callback {
        fun onChatCreated(chat: Stream)
        fun onChatUpdated(chatId: Long, name: String)
        fun onInvitedToChat(chat: Stream)
        fun onUploadedPhotoToChat(chatId: Long, photo: Media)
        fun onLeftChat(chatId: Long, userId: Long)
        fun onChatDeleted(chatId: Long)
    }
    //endregion
}
