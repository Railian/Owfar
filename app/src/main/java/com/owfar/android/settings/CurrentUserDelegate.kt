package com.owfar.android.settings

import com.owfar.android.models.CountryCode
import com.owfar.android.models.errors.Error

interface CurrentUserDelegate {

    fun onAuthorized()
    fun onNeedRegistration(lastCountryCode: CountryCode?, lastPhoneNumber: String?)
    fun onRequestForVerificationCodeSent()
    fun onErrorDuringAuthorization(error: Error)
    fun onErrorDuringNumberVerification(error: Error)
    fun onErrorDuringSignUp(error: Error)
    fun onTimeoutException()

    open class Simple : CurrentUserDelegate {
        override fun onAuthorized() = Unit
        override fun onNeedRegistration(lastCountryCode: CountryCode?, lastPhoneNumber: String?) = Unit
        override fun onRequestForVerificationCodeSent() = Unit
        override fun onErrorDuringAuthorization(error: Error) = Unit
        override fun onErrorDuringNumberVerification(error: Error) = Unit
        override fun onErrorDuringSignUp(error: Error) = Unit
        override fun onTimeoutException() = Unit
    }
}