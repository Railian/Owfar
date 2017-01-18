package com.owfar.android.models.api.enums

enum class MediaStorageType(val storageName: String) {
    USERS_PHOTOS(".users photos"),
    CHATS_PHOTOS(".chats photos"),
    CHANNELS_PHOTOS(".channels photos"),
    STICKERS(".stickers"),
    IMAGES(".images"),
    VIDEOS(".videos"),
    AUDIOS(".audios"),
    SAVED_IMAGES("saved owfar images"),
    SAVED_VIDEOS("saved owfar videos"),
    OTHER(".other")
}