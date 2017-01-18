package com.owfar.android.data

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.owfar.android.DelegatesSet
import com.owfar.android.R
import com.owfar.android.api.users.UsersDelegate
import com.owfar.android.api.users.UsersManager
import com.owfar.android.models.api.classes.*
import com.owfar.android.models.api.enums.MessageBodyType
import com.owfar.android.models.api.enums.MessageStatus
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.models.socket.DeliveredInfo
import com.owfar.android.models.socket.SeenInfo
import com.owfar.android.models.socket.SubscribeData
import com.owfar.android.settings.CurrentUserSettings
import com.owfar.android.socket.SocketListener
import com.owfar.android.socket.SocketManager
import com.owfar.android.ui.main.MainActivity
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import kotlin.reflect.KFunction


object DataManager {

    //region constants
    @JvmStatic val TAG = DataManager::class.java.simpleName

    const private val API_REQUEST_GET_STREAMS = 1
    const private val API_REQUEST_GET_STREAM = 2
    const private val API_REQUEST_GET_MESSAGE = 3
    const private val API_REQUEST_SET_MESSAGE_STATUS = 4
    //endregion

    //region DelegatesSet
    val delegatesSet = DelegatesSet(DataDelegate::class.java)
    private var context: Context? = null
    //endregion

    fun init(appContext: Context) = let { context = appContext }

    private val realm: Realm
        get() = Realm.getDefaultInstance()

    //region DataSource
    var activeStream: Stream? = null
//    val stickersGroups: MutableList<StickersGroup>?
//        get() = realm.where(StickersGroup::class.java).findAll()
//    var recentStickers: MutableList<Sticker>? = null
//        get() = realm.where(Sticker::class.java).equalTo("recent", true).findAll()
//    var interestsGroups: MutableList<InterestsGroup>? = null
//        get() = realm.where(InterestsGroup::class.java).findAll()
    //endregion

    //region Requests
    fun initStreams() {
        delegatesSet.notify().onStreamsUpdated(streamsForList)
        UsersManager.getStreams(TAG)
    }

    private val streamsForList: MutableList<Stream>
        get() = realm.copyFromRealm(allStreams).apply {
            forEach { stream ->
                stream.allMessages = realm.copyFromRealm(realm.where(ReceivedMessage::class.java)
                        .equalTo("streamType", stream.type)
                        .equalTo("streamId", stream.id)
                        .findAllSorted("createdAt", Sort.DESCENDING)
                        .filterIndexed { i, receivedMessage -> i == 0 })
            }
            sortByDescending { it.lastUpdatedAt }
        }

    fun getStreamForMessenger(stream: Stream): Stream? =
            realm.copyFromRealm(allStreams?.find { it.sid == stream.sid })?.apply {
                allMessages = realm.copyFromRealm(realm.where(ReceivedMessage::class.java)
                        .equalTo("streamType", stream.type)
                        .equalTo("streamId", stream.id)
                        .findAllSorted("createdAt", Sort.DESCENDING)
                        .filterIndexed { i, receivedMessage -> i < 20 })
            }


    private val allStreams: RealmResults<Stream>?
        get() = realm.where(Stream::class.java).findAll()

    private fun resetStreams(streams: MutableList<Stream>?) {
        realm.executeTransaction {
            val oldSids = allStreams?.mapNotNull { it.sid }?.toSet() ?: emptySet<String>()
            val newSids = streams?.mapNotNull { it.sid }?.toSet() ?: emptySet<String>()
            val deprecatedSids = oldSids - newSids
            streams?.forEach { stream ->
                it.copyToRealmOrUpdate(stream.receivedMessages)
                it.copyToRealmOrUpdate(stream)
            }
            allStreams?.forEach { if (deprecatedSids.contains(it.sid)) it.deleteFromRealm() }
        }
        updateUnreadMessages(context, silent = true, popup = false)
        delegatesSet.notify().onStreamsUpdated(streamsForList)
    }

