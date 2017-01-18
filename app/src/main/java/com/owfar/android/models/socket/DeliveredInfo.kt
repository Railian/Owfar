package com.owfar.android.models.socket

import com.google.gson.annotations.SerializedName

data class DeliveredInfo(
        @SerializedName("user_id") val userId: Long? = null,
        @SerializedName("id") val id: Long? = null
)