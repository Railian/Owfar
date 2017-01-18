package com.owfar.android.models.api.interfaces

import com.owfar.android.models.api.classes.User
import io.realm.RealmList

interface Conversation {
    var users: RealmList<User>?

    val opponent: User?
}
