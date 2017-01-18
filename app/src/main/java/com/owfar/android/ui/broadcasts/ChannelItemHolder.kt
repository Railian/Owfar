package com.owfar.android.ui.broadcasts

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

import com.owfar.android.R
import com.owfar.android.models.api.classes.Stream

class ChannelItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    //region widgets
    private val vSubscribed: View
    private val cbSubscribed: CheckBox
    private val tvName: TextView
    private val ivArrow: ImageView
    //endregion

    init {
        vSubscribed = itemView.findViewById(R.id.item_broadcast_vSubscribed)
        cbSubscribed = itemView.findViewById(R.id.item_broadcast_cbSubscribed) as CheckBox
        tvName = itemView.findViewById(R.id.item_broadcast_tvName) as TextView
        ivArrow = itemView.findViewById(R.id.item_broadcast_ivArrow) as ImageView
        vSubscribed.setOnClickListener(this)
    }

    //region View Configuration
    fun configureWith(stream: Stream) {
        cbSubscribed.isChecked = stream.asInterest?.isSubscribed ?: false
        tvName.text = stream.asInterest?.name
    }
    //endregion

    //region UI Listeners Implementation
    override fun onClick(v: View) {
        when {
            v === vSubscribed -> cbSubscribed.performClick()
            v === itemView -> listener?.onInterestClick(adapterPosition)
            v === cbSubscribed -> listener?.onInterestCheckedChanged(adapterPosition, cbSubscribed.isChecked)
        }
    }
    //endregion

    //region InterestListener
    private var listener: InterestListener? = null

    fun setInterestListener(listener: InterestListener?) {
        this.listener = listener
        itemView.setOnClickListener(listener?.let { this })
        cbSubscribed.setOnClickListener(listener?.let { this })
    }

    interface InterestListener {
        fun onInterestClick(adapterPosition: Int)
        fun onInterestCheckedChanged(adapterPosition: Int, isChecked: Boolean)
    }
    //endregion
}
