package com.owfar.android.ui.registration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.owfar.android.R
import com.owfar.android.models.CountryCode

class RegistrationCountryCodesFragment : RegistrationBaseFragment(), View.OnClickListener,
        CountryCodeItemAdapter.OnCountryCodeClickListener {

    companion object {

        //region constants
        const val TITLE = "Choose Country Code"

        const private val ARG_SELECTED_COUNTRY_CODE = "ARG_SELECTED_COUNTRY_CODE"
        const val EXTRA_SELECTED_COUNTRY_CODE = "EXTRA_SELECTED_COUNTRY_CODE"
        //endregion

        //region Creating New Instances
        fun newInstance(selectedCountryCode: CountryCode?) = RegistrationCountryCodesFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_SELECTED_COUNTRY_CODE, selectedCountryCode) }
        }
        //endregion
    }

    //region widgets
    private var rvCountryCodes: RecyclerView? = null
    //endregion

    //region fields
    private val adapter: CountryCodeItemAdapter?
        get() = rvCountryCodes?.adapter as? CountryCodeItemAdapter

    private var selectedCountryCode: CountryCode? = null
    //endregion

    //region Fragment Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedCountryCode = arguments.getParcelable<CountryCode>(ARG_SELECTED_COUNTRY_CODE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        appBarHelper.setTitle(TITLE)
                .showBackArrow()

        return inflater.inflate(R.layout.fragment_registration_country_codes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCountryCodes = view.findViewById(R.id.fragment_registration_country_codes_rvCountryCodes) as RecyclerView

        rvCountryCodes?.apply {
            adapter = CountryCodeItemAdapter().apply {
                this.selectedCountryCode = selectedCountryCode
                setOnCountryCodeClickListener(this@RegistrationCountryCodesFragment)
            }
            layoutManager = LinearLayoutManager(activity).apply {
                selectedCountryCode?.let {
                    this@RegistrationCountryCodesFragment.adapter?.getCountryCodeAdapterPosition(it)?.let {
                        scrollToPositionWithOffset(it, 0)
                    }
                }
            }
        }
    }
    //endregion

    //region UI Listeners Implementation
    override fun onClick(v: View) = fragmentManager.popBackStack()
    //endregion

    //region OnCountryCodeClickListener Implementation
    override fun onCountryCodeClick(adapterPosition: Int, countryCode: CountryCode) {
        val result = Intent().putExtra(EXTRA_SELECTED_COUNTRY_CODE, countryCode)
        targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK, result)
        fragmentManager.popBackStack()
    }
    //endregion
}
