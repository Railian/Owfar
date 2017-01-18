package com.owfar.android.models.api.interfaces

import com.owfar.android.models.api.classes.ReceivedMessage
import com.owfar.android.models.api.classes.SentMessage
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.MessageBodyType
import com.owfar.android.models.api.enums.MessageClassType
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.settings.CurrentUserSettings
import io.realm.RealmModel
import java.util.*

interface Message : Comparable<Message>, RealmModel {

    var sid: String?
    var streamType: String?
    var streamId: Long?
    var createdAt: Date?
    var sentAt: Date?
    var user: User?
    var bodyType: String?
    var content: String?
    var sticker: Sticker?

    fun ticket() = StreamType.find(streamType)?.let {
        when (MessageBodyType.find(bodyType)) {
            MessageBodyType.DELETED -> "message has been removed"
            MessageBodyType.SYSTEM -> content
            MessageBodyType.COMMENT -> "${
            if (it == StreamType.CHATS) user?.displayName?.let { "$it: " }.orEmpty()
            else ""
            }$content"
            MessageBodyType.STICKER -> "Sticker" +
                    if (it == StreamType.INTERESTS) ""
                    else " from ${user?.displayName ?: "unknown user"}"
            MessageBodyType.PHOTO -> "Photo" +
                    if (it == StreamType.INTERESTS) ""
                    else " from ${user?.displayName ?: "unknown user"}"
            MessageBodyType.AUDIO -> "Audio" +
                    if (it == StreamType.INTERESTS) ""
                    else " from ${user?.displayName ?: "unknown user"}"
            MessageBodyType.VIDEO -> "Video" +
                    if (it == StreamType.INTERESTS) ""
                    else " from ${user?.displayName ?: "unknown user"}"
            else -> null
        }
    }

    fun lastUpdatedAt() = sentAt ?: createdAt

    fun messageClassType() = when (this) {
        is SentMessage -> MessageClassType.SENT
        is ReceivedMessage -> MessageClassType.RECEIVED
        else -> null
    }

    fun asSent() = this as? SentMessage

    fun asReceived() = this as? ReceivedMessage

    override operator fun compareTo(other: Message): Int {
        val otherSentAt = other.sentAt
        return otherSentAt?.let { sentAt?.compareTo(otherSentAt) } ?: 0
    }

    fun generateSID() = generateSID(streamType, streamId)

    companion object {
        fun generateSID(streamType: String?, streamId: Long?): String {
            val user = CurrentUserSettings.currentUser
            val userId = user?.id
            val timestamp = System.currentTimeMillis()
            return "${streamType}_${streamId}_from_${userId}_at_$timestamp"
        }
    }
}



