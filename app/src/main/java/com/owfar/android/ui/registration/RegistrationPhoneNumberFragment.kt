package com.owfar.android.ui.registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.helpers.KeyboardHelper
import com.owfar.android.models.CountryCode
import com.owfar.android.ui.snackbars.DefaultSnackbar

class RegistrationPhoneNumberFragment : RegistrationBaseFragment(), TextWatcher {

    companion object {

        //region constants
        @JvmStatic val TAG = RegistrationPhoneNumberFragment::class.java.simpleName
        const val TITLE = "Phone Number"

        const private val ARG_LAST_COUNTRY_CODE = "ARG_LAST_COUNTRY_CODE"
        const private val ARG_LAST_PHONE_NUMBER = "ARG_LAST_PHONE_NUMBER"

        const private val STATE_COUNTRY_CODE = "STATE_COUNTRY_CODE"

        const private val REQUEST_CHOOSE_COUNTRY_CODE = 1
        //endregion

        //region Creating New Instances
        fun newInstance(lastCountryCode: CountryCode?, lastPhoneNumber: String?) =
                RegistrationPhoneNumberFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_LAST_COUNTRY_CODE, lastCountryCode)
                        putString(ARG_LAST_PHONE_NUMBER, lastPhoneNumber)
                    }
                }
        //endregion
    }

    //region widgets
    private var vCountryCode: View? = null
    private var tvCountryName: TextView? = null
    private var tvDialCode: TextView? = null
    private var tvCodeLabel: TextView? = null
    private var etPhoneNumber: EditText? = null
    private var btContinue: Button? = null

    private var sbEditTextError: Snackbar? = null
    //endregion

    //region arguments
    private var countryCode: CountryCode? = null
    private var lastPhoneNumber: String? = null
    //endregion

    //region Fragment Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        countryCode = savedInstanceState?.getParcelable<CountryCode>(STATE_COUNTRY_CODE)
                ?: arguments.getParcelable<CountryCode>(ARG_LAST_COUNTRY_CODE)
                ?: calculateDefaultCountryCode()
        lastPhoneNumber = arguments.getString(ARG_LAST_PHONE_NUMBER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        appBarHelper.setTitle(TITLE)
                .showCloseApp()

        return inflater.inflate(R.layout.fragment_registration_phone_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            vCountryCode = findViewById(R.id.fragment_registration_phone_number_vCountryCode)
            tvCountryName = findViewById(R.id.fragment_registration_phone_number_tvCountryName) as TextView
            tvDialCode = findViewById(R.id.fragment_registration_phone_number_tvDialCode) as TextView
            tvCodeLabel = findViewById(R.id.fragment_registration_phone_number_tvCodeLabel) as TextView
            etPhoneNumber = findViewById(R.id.fragment_registration_phone_number_etPhoneNumber) as EditText
            btContinue = findViewById(R.id.fragment_registration_phone_number_btContinue) as Button
        }

        configureCountryCode()
        etPhoneNumber?.apply {
            if (savedInstanceState == null) setText(lastPhoneNumber)
            filters = arrayOf(phoneNumberInputFilter)
            addTextChangedListener(this@RegistrationPhoneNumberFragment)
            setOnEditorActionListener { v, actionId, event -> btContinue?.performClick() ?: false }
        }
        btContinue?.setOnClickListener { onContinueClick() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_COUNTRY_CODE, countryCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        countryCode = data?.getParcelableExtra<CountryCode>(RegistrationCountryCodesFragment.EXTRA_SELECTED_COUNTRY_CODE)
        configureCountryCode()
    }

    override fun onDestroyView() {
        hideEditTextError()
        super.onDestroyView()
    }
    //endregion

    //region View Configuration
    private fun configureCountryCode() {
        vCountryCode?.setOnClickListener { onCountryCodeClick() }
        countryCode?.let {
            tvCountryName?.text = it.name
            tvDialCode?.text = "(${it.dialCode})"
            tvCodeLabel?.text = it.code
        }
    }
    //endregion

    //region Private Tools
    private fun hideEditTextError()
            = sbEditTextError?.dismiss().let { sbEditTextError = null }

    private fun calculateDefaultCountryCode(): CountryCode {
        val tm = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val code = tm.simCountryIso.toUpperCase()
        return CountryCode.find(code) ?: CountryCode.find("NG")!!
    }
    //endregion

    //region UI Listeners Implementation
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
            = hideEditTextError()

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
    override fun afterTextChanged(s: Editable) = Unit

    private fun onCountryCodeClick() = registrationActivity
            .showCountryCodeFragment(this, REQUEST_CHOOSE_COUNTRY_CODE, vCountryCode, countryCode)

    private fun onContinueClick() {
        KeyboardHelper.hideKeyboard()
        etPhoneNumber?.text?.toString()?.let {
            if (!it.isBlank()) registrationActivity?.showNumberVerificationFragment(countryCode, it)
            else null
        } ?: let {
            sbEditTextError = DefaultSnackbar.make(view, "Phone number is required").apply { show() }
        }
    }
    //endregion

    //region phoneNumberInputFilter
    private val phoneNumberInputFilter = InputFilter { source, start, end, dest, dstart, dend ->
        StringBuilder().apply { source?.forEach { if (Character.isDigit(it)) append(it) } }.toString()
    }
    //endregion
}