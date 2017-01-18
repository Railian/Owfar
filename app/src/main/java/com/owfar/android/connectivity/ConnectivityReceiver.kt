package com.owfar.android.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo

abstract class ConnectivityReceiver : BroadcastReceiver(), NetworkInfoListener {

    companion object {
        val intentFilter: IntentFilter
            get() = IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION)
    }

    //region BroadcastReceiver Methods
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val networkConnected = activeNetworkInfo?.isConnected ?: false

        onReceiveNetworkInfo(networkConnected, activeNetworkInfo)
    }
    //endregion

    //region NetworkInfoListener Implementation
    abstract override fun onReceiveNetworkInfo(networkConnected: Boolean, activeNetworkInfo: NetworkInfo?)
    //endregion
}
