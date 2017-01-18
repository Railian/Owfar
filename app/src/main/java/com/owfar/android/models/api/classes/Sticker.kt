package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Sticker(
        @SerializedName("id") @PrimaryKey var id: Long = -1,
        @SerializedName("status") var status: String? = null,
        @SerializedName("media_file") var mediaFile: Media? = null,
        @SerializedName("stickers_group_id") var stickersGroupId: Long? = null
) : RealmObject(), Parcelable {

    //region toString
    override fun toString() = "Sticker(id=$id, status=$status, mediaFile=$mediaFile" +
            ", stickersGroupId=$stickersGroupId)"
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeLong(id)
            writeString(status)
            writeParcelable(mediaFile, flags)
            writeValue(stickersGroupId)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readParcelable(Media::class.java.classLoader),
            source.readValue(Long::class.java.classLoader) as Long?
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Sticker> = object : Parcelable.Creator<Sticker> {
            override fun createFromParcel(source: Parcel): Sticker = Sticker(source)
            override fun newArray(size: Int): Array<Sticker?> = arrayOfNulls(size)
        }
    }
    //endregion
}