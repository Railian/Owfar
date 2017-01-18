package com.owfar.android.ui.messenger.holders

import android.view.View
import android.widget.ImageView
import com.owfar.android.R
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import io.realm.RealmList

class MessengerStickerViewHolder(view: View) : MessengerItemViewHolder(view) {

    private val stickerView: ImageView?

    init {
        stickerView = view.findViewById(R.id.item_messenger_ivSticker) as? ImageView
    }

    //region Configuration Of View
    override fun configureWithMessage(streamType: StreamType?, message: Message?, showStatus: Boolean, users: RealmList<User>?) {
        super.configureWithMessage(streamType, message, showStatus, users)
        stickerView?.apply {
            message?.sticker?.mediaFile?.let {
                MediaHelper
                        .load(it)
                        .withOptions(MediaStorageType.STICKERS, MediaSize._2X)
                        .placeholder(R.drawable.ic_default_sticker)
                        .error(R.drawable.ic_default_sticker)
                        .into(this)
            }
        }
    }
    //endregion
}