    fun getDraft(streamSid: String) = realm.where(Draft::class.java)
            .equalTo("streamSid", streamSid)
            .findFirst()
            ?.let { realm.copyFromRealm(it) }

    fun setDraft(streamSid: String, text: String?) = realm.executeTransaction {
        if (text.isNullOrBlank()) it.where(Draft::class.java)
                .equalTo("streamSid", streamSid)
                .findFirst()
                ?.deleteFromRealm()
        else it.copyToRealmOrUpdate(Draft(streamSid, text))
    }

    fun addOrUpdateStream(streamType: StreamType, streamId: Long)
            = UsersManager.getStreamById(TAG, API_REQUEST_GET_STREAM, streamType, streamId)

    fun addOrUpdateStream(stream: Stream) {
        realm.executeTransaction { it.copyToRealmOrUpdate(stream) }
        delegatesSet.notify().onStreamsUpdated(streamsForList)
    }

    fun setStreamAsRead(stream: Stream) {
        realm.executeTransaction {
            allStreams?.find { it.sid == stream.sid }?.let {
                it.unreadCount = 0
                realm.where(ReceivedMessage::class.java)
                        .equalTo("streamType", it.type)
                        .equalTo("streamId", it.id)
                        .findAllSorted("createdAt", Sort.DESCENDING)
                        ?.firstOrNull()
                        ?.let { UsersManager.setMessageStatus(TAG, null, it.id, MessageStatus.SEEN) }
                updateUnreadMessages(context, silent = true, popup = true)
            }
        }
    }

    fun removeStream(stream: Stream) {
        realm.executeTransaction { allStreams?.find { it.sid == stream.sid }?.deleteFromRealm() }
        delegatesSet.notify().onStreamsUpdated(streamsForList)
    }

    fun removeStream(streamType: StreamType, streamId: Long) {
        realm.executeTransaction {
            allStreams?.find {
                it.type == streamType.jsonName && it.id == streamId
            }?.deleteFromRealm()
        }
        delegatesSet.notify().onStreamsUpdated(streamsForList)
    }

    fun updateChatName(chatId: Long, name: String) {
        realm.executeTransaction {
            allStreams?.find {
                it.type == StreamType.CHATS.jsonName && it.id == chatId
            }?.asChat?.name = name
        }
        delegatesSet.notify().onStreamsUpdated(streamsForList)
    }

    fun addMessage(messageId: Long)
            = UsersManager.getMessageById(TAG, API_REQUEST_GET_MESSAGE, messageId)

    fun addNewMessage(message: Message) {
        realm.executeTransaction {
            allStreams?.find { it.type == message.streamType && it.id == message.streamId }?.let { stream ->
                when (message) {
                    is SentMessage -> realm.copyToRealmOrUpdate(message)
                    is ReceivedMessage -> {
                        realm.copyToRealmOrUpdate((message))
                        val isOwnMessage = message.user?.id == CurrentUserSettings.currentUser?.id
                        val isMessageFromActiveStream = stream.sid == activeStream?.sid
                        val seen = isOwnMessage || isMessageFromActiveStream
                        if (seen) {
                            updateUnreadMessages(context, silent = isOwnMessage, popup = false)
                            UsersManager.setMessageStatus(TAG, null, message.id, MessageStatus.SEEN)
                        } else {
                            stream.unreadCount += 1
                            updateUnreadMessages(context, silent = false, popup = true)
                            UsersManager.setMessageStatus(TAG, null, message.id, MessageStatus.DELIVERED)
                        }
                    }
                }
                delegatesSet.notify().onNewMessageAdded(realm.copyFromRealm(stream), message)
                delegatesSet.notify().onStreamsUpdated(streamsForList)

            } ?: StreamType.find(message.streamType)?.let { type ->
                message.streamId?.let { id -> addOrUpdateStream(type, id) }
            }
        }
    }

