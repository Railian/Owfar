package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.owfar.android.models.api.enums.SubscribedState
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class InterestsGroup(
        @SerializedName("id") @PrimaryKey var id: Long = -1,
        @SerializedName("name") var name: String? = null,
        @SerializedName("photo") var photo: Media? = null,
        @SerializedName("status") var status: Int? = null,
        @SerializedName("interests") var interests: RealmList<Stream>? = null
) : RealmObject(), Parcelable {

    @SerializedName("subscribed") var isSubscribed: Boolean? = null
        set(value) {
            field = value
            interests?.forEach { it.asInterest?.isSubscribed = value }
        }

    val subscribedState: SubscribedState?
        get() = interests?.let {
            when (it.count { it.asInterest?.isSubscribed == true }) {
                0 -> SubscribedState.SUBSCRIBED_NOTHING
                it.size -> SubscribedState.SUBSCRIBED_ALL
                else -> SubscribedState.INDETERMINATE
            }
        } ?: if (isSubscribed == true) SubscribedState.SUBSCRIBED_ALL else SubscribedState.SUBSCRIBED_NOTHING

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeLong(id)
            writeString(name)
            writeParcelable(photo, flags)
            writeValue(status)
            writeList(interests)
            writeValue(isSubscribed)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readParcelable(Media::class.java.classLoader),
            source.readValue(Int::class.java.classLoader) as Int
    ) {
        interests = RealmList<Stream>().apply { source.readList(this, Stream::class.java.classLoader) }
        isSubscribed = source.readValue(Boolean::class.java.classLoader) as Boolean
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<InterestsGroup> = object : Parcelable.Creator<InterestsGroup> {
            override fun createFromParcel(source: Parcel): InterestsGroup = InterestsGroup(source)
            override fun newArray(size: Int): Array<InterestsGroup?> = arrayOfNulls(size)
        }
    }
    //endregion
}