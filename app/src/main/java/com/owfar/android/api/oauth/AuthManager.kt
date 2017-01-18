package com.owfar.android.api.oauth

import android.util.Log
import com.owfar.android.DelegatesSet
import com.owfar.android.InvalidTokenException
import com.owfar.android.api.ApiCallback
import com.owfar.android.api.ApiFactory
import com.owfar.android.models.errors.Error
import com.owfar.android.models.oauth.Token
import com.owfar.android.models.oauth.TokenValidation

object AuthManager {

    //region constants
    @JvmStatic val TAG = AuthManager::class.java.simpleName

    const private val GRANT_TYPE = "password"
    const private val CLIENT_ID = "jO6hEit7Jurc3Ew0iv9Li7he"
    const private val CLIENT_SECRET = "Ju4Ik0Ol8I1oJ8E6tyUt3Thoig"
    //endregion

    //region fields
    private val authService = ApiFactory.authService
    val delegatesSet = DelegatesSet(AuthDelegate::class.java)
    //endregion

    //region Callback Creator
    private fun <T> apiCallback(responseType: Class<T>, tag: String?, requestCode: Int?,
                                onSuccess: (T) -> Unit) = object : ApiCallback<T>() {
        override fun onSuccess(body: T) {
            Log.d(TAG, "TAG: $tag -> onSuccess: $body")
            onSuccess(body)
        }

        override fun onError(error: Error) {
            Log.d(TAG, "TAG: $tag -> onError: $error")
            delegatesSet.notify(tag).onError(requestCode, error)
            if (error.code == 401 && error.error == "invalid_token")
                throw InvalidTokenException()
        }

        override fun onFailure(t: Throwable) {
            Log.d(TAG, "TAG: $tag -> onFailure: $t")
            delegatesSet.notify(tag).onFailure(requestCode, t)
        }
    }
    //endregion

    //region Requests
    fun getToken(tag: String, requestCode: Int, username: String) {
        val call = authService.getToken(GRANT_TYPE, CLIENT_ID, CLIENT_SECRET, username, "exists")
        call.enqueue(apiCallback(Token::class.java, tag, requestCode) { token ->
            delegatesSet.notify(tag).onReceiveToken(requestCode, token)
        })
    }

    fun validateAccessToken(tag: String, requestCode: Int, accessToken: String) {
        val call = authService.validateAccessToken(accessToken)
        call.enqueue(apiCallback(TokenValidation::class.java, tag, requestCode) { tokenValidation ->
            delegatesSet.notify(tag).onReceiveTokenValidation(requestCode, tokenValidation)
        })
    }
    //endregion
}