package com.owfar.android.connectivity

import android.net.NetworkInfo

interface NetworkInfoListener {
    fun onReceiveNetworkInfo(networkConnected: Boolean, activeNetworkInfo: NetworkInfo?)
}
