package com.owfar.android.models.oauth

import com.google.gson.annotations.SerializedName
import com.owfar.android.models.api.classes.User

class TokenValidation(
        @SerializedName("valid") var isValid: Boolean = false,
        @SerializedName("user") var user: User? = null
)
