package com.owfar.android.models.facebook

import com.google.gson.annotations.SerializedName
import java.net.MalformedURLException
import java.net.URL

class FacebookProfile(
        @SerializedName("id") var id: Long = 0,
        @SerializedName("first_name") var firstName: String? = null,
        @SerializedName("last_name") var lastName: String? = null,
        @SerializedName("gender") var gender: String? = null
) {
    fun getPhotoUrl(width: Int, height: Int): URL? = try {
        URL("https://graph.facebook.com/$id/picture?width=$width&height=$height")
    } catch (e: MalformedURLException) {
        null
    }
}
