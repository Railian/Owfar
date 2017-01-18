package com.owfar.android.models.errors

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

class ErrorWithExtra(
        @SerializedName("error_code") var errorCode: Int = 0,
        @SerializedName("error_message") var errorMessage: String? = null,
        @SerializedName("error_extra") var errorExtra: String? = null,
        @SerializedName("fields") var fields: JsonObject? = null
)