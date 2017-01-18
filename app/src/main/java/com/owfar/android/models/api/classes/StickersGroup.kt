package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class StickersGroup(
        @SerializedName("id") @PrimaryKey var id: Long = -1,
        @SerializedName("name") var name: String? = null,
        @SerializedName("status") var status: Int? = null,
        @SerializedName("store_id") var storeId: String? = null,
        @SerializedName("is_free") var isFree: Boolean? = null,
        @SerializedName("is_bought") var isBought: Boolean? = null,
        @SerializedName("photo") var photo: Media? = null,
        @SerializedName("stickers") var stickers: RealmList<Sticker>? = null
) : RealmObject(), Parcelable {

    //region toString
    override fun toString() = "StickersGroup(id=$id, name=$name, status=$status" +
            ", stickers=$stickers, photo=$photo)"
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeLong(id)
            writeString(name)
            writeValue(status)
            writeString(storeId)
            writeValue(isFree)
            writeValue(isBought)
            writeParcelable(photo, flags)
            writeList(stickers)
//            writeParcelableArray(stickers?.requireNoNulls()?.toTypedArray(), flags)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString(),
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readParcelable(Media::class.java.classLoader)
    ) {
        stickers = RealmList<Sticker>().apply { source.readList(this, Sticker::class.java.classLoader) }
//        stickers = RealmList<Sticker>().apply { addAll(source.readParcelableArray(Sticker::class.java.classLoader) as Array<out Sticker>) }
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<StickersGroup> = object : Parcelable.Creator<StickersGroup> {
            override fun createFromParcel(source: Parcel): StickersGroup = StickersGroup(source)
            override fun newArray(size: Int): Array<StickersGroup?> = arrayOfNulls(size)
        }
    }
    //endregion
}