    fun updateUnreadMessages(context: Context?, silent: Boolean, popup: Boolean) {

        streamsForList.filter { it.unreadCount > 0 }.let {

            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            var title: String? = null
            var message: String? = null
            var pendingIntent: PendingIntent? = null

            when (it.size) {
                0 -> {
                    notificationManager.cancel(0)
                    return
                }
                1 -> it.first().let {
                    title = it.displayName
                    message = it.ticket
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                            MainActivity.createIntentToShowMessenger(context, getStreamForMessenger(it)),
                            PendingIntent.FLAG_UPDATE_CURRENT)
                }
                else -> {
                    title = "${it.sumBy { it.unreadCount }} unread messages"
                    message = it.joinToString(prefix = "from: ") { "${it.displayName}" }
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                            MainActivity.createIntentToShowChats(context),
                            PendingIntent.FLAG_UPDATE_CURRENT)
                }
            }

            val notificationBuilder = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notifications_chats)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentInfo("${it.sumBy { it.unreadCount }}")
                    .setContentIntent(pendingIntent)
            if (!silent) notificationBuilder
                    .setLights(0x21C0C0, 1000, 2500)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            if (popup) notificationBuilder.priority = Notification.PRIORITY_MAX

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }
    }

    fun addOldMessages(messages: RealmList<Message>?) {
        realm.executeTransaction {
            messages?.groupBy {
                val streamType = StreamType.find(it.streamType)
                val streamId = it.streamId
                if (streamType == null || streamId == null) null
                else Stream.generateSid(streamType, streamId)
            }?.forEach { group ->
                allStreams?.find { it.sid == group.key }?.let { stream ->
                    group.value.forEach { message ->
                        when (message) {
                            is SentMessage -> realm.copyToRealmOrUpdate(message)
                            is ReceivedMessage -> realm.copyToRealmOrUpdate(message)
                        }
                    }
                    delegatesSet.notify().onOldMessagesAdded(realm.copyFromRealm(stream), messages)
                }
            }
        }
    }

    private fun updateMessageStatus(info: DeliveredInfo) {
        if (info.userId != null)
            realm.executeTransaction {
                realm.where(ReceivedMessage::class.java)
                        ?.equalTo("id", info.id)
                        ?.findFirst()
                        ?.let { message ->
                            message.setMessageStatus(info.userId, MessageStatus.DELIVERED)
                            delegatesSet.notify(TAG).onMessageStatusUpdated(message.id, info.userId, MessageStatus.DELIVERED)
                        }
            }
    }

    private fun updateMessageStatus(info: SeenInfo) {
        if (info.userId != null)
            realm.executeTransaction {
                info.ids?.forEach { messageId ->
                    realm.where(ReceivedMessage::class.java)
                            ?.equalTo("id", messageId)
                            ?.findFirst()
                            ?.let { message ->
                                message.setMessageStatus(info.userId, MessageStatus.SEEN)
                                delegatesSet.notify(TAG).onMessageStatusUpdated(message.id, info.userId, MessageStatus.SEEN)
                            }
                }
            }
    }

    fun setMessageAsDeleted(messageId: Long) {
        realm.executeTransaction {
            realm.where(ReceivedMessage::class.java)
                    .equalTo("id", messageId)
                    .findFirst()?.let {
                it.bodyType == MessageBodyType.DELETED.jsonName
                delegatesSet.notify().onMessageDeleted(messageId)
                delegatesSet.notify().onStreamsUpdated(streamsForList)
            }
        }
    }

    fun clearAll() = realm.executeTransaction(Realm::deleteAll)
//endregion

    //region usersDelegate
    private val usersDelegate = object : UsersDelegate.Simple() {

        override fun onMessageReceived(requestCode: Int?, message: ReceivedMessage)
                = addNewMessage(message)

        override fun onStreamsReceived(requestCode: Int?, streams: RealmList<Stream>)
                = resetStreams(streams)

        override fun onStreamReceived(requestCode: Int?, stream: Stream)
                = addOrUpdateStream(stream)
    }
