package com.owfar.android.api.oauth

import com.owfar.android.models.errors.Error
import com.owfar.android.models.oauth.Token
import com.owfar.android.models.oauth.TokenValidation

interface AuthDelegate {

    fun onReceiveToken(requestCode: Int?, token: Token)
    fun onReceiveTokenValidation(requestCode: Int?, validation: TokenValidation)

    fun onError(requestCode: Int?, error: Error)
    fun onFailure(requestCode: Int?, throwable: Throwable)

    //region Simple
    open class SimpleAuthDelegate : AuthDelegate {

        override fun onReceiveToken(requestCode: Int?, token: Token) {
        }

        override fun onReceiveTokenValidation(requestCode: Int?, validation: TokenValidation) {
        }

        override fun onError(requestCode: Int?, error: Error) {
        }

        override fun onFailure(requestCode: Int?, throwable: Throwable) {
        }
    }
    //endregion
}
