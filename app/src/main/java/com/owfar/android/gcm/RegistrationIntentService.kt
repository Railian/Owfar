package com.owfar.android.gcm

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.gcm.GcmPubSub
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.owfar.android.R
import com.owfar.android.api.users.UsersManager
import com.owfar.android.data.logFun
import java.io.IOException

class RegistrationIntentService : IntentService(RegistrationIntentService.TAG) {

    companion object {
        @JvmStatic private val TAG = RegistrationIntentService::class.java.simpleName
        private val TOPICS = arrayOf("global")
        const private val API_REQUEST_REGISTER_DEVICE_TOKEN = 1
    }

    override fun onHandleIntent(intent: Intent?) {
        logFun(TAG, RegistrationIntentService::onHandleIntent, intent)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        try {
            val instanceID = InstanceID.getInstance(this)
            val token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)
            Log.i(TAG, "GCM Registration Token: " + token)

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token)

            // Subscribe to topic channels
            subscribeTopics(token)

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply()
            // [END register_for_gcm]
        } catch (e: Exception) {
            Log.d(TAG, "Failed to complete token refresh", e)
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply()
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        val registrationComplete = Intent(QuickstartPreferences.REGISTRATION_COMPLETE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)
    }

    /**
     * Persist registration to third-party servers.
     *
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.

     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        UsersManager.registerDeviceToken(TAG, API_REQUEST_REGISTER_DEVICE_TOKEN, token)
        // Add custom implementation, as needed.
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.

     * @param token GCM token
     * *
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    @Throws(IOException::class)
    private fun subscribeTopics(token: String) {
        val pubSub = GcmPubSub.getInstance(this)
        for (topic in TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null)
        }
    }
    // [END subscribe_topics]

    override fun onCreate() {
        super.onCreate()
        logFun(TAG, RegistrationIntentService::onCreate)
    }

    override fun onDestroy() {
        logFun(TAG, RegistrationIntentService::onDestroy)
        super.onDestroy()
    }

}