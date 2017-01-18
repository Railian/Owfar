package com.owfar.android.models.api.classes

import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.google.gson.annotations.SerializedName
import com.owfar.android.extensions.formattedDate
import com.owfar.android.extensions.formattedTime
import com.owfar.android.extensions.isToday
import com.owfar.android.extensions.orNullIfBlank
import com.owfar.android.models.api.enums.MediaStorageType
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Chat
import com.owfar.android.models.api.interfaces.Conversation
import com.owfar.android.models.api.interfaces.Interest
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.settings.CurrentUserSettings
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.*

open class Stream(
        @SerializedName("sid") @PrimaryKey var sid: String? = null,
        @SerializedName("id") var id: Long = -1,
        @SerializedName("type") var type: String? = null,
        @SerializedName("status") var status: Int? = null,
        @SerializedName("name") private var name: String? = null,
        @SerializedName("description") private var description: String? = null,
        @SerializedName("avatar") private var avatar: Media? = null,
        @SerializedName("photo") private var photo: Media? = null,
        @SerializedName("subscribed") private var subscribed: Boolean? = null,
        @SerializedName("followers") private var followers: Int? = null,
        @SerializedName("unread_count") var unreadCount: Int = 0,
        @SerializedName("users") private var users: RealmList<User>? = null,
        @SerializedName("messages") @Ignore var receivedMessages: MutableList<ReceivedMessage>? = null
) : RealmObject(), Parcelable {

    @Ignore @Transient var allMessages: MutableList<Message>? = null

    val asConversation: Conversation?
        get() = if (StreamType.find(type) == StreamType.CONVERSATIONS) object : Conversation {

            override var users: RealmList<User>?
                get() = this@Stream.users
                set(value) {
                    this@Stream.users = value
                }

            override val opponent: User?
                get() = users?.filter { it.id != CurrentUserSettings.currentUser?.id }?.first()

        } else null

    val asChat: Chat?
        get() = if (StreamType.find(type) == StreamType.CHATS) object : Chat {
            override var name: String?
                get() = this@Stream.name
                set(value) {
                    this@Stream.name = value
                }

            override var photo: Media?
                get() = this@Stream.photo
                set(value) {
                    this@Stream.photo = value
                }

            override var users: RealmList<User>?
                get() = this@Stream.users
                set(value) {
                    this@Stream.users = value
                }

            override val displayName: String?
                get() = name?.orNullIfBlank() ?: getFirstUsersNamesWithoutYourself(5)

            override fun getFirstUsersNamesWithoutYourself(limit: Int) = users
                    ?.filter { it.id != CurrentUserSettings.currentUser?.id }
                    ?.let {
                        it.map(User::displayName)
                                .filter { it != null && it.isNotBlank() }
                                .take(limit)
                                .joinToString(", ")
                                .plus(if (it.size > limit) ", etc." else "")
                    }

        } else null

    val asInterest: Interest?
        get() = if (StreamType.find(type) == StreamType.INTERESTS) object : Interest {

            override var name: String?
                get() = this@Stream.name
                set(value) {
                    this@Stream.name = value
                }

            override var description: String?
                get() = this@Stream.description
                set(value) {
                    this@Stream.description = value
                }

            override var photo: Media?
                get() = this@Stream.photo
                set(value) {
                    this@Stream.photo = value
                }

            override var avatar: Media?
                get() = this@Stream.avatar
                set(value) {
                    this@Stream.avatar = value
                }

            override var isSubscribed: Boolean?
                get() = this@Stream.subscribed
                set(value) {
                    this@Stream.subscribed = value
                }

            override var followers: Int?
                get() = this@Stream.followers
                set(value) {
                    this@Stream.followers = value
                }

        } else null

    val displayName: String?
        get() = when (StreamType.find(type)) {
            StreamType.CONVERSATIONS -> asConversation?.opponent?.displayName
            StreamType.CHATS -> asChat?.displayName
            StreamType.INTERESTS -> asInterest?.name
            else -> null
        }

    val image: Media?
        get() = when (StreamType.find(type)) {
            StreamType.CONVERSATIONS -> asConversation?.opponent?.profile?.photo
            StreamType.CHATS -> asChat?.photo
            StreamType.INTERESTS -> asInterest?.avatar
            else -> null
        }

    val imageMediaType: MediaStorageType
        get() = when (StreamType.find(type)) {
            StreamType.CONVERSATIONS -> MediaStorageType.USERS_PHOTOS
            StreamType.CHATS -> MediaStorageType.CHATS_PHOTOS
            StreamType.INTERESTS -> MediaStorageType.CHANNELS_PHOTOS
            else -> MediaStorageType.OTHER
        }

    val lastMessage: Message?
        get() = allMessages?.firstOrNull()

    val lastReceivedMessage: ReceivedMessage?
        get() = allMessages?.filter { it is ReceivedMessage }?.firstOrNull() as? ReceivedMessage

    val ticket: String?
        get() = lastMessage?.ticket() ?: asInterest?.description

    val lastUpdatedAt: Date?
        get() = lastMessage?.lastUpdatedAt()

    val formattedLastUpdatedAt: String?
        get() = lastUpdatedAt?.let { if (it.isToday()) it.formattedTime else it.formattedDate }

//    fun addNewMessage(message: Message) {
//        when (message.messageClassType()) {
//            MessageClassType.RECEIVED -> {
//                receivedMessages ?: let {
//                    receivedMessages = RealmList()
//                    receivedMessages
//                }?.apply {
//                    add(0, message as ReceivedMessage)
//                    sort()
//                    indexOf(message)
//                }
//            }
//        }
//    }
//
//    fun addOldMessage(message: Message?) = {
//        if (messages == null) messages = RealmList()
//        messages?.add(message)
//        messages?.sort()
//        messages?.indexOf(message)
//    }

    fun getRemovingText(accentColor: Int) = displayName?.let {
        when (StreamType.find(type)) {
            StreamType.CONVERSATIONS -> "Conversation with $it was removed"
            StreamType.CHATS -> asChat?.name?.let { "You have left chat named $it" } ?: "You have left chat with $it"
            StreamType.INTERESTS -> "You have unsubscribed from $it chanel"
            else -> "Stream was removed"
        }
    }

    private fun spannable(pattern: String, key: String?, color: Int) =
            SpannableString(pattern).apply {
                setSpan(ForegroundColorSpan(color), 0, length, 0)
                setSpan(StyleSpan(Typeface.BOLD_ITALIC), 0, length, 0)
            }

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(sid)
        writeLong(id)
        writeString(type)
        writeValue(status)
        writeString(name)
        writeString(description)
        writeParcelable(avatar, flags)
        writeParcelable(photo, flags)
        writeValue(subscribed)
        writeValue(followers)
        writeInt(unreadCount)
        writeList(users)
        writeList(receivedMessages)
        writeList(allMessages?.filter { it is SentMessage })
        writeList(allMessages?.filter { it is ReceivedMessage })
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readLong(),
            source.readString(),
            source.readValue(Int::class.java.classLoader) as? Int,
            source.readString(),
            source.readString(),
            source.readParcelable(Media::class.java.classLoader),
            source.readParcelable(Media::class.java.classLoader),
            source.readValue(Boolean::class.java.classLoader) as? Boolean,
            source.readValue(Int::class.java.classLoader) as? Int,
            source.readInt()
    ) {
        users = RealmList<User>().apply {
            source.readList(this, User::class.java.classLoader)
        }
        receivedMessages = RealmList<ReceivedMessage>().apply {
            source.readList(this, ReceivedMessage::class.java.classLoader)
        }
        allMessages = RealmList<Message>().apply {
            source.readList(this, SentMessage::class.java.classLoader)
            source.readList(this, ReceivedMessage::class.java.classLoader)
        }
    }

    companion object {

        fun generateSid(type: StreamType?, id: Long) = "${type ?: "UNKNOWN"}_$id"

        @JvmField val CREATOR: Parcelable.Creator<Stream> = object : Parcelable.Creator<Stream> {
            override fun createFromParcel(source: Parcel) = Stream(source)
            override fun newArray(size: Int): Array<Stream?> = arrayOfNulls(size)
        }
    }
    //endregion
}