package com.owfar.android.socket

import com.owfar.android.models.api.classes.Media
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.models.socket.DeliveredInfo
import com.owfar.android.models.socket.SeenInfo
import com.owfar.android.models.socket.SubscribeData

interface SocketListener {

    fun onConnected()
    fun onMessageFailed()
    fun onMessageSent()
    fun onMessageReceived(message: Message)
    fun onMessageDelivered(info: DeliveredInfo)
    fun onMessagesSeen(info: SeenInfo)
    fun onMessageRemoved(messageId: Long)
    fun onSubscribed(data: SubscribeData)
    fun onUnsubscribed(data: SubscribeData)
    fun onChatCreated(chat: Stream)
    fun onChatUpdated(chatId: Long, name: String)
    fun onInvitedToChat(chat: Stream)
    fun onUploadedPhotoToChat(chatId: Long, photo: Media)
    fun onLeftChat(chatId: Long, userId: Long)
    fun onChatDeleted(chatId: Long)
    fun onStickersGroupCreated()
    fun onStickersGroupUpdated()
    fun onStickersGroupDeleted()
    fun onStickerCreated()
    fun onStickerDeleted()

    open class Simple : SocketListener {
        override fun onConnected() = Unit
        override fun onMessageFailed() = Unit
        override fun onMessageSent() = Unit
        override fun onMessageReceived(message: Message) = Unit
        override fun onMessageDelivered(info: DeliveredInfo) = Unit
        override fun onMessagesSeen(info: SeenInfo) = Unit
        override fun onMessageRemoved(messageId: Long) = Unit
        override fun onSubscribed(data: SubscribeData) = Unit
        override fun onUnsubscribed(data: SubscribeData) = Unit
        override fun onChatCreated(chat: Stream) = Unit
        override fun onChatUpdated(chatId: Long, name: String) = Unit
        override fun onInvitedToChat(chat: Stream) = Unit
        override fun onUploadedPhotoToChat(chatId: Long, photo: Media) = Unit
        override fun onLeftChat(chatId: Long, userId: Long) = Unit
        override fun onChatDeleted(chatId: Long) = Unit
        override fun onStickersGroupCreated() = Unit
        override fun onStickersGroupUpdated() = Unit
        override fun onStickersGroupDeleted() = Unit
        override fun onStickerCreated() = Unit
        override fun onStickerDeleted() = Unit
    }
}