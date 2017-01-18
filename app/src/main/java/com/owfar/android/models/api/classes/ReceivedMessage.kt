package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.owfar.android.extensions.asStringOrNull
import com.owfar.android.models.api.enums.MessageBodyType
import com.owfar.android.models.api.enums.MessageStatus
import com.owfar.android.models.api.interfaces.Message
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.lang.reflect.Type
import java.util.*

@Suppress("DeprecatedCallableAddReplaceWith")
open class ReceivedMessage(
        @SerializedName("id") @PrimaryKey var id: Long = -1,
        @SerializedName("sid") override var sid: String? = null,
        @SerializedName("stream_type") override var streamType: String? = null,
        @SerializedName("stream_id") override var streamId: Long? = null,
        @SerializedName("created_at") override var createdAt: Date? = null,
        @SerializedName("sentAt") override var sentAt: Date? = null,
        @SerializedName("user") override var user: User? = null,
        @SerializedName("body_type") override var bodyType: String? = null,
        @SerializedName("content") override var content: String? = null,
        @SerializedName("sticker") override var sticker: Sticker? = null,
        @SerializedName("media") var media: Media? = null,
        @SerializedName("seen") private var seenList: RealmList<Seen>? = null
) : RealmObject(), Message, Parcelable {

    fun getMessageStatus(users: List<User>?): MessageStatus? {
        if (seenList != null && users != null)
            if (seenList?.count() == users.count())
                seenList?.groupBy(Seen::messageStatus)?.let {
                    return when {
                        it[null]?.count() ?: 0 > 0 -> MessageStatus.SENT
                        it[MessageStatus.DELIVERED.jsonName]?.count() ?: 0 > 0 -> MessageStatus.DELIVERED
                        else -> MessageStatus.SEEN
                    }
                }
        return MessageStatus.SENT
    }

    fun setMessageStatus(userId: Long, status: MessageStatus?) {
        if (seenList == null) seenList = RealmList()
        seenList?.apply {
            find { it.messageId == id && it.userId == userId }?.let { info ->
                val oldStatus = MessageStatus.find(info.messageStatus) ?: MessageStatus.SENDING
                val newStatus = status ?: MessageStatus.SENDING
                if (newStatus.ordinal >= oldStatus.ordinal)
                    info.messageStatus = newStatus.jsonName
            } ?: let {
                add(Seen(System.currentTimeMillis(), id, userId, status?.jsonName))
            }
        }
    }

    //region toString
    override fun toString() = "ReceivedMessage(id=$id, sid=$sid, streamType=$streamType" +
            ", streamId=$streamId, createdAt=$createdAt, sentAt=$sentAt, user=$user" +
            ", bodyType=$bodyType, content=$content, sticker=$sticker, media=$media" +
            ", seenList=$seenList)"
    //endregion

    //region Deprecated
    @Deprecated("Supported only in interface Message")
    override fun asSent() = super.asSent()

    @Deprecated("Supported only in interface Message")
    override fun asReceived() = super.asReceived()
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeLong(id)
            writeString(sid)
            writeString(streamType)
            writeValue(streamId)
            writeSerializable(createdAt)
            writeSerializable(sentAt)
            writeParcelable(user, flags)
            writeString(bodyType)
            writeString(content)
            writeParcelable(sticker, flags)
            writeParcelable(media, flags)
            writeList(seenList)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readValue(Long::class.java.classLoader) as Long,
            source.readSerializable() as Date?,
            source.readSerializable() as Date?,
            source.readParcelable(User::class.java.classLoader),
            source.readString(),
            source.readString(),
            source.readParcelable(Sticker::class.java.classLoader),
            source.readParcelable(Media::class.java.classLoader)
    ) {
        seenList = RealmList<Seen>().apply { source.readList(this, Seen::class.java.classLoader) }
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ReceivedMessage> = object : Parcelable.Creator<ReceivedMessage> {
            override fun createFromParcel(source: Parcel): ReceivedMessage = ReceivedMessage(source)
            override fun newArray(size: Int): Array<ReceivedMessage?> = arrayOfNulls(size)
        }
    }
    //endregion

    //region JsonTypeAdapter
    class JsonTypeAdapter : JsonSerializer<ReceivedMessage>, JsonDeserializer<ReceivedMessage> {

        override fun serialize(src: ReceivedMessage?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return context?.serialize(src, typeOfSrc) ?: JsonNull.INSTANCE
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ReceivedMessage? {

            json.asJsonObject?.let { json ->
                return ReceivedMessage().apply {
                    id = json.get("id").asLong
                    sid = json.get("sid").asStringOrNull
                    streamType = json.get("stream_type").asStringOrNull
                    streamId = json.get("stream_id").asLong
                    createdAt = context.deserialize(json.get("created_at"), Date::class.java)
                    sentAt = context.deserialize(json.get("sent_at"), Date::class.java)
                    user = context.deserialize(json.get("user"), User::class.java)

                    bodyType = json.get("body_type")?.asStringOrNull
                    json.getAsJsonObject("body")?.let { json ->
                        if (json.entrySet().size == 0) bodyType = MessageBodyType.DELETED.jsonName
                        else when (MessageBodyType.find(bodyType)) {
                            MessageBodyType.DELETED -> content = json.get("content")?.asStringOrNull
                            MessageBodyType.SYSTEM -> content = json.get("content")?.asStringOrNull
                            MessageBodyType.COMMENT -> content = json.get("content")?.asStringOrNull
                            MessageBodyType.STICKER -> sticker = context.deserialize(json, Sticker::class.java)
                            MessageBodyType.PHOTO -> media = context.deserialize(json, Media::class.java)
                            MessageBodyType.AUDIO -> media = context.deserialize(json, Media::class.java)
                            MessageBodyType.VIDEO -> media = context.deserialize(json, Media::class.java)
                        }
                    } ?: bodyType.apply { bodyType = MessageBodyType.DELETED.jsonName }

                    json.getAsJsonArray("seen")?.let { json ->
                        seenList = RealmList<Seen>().apply {
                            json.forEach { add(context.deserialize(it, Seen::class.java)) }
                        }
                    }
                }
            } ?: return null
        }
    }
    //endregion
}