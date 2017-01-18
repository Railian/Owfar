package com.owfar.android.models.api.enums

import com.owfar.android.R

enum class StreamType(val jsonName: String, val notificationRes: Int
) {
    CONVERSATIONS("conversations", R.drawable.indicator_orange),
    CHATS("chats", R.drawable.indicator_green),
    INTERESTS("interests", R.drawable.indicator_blue);

    companion object {
        fun find(jsonName: String?): StreamType? = StreamType.values().find { it.jsonName == jsonName }
    }
}