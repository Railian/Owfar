package com.owfar.android.models.api

import com.google.gson.annotations.SerializedName
import com.owfar.android.models.api.classes.ReceivedMessage
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.classes.StickersGroup
import com.owfar.android.models.api.classes.User
import io.realm.RealmList

data class ChatUsersBody(@SerializedName("usersIds") var usersIds: List<Long>? = null)

data class PhoneList(@SerializedName("phones") var phones: List<String>? = null)

data class ContactList(@SerializedName("users") var users: RealmList<User>? = null)

data class MessageList(@SerializedName("messages") var messages: RealmList<ReceivedMessage>? = null)

data class StickerList(
        @SerializedName("recent") var recent: RealmList<Sticker>? = null,
        @SerializedName("groups") var groups: RealmList<StickersGroup>? = null
)

data class ContactSearching(
        @SerializedName("exists") var exists: Boolean = false,
        @SerializedName("user") var user: User? = null
)

data class UserCreation(
        @SerializedName("user") var user: User? = null,
        @SerializedName("firstAuth") var firstAuth: Boolean = false
)