package com.owfar.android.models.oauth

import com.google.gson.annotations.SerializedName

class Token(
        @SerializedName("token_type") var tokenType: String? = null,
        @SerializedName("access_token") var accessToken: String? = null
)
