package com.owfar.android.ui.messenger.holders

import android.view.View
import android.widget.TextView

import com.owfar.android.R
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message

import io.realm.RealmList

class MessengerCommentViewHolder(view: View) : MessengerItemViewHolder(view) {

    private val tvMessage: TextView

    init {
        tvMessage = view.findViewById(R.id.item_messenger_tvMessage) as TextView
    }

    //region Configuration Of View
    override fun configureWithMessage(streamType: StreamType?, message: Message?,
                                      showStatus: Boolean, users: RealmList<User>?) {
        super.configureWithMessage(streamType, message, showStatus, users)
        tvMessage.text = message?.content
    }
    //endregion
}