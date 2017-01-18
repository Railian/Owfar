package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Role(
        @SerializedName("id") @PrimaryKey var id: Long = -1,
        @SerializedName("name") var name: String? = null
) : RealmObject(), Parcelable {

    //region toString
    override fun toString() = "Role(id=$id, name=$name)"
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeLong(id)
            writeString(name)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString()
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Role> = object : Parcelable.Creator<Role> {
            override fun createFromParcel(source: Parcel): Role = Role(source)
            override fun newArray(size: Int): Array<Role?> = arrayOfNulls(size)
        }
    }
    //endregion
}
