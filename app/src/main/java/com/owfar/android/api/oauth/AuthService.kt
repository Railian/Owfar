package com.owfar.android.api.oauth


import com.owfar.android.api.ApiFactory
import com.owfar.android.models.oauth.Token
import com.owfar.android.models.oauth.TokenValidation
import retrofit2.Call
import retrofit2.http.*

interface AuthService {

    companion object {
        const val OAUTH_PREFIX = ApiFactory.API_PREFIX + "/oauth"
    }

    @POST(OAUTH_PREFIX + "/token")
    @FormUrlEncoded
    fun getToken(
            @Field("grant_type") grantType: String,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("username") username: String,
            @Field("password") password: String
    ): Call<Token>

    @GET(OAUTH_PREFIX + "/validate")
    fun validateAccessToken(
            @Query("access_token") accessToken: String
    ): Call<TokenValidation>

}
