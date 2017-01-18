package com.owfar.android.models.api.interfaces

import com.owfar.android.models.api.classes.Media
import com.owfar.android.models.api.classes.User
import io.realm.RealmList

interface Chat {
    var name: String?
    var photo: Media?
    var users: RealmList<User>?

    val displayName: String?
    fun getFirstUsersNamesWithoutYourself(limit: Int): String?
}

