package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Settings(
        @SerializedName("id") var id: Long = -1,
        @SerializedName("user_id") var userId: Long? = 0,
        @SerializedName("language") var language: String? = null,
        @SerializedName("notifications") var notifications: Boolean? = false
) : Parcelable {

    //region toString
    override fun toString() = "Settings(id=$id, userId=$userId, language=$language" +
            ", notifications=$notifications)"
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeLong(id)
            writeValue(userId)
            writeString(language)
            writeValue(notifications)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readValue(Long::class.java.classLoader) as Long?,
            source.readString(),
            source.readValue(Boolean::class.java.classLoader) as Boolean?
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Settings> = object : Parcelable.Creator<Settings> {
            override fun createFromParcel(source: Parcel): Settings = Settings(source)
            override fun newArray(size: Int): Array<Settings?> = arrayOfNulls(size)
        }
    }
    //endregion
}
