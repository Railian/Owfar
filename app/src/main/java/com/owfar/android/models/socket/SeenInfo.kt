package com.owfar.android.models.socket

import com.google.gson.annotations.SerializedName

@Suppress("ArrayInDataClass")
data class SeenInfo(
        @SerializedName("user_id") val userId: Long? = null,
        @SerializedName("ids") val ids: Array<Long>? = null
)
