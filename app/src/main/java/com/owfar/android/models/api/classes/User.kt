package com.owfar.android.models.api.classes

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.owfar.android.extensions.orNullIfBlank
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class User(
        @SerializedName("id") @PrimaryKey var id: Long = -1,
        @SerializedName("phone") var phone: String? = null,
        @SerializedName("email") var email: String? = null,
        @SerializedName("password") var password: String? = null,
        @SerializedName("status") var status: String? = null,
        @SerializedName("phone_last7") var phoneLast7: String? = null,
        @SerializedName("role") var role: Role? = null,
        @SerializedName("profile") var profile: Profile? = null
) : RealmObject(), Comparable<User>, Parcelable {

    @Ignore @Transient var fullNameFromContacts: String? = null
    @Ignore @Transient var photoFromContacts: Uri? = null
    @Ignore @Transient var hasOwfar: Boolean? = false

    val displayName: String?
        get() = profile?.fullName?.orNullIfBlank() ?: phone?.orNullIfBlank()

    //region toString
    override fun toString() = "User(id=$id, phone=$phone, email=$email, password=$password" +
            ", status=$status, phoneLast7=$phoneLast7, role=$role, profile=$profile" +
            ", fullNameFromContacts=$fullNameFromContacts" +
            ", photoFromContacts=$photoFromContacts, hasOwfar=$hasOwfar)"
    //endregion

    //region Comparable Implementation
    override fun compareTo(other: User) = other.displayName?.let { displayName?.compareTo(it) } ?: 0
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeLong(id)
            writeString(phone)
            writeString(email)
            writeString(password)
            writeString(status)
            writeString(phoneLast7)
            writeParcelable(role, flags)
            writeParcelable(profile, flags)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable(Role::class.java.classLoader),
            source.readParcelable(Profile::class.java.classLoader)
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(source: Parcel): User = User(source)
            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
    //endregion
}