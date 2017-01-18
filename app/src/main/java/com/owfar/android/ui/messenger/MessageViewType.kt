package com.owfar.android.ui.messenger

import android.support.annotation.LayoutRes
import com.owfar.android.R
import com.owfar.android.models.api.enums.MessageBodyType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.settings.CurrentUserSettings

enum class MessageViewType(@LayoutRes val layoutRes: Int) {

    LEFT_COMMENT(R.layout.item_messenger_left_comment),
    LEFT_STICKER(R.layout.item_messenger_left_sticker),
    LEFT_PHOTO(R.layout.item_messenger_left_photo),
    LEFT_AUDIO(R.layout.item_messenger_left_audio),
    LEFT_VIDEO(R.layout.item_messenger_left_video),
    LEFT_DELETED(R.layout.item_messenger_left_deleted),
    RIGHT_COMMENT(R.layout.item_messenger_right_comment),
    RIGHT_STICKER(R.layout.item_messenger_right_sticker),
    RIGHT_PHOTO(R.layout.item_messenger_right_photo),
    RIGHT_AUDIO(R.layout.item_messenger_right_audio),
    RIGHT_VIDEO(R.layout.item_messenger_right_video),
    RIGHT_DELETED(R.layout.item_messenger_right_deleted),
    SYSTEM(R.layout.item_messenger_system);

    companion object {
        fun find(message: Message?): MessageViewType? = message?.let {
            val currentUser = CurrentUserSettings.currentUser
            val prefix = if (it.user?.id == currentUser?.id) "RIGHT_" else "LEFT_"
            MessageBodyType.find(it.bodyType)?.let { bodyType ->
                when (bodyType) {
                    MessageBodyType.SYSTEM -> SYSTEM
                    MessageBodyType.DELETED -> valueOf("${prefix}DELETED")
                    else -> valueOf("$prefix${bodyType.name}")
                }
            }
        }
    }
}