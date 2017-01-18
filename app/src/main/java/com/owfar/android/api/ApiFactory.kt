package com.owfar.android.api

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.owfar.android.api.file.DownloadProgressInterceptor
import com.owfar.android.api.file.FileService
import com.owfar.android.api.file.UploadProgressInterceptor
import com.owfar.android.api.oauth.AuthService
import com.owfar.android.api.users.UsersService
import com.owfar.android.models.api.classes.Media
import com.owfar.android.models.api.classes.ReceivedMessage
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.models.errors.Error
import io.realm.RealmObject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiFactory {

    //region constants
    private const val BASE_URL = "http://52.35.131.155:3000"
    const val API_PREFIX = "/api"

    private const val CONNECT_TIMEOUT = 10000
    private const val WRITE_TIMEOUT = 120000
    private const val READ_TIMEOUT = 60000

    private val CLIENT = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(DownloadProgressInterceptor())
            .addNetworkInterceptor(UploadProgressInterceptor())
            .build()

    @JvmStatic val GSON = GsonBuilder()
            .registerTypeAdapter(Error::class.java, Error.JsonTypeAdapter())
            .registerTypeAdapter(Media::class.java, Media.JsonTypeAdapter())
            .registerTypeAdapter(ReceivedMessage::class.java, ReceivedMessage.JsonTypeAdapter())
            .setExclusionStrategies(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>?) = false
                override fun shouldSkipField(f: FieldAttributes?) = f?.declaringClass?.equals(RealmObject::class) ?: false
            })
            .create()!!
    //endregion

    //region Public Tools
    @JvmStatic val authService: AuthService =
            getRetrofit(BASE_URL).create(AuthService::class.java)
    @JvmStatic val usersService: UsersService = getRetrofit(BASE_URL).create(UsersService::class.java)
    @JvmStatic val fileService: FileService = getRetrofit(BASE_URL).create(FileService::class.java)
    //endregion

    //region Private Tools
    private fun getRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .client(CLIENT)
                .build()
    }
    //endregion
}