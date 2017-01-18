package com.owfar.android.gcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (checkPlayServices(context))
            context.startService(Intent(context, RegistrationIntentService::class.java))
    }

    private fun checkPlayServices(context: Context) = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
}
