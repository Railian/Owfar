package com.owfar.android.api

import android.util.Log
import com.owfar.android.data.logFun
import com.owfar.android.models.errors.Error
import retrofit2.Call
import retrofit2.Response

abstract class ApiCallback<T> : retrofit2.Callback<T> {

    companion object {
        @JvmStatic private val TAG = ApiCallback::class.java.simpleName
    }

    //region Retrofit2 Callback Implementation
    override fun onResponse(call: Call<T>, response: Response<T>) {
        logFun(TAG, ApiCallback<T>::onResponse, call, response)
        with(response) {
            body()?.let { body ->
                onSuccess(body)
            } ?: errorBody()?.let {
                tryOrNull { it.string() }?.let { rawError ->
                    ApiFactory.GSON.fromJson(rawError, Error::class.java)
                            ?.let { error -> onError(error) }
                            ?: let { onFailure(Throwable(rawError)) }
                }
            }
        }
    }

    override fun onFailure(call: Call<T>, throwable: Throwable) {
        Log.w(TAG, "onFailure() -> ${listOf(call, throwable)}")
        onFailure(throwable)
    }
    //endregion

    //region New Callback Methods
    abstract fun onSuccess(body: T)

    open fun onError(error: Error) = Unit
    open fun onFailure(throwable: Throwable) = Unit
    //endregion
}

fun <T> tryOrNull(function: () -> T) = try {
    function()
} catch (throwable: Throwable) {
    throwable.printStackTrace()
    null
}
