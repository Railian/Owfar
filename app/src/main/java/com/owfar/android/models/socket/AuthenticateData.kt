package com.owfar.android.models.socket

import com.google.gson.annotations.SerializedName

data class AuthenticateData(
        @SerializedName("token") var token: String? = null,
        @SerializedName("device_token") var deviceToken: String? = null
)