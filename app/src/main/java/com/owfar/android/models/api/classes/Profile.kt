package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Profile(
        @SerializedName("id") @PrimaryKey var id: Long? = null,
        @SerializedName("gender") var gender: String? = null,
        @SerializedName("fname") @Expose var firstName: String? = null,
        @SerializedName("lname") var lastName: String? = null,
        @SerializedName("fullName") var fullName: String? = null,
        @SerializedName("synopsis") var synopsis: String? = null,
        @SerializedName("online") var online: String? = null,
        @SerializedName("last_online_ts") var lastOnlineTs: String? = null,
        @SerializedName("photo") var photo: Media? = null
) : RealmObject(), Parcelable {

    //region toString
    override fun toString() = "Profile(id=$id, gender=$gender, firstName=$firstName" +
            ", lastName=$lastName, fullName=$fullName, synopsis=$synopsis, online=$online" +
            ", lastOnlineTs=$lastOnlineTs, photo=$photo)"
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeValue(id)
            writeString(gender)
            writeString(firstName)
            writeString(lastName)
            writeString(fullName)
            writeString(synopsis)
            writeString(online)
            writeString(lastOnlineTs)
            writeParcelable(photo, flags)
        }
    }

    constructor(source: Parcel) : this(
            source.readValue(Long::class.java.classLoader) as? Long,
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable(Media::class.java.classLoader)
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Profile> = object : Parcelable.Creator<Profile> {
            override fun createFromParcel(source: Parcel): Profile = Profile(source)
            override fun newArray(size: Int): Array<Profile?> = arrayOfNulls(size)
        }
    }
    //endregion
}