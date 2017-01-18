package com.owfar.android.settings

import android.util.Log
import com.owfar.android.DelegatesSet
import com.owfar.android.api.oauth.AuthDelegate
import com.owfar.android.api.oauth.AuthManager
import com.owfar.android.api.users.UsersDelegate
import com.owfar.android.api.users.UsersManager
import com.owfar.android.contain
import com.owfar.android.data.DataManager
import com.owfar.android.models.api.UserCreation
import com.owfar.android.models.api.classes.Profile
import com.owfar.android.models.errors.Error
import com.owfar.android.models.oauth.Token
import java.net.SocketTimeoutException

object CurrentUserManager {

    //region constants
    @JvmStatic private val TAG = CurrentUserManager::class.java.simpleName

    const private val API_REQUEST_FOR_VERIFICATION_CODE = 1
    const private val API_REQUEST_CREATE_USER = 2
    const private val API_REQUEST_GET_PROFILE_DURING_SIGN_UP = 3
    const private val API_REQUEST_GET_TOKEN_DURING_SIGN_UP = 4
    //endregion

    //region fields
    val delegatesSet: DelegatesSet<CurrentUserDelegate> = DelegatesSet(CurrentUserDelegate::class.java)
    //endregion

    //region Requests
    fun requestForVerificationCode(username: String) {
        Log.d(TAG, "requestForVerificationCode() -> [$username]")
        UsersManager.requestForVerificationCode(TAG, API_REQUEST_FOR_VERIFICATION_CODE, username)
    }

    fun signUp(username: String, code: String) {
        Log.d(TAG, "signUp() -> username = [$username], code = [$code]")
        UsersManager.createUser(TAG, API_REQUEST_CREATE_USER, username, code)
    }

    fun logout() {
        Log.d(TAG, "logout() called")
        with(CurrentUserSettings) {
            tokenType = null
            accessToken = null
            currentUsername = null
            currentUser = null
        }
        DataManager.clearAll()
    }
    //endregion

    //region authDelegate
    private val authDelegate = object : AuthDelegate.SimpleAuthDelegate() {

        override fun onReceiveToken(requestCode: Int?, token: Token) {
            with(CurrentUserSettings) {
                tokenType = token.tokenType
                accessToken = token.accessToken
            }
            UsersManager.getProfile(TAG, API_REQUEST_GET_PROFILE_DURING_SIGN_UP, UsersManager.MY_USER_ID)
        }

        override fun onError(requestCode: Int?, error: Error) {
            if (requestCode == API_REQUEST_GET_TOKEN_DURING_SIGN_UP)
                delegatesSet.notify().onErrorDuringSignUp(error)
        }
    }
    //endregion

    //region usersDelegate
    private val usersDelegate = object : UsersDelegate.Simple() {

        override fun onRequestForVerificationCodeSent(requestCode: Int?) =
                delegatesSet.notify().onRequestForVerificationCodeSent()

        override fun onCreateUser(requestCode: Int?, userCreation: UserCreation) {
            val user = userCreation.user
            val username = user?.phone
            with(CurrentUserSettings) {
                currentUser = user
                currentUsername = username
            }
            username?.let { AuthManager.getToken(TAG, API_REQUEST_GET_TOKEN_DURING_SIGN_UP, it) }
        }

        override fun onProfileReceived(requestCode: Int?, profile: Profile) {
            with(CurrentUserSettings) { currentUser = currentUser?.apply { this.profile = profile } }
            delegatesSet.notify().onAuthorized()
        }

        override fun onError(requestCode: Int?, error: Error) {
            when (requestCode) {
                API_REQUEST_FOR_VERIFICATION_CODE ->
                    delegatesSet.notify().onErrorDuringNumberVerification(error)
                API_REQUEST_CREATE_USER, API_REQUEST_GET_PROFILE_DURING_SIGN_UP ->
                    delegatesSet.notify().onErrorDuringSignUp(error)
            }
        }

        override fun onFailure(requestCode: Int?, throwable: Throwable) = when {
            throwable.contain(SocketTimeoutException::class.java) -> delegatesSet.notify().onTimeoutException()
            else -> Unit
        }
    }
//endregion

    //region Initialization
    init {
        AuthManager.delegatesSet.addDelegate(TAG, authDelegate)
        UsersManager.delegatesSet.addDelegate(TAG, usersDelegate)
    }
    //endregion
}