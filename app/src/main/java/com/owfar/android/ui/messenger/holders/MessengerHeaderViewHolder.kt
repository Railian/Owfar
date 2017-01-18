package com.owfar.android.ui.messenger.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.extensions.formattedDate
import com.owfar.android.extensions.isToday
import com.owfar.android.extensions.isYesterday
import java.util.*

class MessengerHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val tvDate: TextView?

    init {
        tvDate = view.findViewById(R.id.item_messenger_header_section_tvDate) as? TextView
    }

    //region View Configuration
    fun configureWith(date: Date?) {
        tvDate?.text = with(checkNotNull(date)) {
            if (isToday()) "Today" else if (isYesterday()) "Yesterday" else formattedDate }
    }
    //endregion
}