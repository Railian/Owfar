package com.owfar.android.models.socket

import com.google.gson.annotations.SerializedName
import com.owfar.android.models.api.enums.StreamType

data class SubscribeData(
        @SerializedName("streamType") var streamType: String? = null,
        @SerializedName("streamId") var streamId: Long? = null
)