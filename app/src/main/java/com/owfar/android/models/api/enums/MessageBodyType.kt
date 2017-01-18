package com.owfar.android.models.api.enums

enum class MessageBodyType (val jsonName: String) {

    DELETED("deleted"), SYSTEM("systems"), COMMENT("comments"), STICKER("stickers"), PHOTO("photos"),
    AUDIO("audios"), VIDEO("videos");

    companion object {
        fun find(jsonName: String?): MessageBodyType? = values().find { it.jsonName == jsonName }
    }
}
