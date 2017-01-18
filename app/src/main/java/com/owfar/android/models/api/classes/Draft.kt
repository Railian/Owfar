package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Draft(
        @SerializedName("stream_sid") @PrimaryKey var streamSid: String = "",
        @SerializedName("text") var text: String? = null
) : RealmObject(), Parcelable {

    override fun toString() = "Draft(streamSid='$streamSid', text=$text)"

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(streamSid)
        writeString(text)
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString()
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Draft> = object : Parcelable.Creator<Draft> {
            override fun createFromParcel(source: Parcel) = Draft(source)
            override fun newArray(size: Int): Array<Draft?> = arrayOfNulls(size)
        }
    }
}