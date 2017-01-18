package com.owfar.android.socket

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.owfar.android.api.ApiFactory
import com.owfar.android.models.api.classes.SentMessage
import com.owfar.android.models.api.enums.MessageBodyType
import org.json.JSONException
import org.json.JSONObject

abstract class BaseSocketHelper {

    protected fun logArg(event: String?, arg: Any)
            = logArgs(event, arrayOf(arg))

    protected fun logArgs(event: String?, args: Array<out Any>)
            = Log.d(SocketManager.TAG, "$event -> args = ${args.asList()}")

    protected fun <T> convertFirstArgFromJson(clazz: Class<T>, args: Array<out Any>) =
            args.firstOrNull()?.let { convertFromJson(it, clazz) }

    protected fun <T> convertFromJson(any: Any?, clazz: Class<T>): T? = try {
        ApiFactory.GSON.fromJson(any?.toString(), clazz)
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        null
    }

    protected fun convertToJSONObject(src: Any?): JSONObject? = try {
        JSONObject(ApiFactory.GSON.toJson(src)).apply {
            if (src is SentMessage && MessageBodyType.find(src.bodyType) == MessageBodyType.STICKER) {
                put("content", convertToJSONObject(src.sticker))
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        null
    }
}
