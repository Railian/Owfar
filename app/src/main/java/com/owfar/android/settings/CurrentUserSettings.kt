package com.owfar.android.settings

import android.content.Context
import android.content.SharedPreferences
import com.owfar.android.InvalidTokenException

import com.owfar.android.api.ApiFactory
import com.owfar.android.models.CountryCode
import com.owfar.android.models.api.classes.User

// TODO: 29.03.16 Save lastCountryCode
object CurrentUserSettings {

    //region constants
    const private val SETTINGS_CURRENT_USER = "SETTINGS_CURRENT_USER"

    const private val PREF_TOKEN_TYPE = "PREF_TOKEN_TYPE"
    const private val PREF_ACCESS_TOKEN = "PREF_ACCESS_TOKEN"

    const private val PREF_CURRENT_USERNAME = "PREF_CURRENT_USERNAME"
    const private val PREF_LAST_PHONE_NUMBER = "PREF_LAST_PHONE_NUMBER"
    const private val PREF_LAST_COUNTRY_CODE = "PREF_LAST_COUNTRY_CODE"

    const private val PREF_CURRENT_USER = "PREF_CURRENT_USER"
    //endregion

    //region fields
    private var settings: SharedPreferences? = null
    //endregion

    //region Initialization
    fun init(appContext: Context) {
        settings = appContext.getSharedPreferences(SETTINGS_CURRENT_USER, Context.MODE_PRIVATE)
    }
    //endregion

    //region Getters And Setters
    var tokenType: String?
        get() = settings?.getString(PREF_TOKEN_TYPE, null) ?: throw InvalidTokenException()
        set(tokenType) = settings?.edit()?.putString(PREF_TOKEN_TYPE, tokenType)?.apply() ?: Unit

    var accessToken: String?
        get() = settings?.getString(PREF_ACCESS_TOKEN, null) ?: throw InvalidTokenException()
        set(accessToken) = settings?.edit()?.putString(PREF_ACCESS_TOKEN, accessToken)?.apply() ?: Unit

    var currentUsername: String?
        get() = settings?.getString(PREF_CURRENT_USERNAME, null) ?: throw InvalidTokenException()
        set(currentUsername) = settings?.edit()?.putString(PREF_CURRENT_USERNAME, currentUsername)?.apply() ?: Unit

    var currentUser: User?
        get() = ApiFactory.GSON.fromJson(settings?.getString(PREF_CURRENT_USER, null), User::class.java) ?: throw InvalidTokenException()
        set(user) = settings?.edit()?.putString(PREF_CURRENT_USER, ApiFactory.GSON.toJson(user))?.apply() ?: Unit

    var lastCountryCode: CountryCode?
        get() = CountryCode.find(settings?.getString(PREF_LAST_COUNTRY_CODE, null))
        set(countryCode) = settings?.edit()?.putString(PREF_LAST_COUNTRY_CODE, countryCode?.code)?.apply() ?: Unit

    var lastPhoneNumber: String?
        get() = settings?.getString(PREF_LAST_PHONE_NUMBER, null)
        set(lastPhoneNumber) = settings?.edit()?.putString(PREF_LAST_PHONE_NUMBER, lastPhoneNumber)?.apply() ?: Unit
    //endregion

    //region toString
    override fun toString() = "CurrentUserSettings{tokenType='$tokenType'" +
            ", accessToken='$accessToken', currentUsername='$currentUsername'" +
            ", lastPhoneNumber='$lastPhoneNumber', lastCountryCode='$lastCountryCode'" +
            ", currentUser='$currentUser'}"
    //endregion
}

