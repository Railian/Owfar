package com.owfar.android.connectivity

import android.content.Context
import android.net.NetworkInfo

object ConnectivityManager {

    //region constants
    @JvmStatic private val TAG = ConnectivityManager::class.java.simpleName
    //endregion

    //region Initialisation
    fun init(context: Context)
            = context.registerReceiver(connectivityReceiver, ConnectivityReceiver.intentFilter)

    fun release(context: Context)
            = context.unregisterReceiver(connectivityReceiver)
    //endregion

    //region Tools
    var isNetworkConnected: Boolean = false
        private set
    var activeNetworkInfo: NetworkInfo? = null
        private set
    private var networkInfoListener: NetworkInfoListener? = null

    fun setNetworkInfoListener(listener: NetworkInfoListener) {
        networkInfoListener = listener
    }
    //endregion

    //region connectivityReceiver
    private val connectivityReceiver = object : ConnectivityReceiver() {
        override fun onReceiveNetworkInfo(networkConnected: Boolean, activeNetworkInfo: NetworkInfo?) {
            this@ConnectivityManager.isNetworkConnected = networkConnected
            this@ConnectivityManager.activeNetworkInfo = activeNetworkInfo
            networkInfoListener?.onReceiveNetworkInfo(networkConnected, activeNetworkInfo)
        }
    }
    //endregion
}
