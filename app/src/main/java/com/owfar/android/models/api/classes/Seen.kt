package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Seen(
        @SerializedName("id") @PrimaryKey var id: Long = -1,
        @SerializedName("message_id") var messageId: Long = -1,
        @SerializedName("user_id") var userId: Long = -1,
        @SerializedName("status") var messageStatus: String? = null
) : RealmObject(), Parcelable {

    //region toString
    override fun toString() = "Seen(id=$id, messageId=$messageId, userId=$userId" +
            ", messageStatus=$messageStatus)"
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeLong(id)
            writeLong(messageId)
            writeLong(userId)
            writeString(messageStatus)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readLong(),
            source.readLong(),
            source.readString()
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Seen> = object : Parcelable.Creator<Seen> {
            override fun createFromParcel(source: Parcel): Seen = Seen(source)
            override fun newArray(size: Int): Array<Seen?> = arrayOfNulls(size)
        }
    }
    //endregion
}

