package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.owfar.android.models.api.interfaces.Message
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

@Suppress("DeprecatedCallableAddReplaceWith")
open class SentMessage(
        @SerializedName("sid") @PrimaryKey override var sid: String? = "",
        @SerializedName("stream_type") override var streamType: String? = null,
        @SerializedName("stream_id") override var streamId: Long? = null,
        @SerializedName("created_at") override var createdAt: Date? = null,
        @SerializedName("sent_at") override var sentAt: Date? = null,
        @SerializedName("user") override var user: User? = null,
        @SerializedName("content_type") override var bodyType: String? = null,
        @SerializedName("content") override var content: String? = null,
        @SerializedName("sticker") override var sticker: Sticker? = null,
        @SerializedName("message_status") var messageStatus: String? = null
) : RealmObject(), Message, Parcelable {

    //region toString
    override fun toString() = "SentMessage(sid=$sid, streamType=$streamType, streamId=$streamId" +
            ", createdAt=$createdAt, sentAt=$sentAt, user=$user, bodyType=$bodyType" +
            ", content=$content, sticker=$sticker, messageStatus=$messageStatus)"
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
            writeString(sid)
            writeString(streamType)
            writeValue(streamId)
            writeSerializable(createdAt)
            writeSerializable(sentAt)
            writeParcelable(user, flags)
            writeString(bodyType)
            writeString(content)
            writeParcelable(sticker, flags)
            writeString(messageStatus)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readValue(Long::class.java.classLoader) as Long?,
            source.readSerializable() as Date?,
            source.readSerializable() as Date?,
            source.readParcelable(User::class.java.classLoader),
            source.readString(),
            source.readString(),
            source.readParcelable(Sticker::class.java.classLoader),
            source.readString()
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<SentMessage> = object : Parcelable.Creator<SentMessage> {
            override fun createFromParcel(source: Parcel): SentMessage = SentMessage(source)
            override fun newArray(size: Int): Array<SentMessage?> = arrayOfNulls(size)
        }
    }
    //endregion
}