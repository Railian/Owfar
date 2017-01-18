package com.owfar.android.data

import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.enums.MessageStatus
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import io.realm.RealmList

interface DataDelegate {
    fun onStreamsUpdated(streams: MutableList<Stream>)
    fun onNewMessageAdded(stream: Stream, message: Message)
    fun onOldMessagesAdded(stream: Stream, messages: MutableList<Message>)
    fun onMessageStatusUpdated(messageId: Long, userId: Long, status: MessageStatus)
    fun onMessageDeleted( messageId: Long)
}
