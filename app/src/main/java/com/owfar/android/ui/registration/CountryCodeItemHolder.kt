package com.owfar.android.ui.registration

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import com.owfar.android.R
import com.owfar.android.models.CountryCode

class CountryCodeItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    //region widgets
    private val tvCountryName: TextView
    private val tvDialCode: TextView
    private val tvCode: TextView
    //endregion

    //region widgets
    init {
        tvCountryName = itemView.findViewById(R.id.item_country_code_tvCountryName) as TextView
        tvDialCode = itemView.findViewById(R.id.item_country_code_tvDialCode) as TextView
        tvCode = itemView.findViewById(R.id.item_country_code_tvCode) as TextView
    }
    //endregion

    //region View Configuration
    fun configureWith(countryCode: CountryCode, isSelected: Boolean) {
        with(countryCode) {
            tvCountryName.text = name
            tvDialCode.hint = dialCode
            tvCode.text = code
            tvCode.isActivated = isSelected
        }
    }
    //endregion

    //region OnCountryCodeClickListener
    private var listener: OnCountryCodeClickListener? = null

    fun setOnCountryCodeClickListener(listener: OnCountryCodeClickListener?) {
        itemView.setOnClickListener(listener?.let { this })
        this.listener = listener
    }

    override fun onClick(v: View)
            = listener?.onCountryCodeClick(adapterPosition) ?: Unit

    interface OnCountryCodeClickListener {
        fun onCountryCodeClick(adapterPosition: Int)
    }
    //endregion
}