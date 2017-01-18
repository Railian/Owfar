package com.owfar.android.socket

import com.owfar.android.models.api.classes.Media
import com.owfar.android.models.api.classes.ReceivedMessage
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.socket.DeliveredInfo
import com.owfar.android.models.socket.SeenInfo
import com.owfar.android.models.socket.SubscribeData
import com.owfar.android.socket.helpers.*
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketManager {

    //region constants
    @Suppress("HasPlatformType") @JvmStatic val TAG = SocketManager::class.java.simpleName
    const private val BASE_SOCKET_URL = "http://52.35.131.155:3000"
    //endregion

    //region fields
    private var socket: Socket? = null
    var listener: SocketListener? = null
    //endregion

    //region Initialisation
    fun initSocket(): Socket? {
        socket = IO.socket(BASE_SOCKET_URL, IO.Options().apply { forceNew = true }).apply {
            on(ConnectHelper.LISTENER_EVENT, ConnectHelper.Listener(connectCallback))
            on(MessageHelper.LISTENER_EVENT_FAILED, MessageHelper.FailedListener(messageCallback))
            on(MessageHelper.LISTENER_EVENT_SUCCESS, MessageHelper.SuccessListener(messageCallback))
            on(MessageHelper.LISTENER_EVENT_BROADCAST, MessageHelper.BroadcastListener(messageCallback))
            on(MessageHelper.LISTENER_EVENT_DELIVERED, MessageHelper.DeliveredListener(messageCallback))
            on(MessageHelper.LISTENER_EVENT_SEEN, MessageHelper.SeenListener(messageCallback))
            on(MessageHelper.LISTENER_EVENT_DELETE, MessageHelper.DeleteListener(messageCallback))
            on(SubscribeHelper.LISTENER_EVENT, SubscribeHelper.Listener(subscribeCallback))
            on(UnsubscribeHelper.LISTENER_EVENT, UnsubscribeHelper.Listener(unsubscribeCallback))
            on(AuthenticateHelper.LISTENER_EVENT_FAILED, AuthenticateHelper.FailedListener(authenticateCallback))
            on(AuthenticateHelper.LISTENER_EVENT_SUCCESS, AuthenticateHelper.SuccessListener(authenticateCallback))
            on(ChatHelper.LISTENER_EVENT_CREATE, ChatHelper.CreateListener(chatCallback))
            on(ChatHelper.LISTENER_EVENT_UPDATE, ChatHelper.UpdateListener(chatCallback))
            on(ChatHelper.LISTENER_EVENT_INVITE, ChatHelper.InviteListener(chatCallback))
            on(ChatHelper.LISTENER_EVENT_UPLOAD_PHOTO, ChatHelper.UploadPhotoListener(chatCallback))
            on(ChatHelper.LISTENER_EVENT_LEAVE, ChatHelper.LeaveListener(chatCallback))
            on(ChatHelper.LISTENER_EVENT_DELETE, ChatHelper.DeleteListener(chatCallback))
            on(RoleHelper.LISTENER_EVENT, RoleHelper.Listener(roleCallback))
            on(ChargeHelper.LISTENER_EVENT_FAILED, ChargeHelper.FailedListener(chargeCallback))
            on(ChargeHelper.LISTENER_EVENT_SUCCEEDED, ChargeHelper.SucceededListener(chargeCallback))
            on(StickersGroupsHelper.LISTENER_EVENT_CREATE, StickersGroupsHelper.CreateListener(stickersGroupsCallback))
            on(StickersGroupsHelper.LISTENER_EVENT_UPDATE, StickersGroupsHelper.UpdateListener(stickersGroupsCallback))
            on(StickersGroupsHelper.LISTENER_EVENT_DELETE, StickersGroupsHelper.DeleteListener(stickersGroupsCallback))
            on(StickersHelper.LISTENER_EVENT_CREATE, StickersHelper.CreateListener(stickersCallback))
            on(StickersHelper.LISTENER_EVENT_DELETE, StickersHelper.DeleteListener(stickersCallback))
        }
        return socket
    }

    fun releaseSocket() = disconnect()?.apply {
        off()
        socket = null
    }
    //endregion

    //region Public Tools
    val isConnected: Boolean
        get() = socket?.connected() ?: false

    fun connect() = socket?.apply {
        if (isConnected) disconnect()
        connect()
    }

    fun disconnect()
            = socket?.disconnect()

    fun resetSocketIfNull() = socket ?: try {
        initSocket()?.apply { connect() }
    } catch (e: URISyntaxException) {
        e.printStackTrace()
        null
    }
    //endregion

    //region Requests
    fun sendComment(streamType: StreamType, streamId: Long, comment: String) =
            resetSocketIfNull()?.let { MessageHelper.sendComment(it, streamType, streamId, comment) }

    fun sendSticker(streamType: StreamType, streamId: Long, sticker: Sticker) =
            resetSocketIfNull()?.let { MessageHelper.sendSticker(it, streamType, streamId, sticker) }
    //endregion

    //region Callbacks
    private val connectCallback = object : ConnectHelper.Callback {
        override fun onSocketConnected() = resetSocketIfNull()?.let {
            AuthenticateHelper.authenticate(it)
            listener?.onConnected()
        } ?: Unit
    }

    private val messageCallback = object : MessageHelper.Callback {
        override fun onMessageFailed() = listener?.onMessageFailed() ?: Unit
        override fun onMessageSuccess() = listener?.onMessageSent() ?: Unit
        override fun onMessageBroadcast(message: ReceivedMessage) = listener?.onMessageReceived(message) ?: Unit
        override fun onMessageDelivered(info: DeliveredInfo) = listener?.onMessageDelivered(info) ?: Unit
        override fun onMessagesSeen(info: SeenInfo) = listener?.onMessagesSeen(info) ?: Unit
        override fun onMessageDeleted(messageId: Long) = listener?.onMessageRemoved(messageId) ?: Unit
    }

    private val subscribeCallback = object : SubscribeHelper.Callback {
        override fun onSubscribed(data: SubscribeData) = resetSocketIfNull()?.let {
            SubscribeHelper.subscribe(it, data)
            listener?.onSubscribed(data)
        } ?: Unit
    }

    private val unsubscribeCallback = object : UnsubscribeHelper.Callback {
        override fun onUnsubscribed(data: SubscribeData) = resetSocketIfNull()?.let {
            UnsubscribeHelper.unsubscribe(it, data)
            listener?.onUnsubscribed(data)
        } ?: Unit
    }

    private val authenticateCallback = object : AuthenticateHelper.Callback {
        override fun onAuthenticateFailed() = Unit
        override fun onAuthenticateSuccess() = Unit
    }

    private val chatCallback = object : ChatHelper.Callback {
        override fun onChatCreated(chat: Stream) = listener?.onChatCreated(chat) ?: Unit
        override fun onChatUpdated(chatId: Long, name: String) = listener?.onChatUpdated(chatId, name) ?: Unit
        override fun onInvitedToChat(chat: Stream) = listener?.onInvitedToChat(chat) ?: Unit
        override fun onUploadedPhotoToChat(chatId: Long, media: Media) = listener?.onUploadedPhotoToChat(chatId, media) ?: Unit
        override fun onLeftChat(chatId: Long, userId: Long) = listener?.onLeftChat(chatId, userId) ?: Unit
        override fun onChatDeleted(chatId: Long) = listener?.onChatDeleted(chatId) ?: Unit
    }

    private val roleCallback = object : RoleHelper.Callback {
        override fun onRoleChanged() = Unit
    }

    private val chargeCallback = object : ChargeHelper.Callback {
        override fun onChargeFailed() = Unit
        override fun onChargeSucceeded() = Unit
    }

    private val stickersGroupsCallback = object : StickersGroupsHelper.Callback {
        override fun onStickersGroupCreated() = listener?.onStickersGroupCreated() ?: Unit
        override fun onStickersGroupUpdated() = listener?.onStickersGroupUpdated() ?: Unit
        override fun onStickersGroupDeleted() = listener?.onStickersGroupDeleted() ?: Unit
    }

    private val stickersCallback = object : StickersHelper.Callback {
        override fun onStickerCreated() = listener?.onStickerCreated() ?: Unit
        override fun onStickerDeleted() = listener?.onStickerDeleted() ?: Unit
    }
    //endregion
}