//endregion

    //region socketListener
    private val socketListener = object : SocketListener {

        override fun onConnected() = logFun(TAG, SocketListener::onConnected)
        override fun onMessageFailed() = logFun(TAG, SocketListener::onMessageFailed)
        override fun onMessageSent() = logFun(TAG, SocketListener::onMessageSent)

        override fun onMessageReceived(message: Message) {
            logFun(TAG, SocketListener::onMessageReceived, message)
            addNewMessage(message)
        }

        override fun onMessageDelivered(info: DeliveredInfo) {
            logFun(TAG, SocketListener::onMessageDelivered, info)
            updateMessageStatus(info)
        }

        override fun onMessagesSeen(info: SeenInfo) {
            logFun(TAG, SocketListener::onMessagesSeen, info)
            updateMessageStatus(info)
        }

        override fun onMessageRemoved(messageId: Long) {
            logFun(TAG, SocketListener::onMessageRemoved, messageId)
            setMessageAsDeleted(messageId)
        }

        override fun onSubscribed(data: SubscribeData) {
            logFun(TAG, SocketListener::onSubscribed, data)
            val steamId = data.streamId
            val streamType = StreamType.find(data.streamType)
            if (steamId == null || streamType == null) initStreams()
            else addOrUpdateStream(streamType, steamId)
        }

        override fun onUnsubscribed(data: SubscribeData) {
            logFun(TAG, SocketListener::onUnsubscribed, data)
            val steamId = data.streamId
            val streamType = StreamType.find(data.streamType)
            if (steamId == null || streamType == null) initStreams()
            else removeStream(streamType, steamId)
        }

        override fun onChatCreated(chat: Stream) {
            logFun(TAG, SocketListener::onChatCreated, chat)
            addOrUpdateStream(chat)
        }

        override fun onChatUpdated(chatId: Long, name: String) {
            logFun(TAG, SocketListener::onChatUpdated, chatId, name)
            updateChatName(chatId, name)
        }

        override fun onInvitedToChat(chat: Stream) {
            logFun(TAG, SocketListener::onInvitedToChat, chat)
            addOrUpdateStream(chat)
        }

        override fun onUploadedPhotoToChat(chatId: Long, photo: Media) {
            logFun(TAG, SocketListener::onUploadedPhotoToChat, chatId, photo)
//            val stream = streamsMap!![Stream.generateSid(StreamType.CHATS, chatId)]
//            if (stream != null) {
//                stream.asChat!!.photo = photo
//                delegatesSet.notify(TAG).onStreamsUpdated()
//            }
        }

        override fun onLeftChat(chatId: Long, userId: Long) {
            logFun(TAG, SocketListener::onLeftChat, chatId, userId)
            // TODO remove this user from streams
            CurrentUserSettings.currentUser?.id?.let { removeStream(StreamType.CHATS, it) }
        }

        override fun onChatDeleted(chatId: Long) {
            logFun(TAG, SocketListener::onChatDeleted, chatId)
            removeStream(StreamType.CHATS, chatId)
        }

        override fun onStickersGroupCreated() = Unit
        override fun onStickersGroupUpdated() = Unit
        override fun onStickersGroupDeleted() = Unit
        override fun onStickerCreated() = Unit
        override fun onStickerDeleted() = Unit
    }
//endregion

    //region Initialization
    init {
        UsersManager.delegatesSet.addDelegate(TAG, usersDelegate)
        SocketManager.listener = socketListener
    }
//endregion
}

fun logFun(tag: String?, kFunction: KFunction<Any?>, vararg arguments: Any?) {
    Log.d(tag, "${kFunction.name}(${kFunction.parameters.filter { it.name != null }
            .joinToString(", ") { "${it.name}: ${it.type.toString().split(".").last()}" }})" +
            " -> ${arguments.asList()}")
}