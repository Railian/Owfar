package com.owfar.android.ui.registration

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.owfar.android.R
import com.owfar.android.models.CountryCode

class CountryCodeItemAdapter : RecyclerView.Adapter<CountryCodeItemHolder>(),
        CountryCodeItemHolder.OnCountryCodeClickListener {

    //region Data-Source Methods
    var selectedCountryCode: CountryCode? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun getCountryCode(adapterPosition: Int)
            = CountryCode.VALUES[adapterPosition]
    //endregion

    //region Adapter Methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryCodeItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_country_code, parent, false)
        return CountryCodeItemHolder(itemView).apply {
            setOnCountryCodeClickListener(this@CountryCodeItemAdapter)
        }
    }

    override fun onBindViewHolder(holder: CountryCodeItemHolder, adapterPosition: Int) {
        getCountryCode(adapterPosition).let { holder.configureWith(it, it == selectedCountryCode) }
    }

    override fun getItemCount() = CountryCode.VALUES.size

    fun getCountryCodeAdapterPosition(countryCode: CountryCode): Int? {
        CountryCode.VALUES.forEachIndexed { i, it -> if (it == countryCode) return i }
        return null
    }
    //endregion

    //region OnCountryCodeClickListener
    private var listener: OnCountryCodeClickListener? = null

    fun setOnCountryCodeClickListener(listener: OnCountryCodeClickListener) {
        this.listener = listener
    }

    override fun onCountryCodeClick(adapterPosition: Int)
            = listener?.onCountryCodeClick(adapterPosition, getCountryCode(adapterPosition)) ?: Unit

    interface OnCountryCodeClickListener {
        fun onCountryCodeClick(adapterPosition: Int, countryCode: CountryCode)
    }
    //endregion
}
