package com.owfar.android.models.api.interfaces

import com.owfar.android.models.api.classes.Media

interface Interest {
    var name: String?
    var description: String?
    var photo: Media?
    var avatar: Media?
    var isSubscribed: Boolean?
    var followers: Int?
}