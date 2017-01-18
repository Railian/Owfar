package com.owfar.android.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.owfar.android.api.ApiFactory
import com.owfar.android.models.facebook.FacebookProfile
import java.util.*

class FacebookHelper(applicationContext: Context, private val listener: FacebookHelper.FacebookHelperListener?) {

    companion object {
        private val TAG = FacebookHelper::class.java.simpleName
    }

    private val callbackManager: CallbackManager

    //region facebookCallback
    private val facebookCallback = object : FacebookCallback<LoginResult> {

        override fun onSuccess(loginResult: LoginResult) {
            Log.d(TAG, "onSuccess() called with: loginResult = [$loginResult]")
            requestFacebookProfile(loginResult)
        }

        override fun onCancel() {
            Log.w(TAG, "onCancel() called")
        }

        override fun onError(exception: FacebookException) {
            Log.e(TAG, "onError() called with: exception = [$exception]")
        }
    }
    //endregion

    //region Initialization
    init {
        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback)
    }
    //endregion

    //region Requests
    fun requestFacebookProfile(activity: Activity)
            = LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile"))

    fun requestFacebookProfile(fragment: Fragment)
            = LoginManager.getInstance().logInWithReadPermissions(fragment, Arrays.asList("public_profile"))

    private fun requestFacebookProfile(loginResult: LoginResult)
            = GraphRequest.newMeRequest(loginResult.accessToken, requestFacebookProfileCallback).apply {
        parameters = Bundle().apply { putString("fields", "id, first_name, last_name, gender") }
        executeAsync()
    }
    //endregion

    //region Public Tools
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent)
            = callbackManager.onActivityResult(requestCode, resultCode, data)
    //endregion

    //region Callbacks
    private val requestFacebookProfileCallback = GraphRequest.GraphJSONObjectCallback { `object`, response ->
        Log.d(TAG, "onCompleted() called with: object = [$`object`], response = [$response]")
        val facebookProfile = ApiFactory.GSON.fromJson(`object`.toString(), FacebookProfile::class.java)
        if (listener != null && facebookProfile != null)
            listener.onFacebookProfileReceived(facebookProfile)
    }
    //endregion

    //region Interface FacebookHelperListener
    interface FacebookHelperListener {
        fun onFacebookProfileReceived(facebookProfile: FacebookProfile)
    }
    //endregion
}