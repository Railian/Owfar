package com.owfar.android.models.errors

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

class Error private constructor(error: ErrorWithExtra) {

    @SerializedName("code") var code: Int? = null
    @SerializedName("error") var error: String? = null
    @SerializedName("error_description") var errorDescription: String? = null

    init {
        this.code = error.errorCode
        this.error = error.errorMessage
        this.errorDescription = error.errorExtra
        error.fields?.let {
            if (it.isJsonObject) {
                val descriptionBuilder = StringBuilder()
                for (entry in it.entrySet())
                    if (entry != null) {
                        if (descriptionBuilder.isNotEmpty()) descriptionBuilder.append("\n")
                        descriptionBuilder.append(entry.key).append(": ")
                        if (entry.value.isJsonArray)
                            entry.value.asJsonArray
                                    .filter { it.isJsonPrimitive }
                                    .forEach { descriptionBuilder.append(it.asString).append(". ") }
                    }
                if (descriptionBuilder.isNotEmpty())
                    if (errorDescription != null) errorDescription += "\n" + descriptionBuilder.toString()
                    else errorDescription = descriptionBuilder.toString()
            }
        }
    }

    override fun toString()
            = "Error(code=$code, error=$error, errorDescription=$errorDescription)"

    class JsonTypeAdapter : JsonDeserializer<Error> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Error {
            val error = json.asJsonObject.get("error")
            if (error.isJsonPrimitive) return Gson().fromJson<Error>(json, typeOfT)
            return Error(context.deserialize<Any>(error, ErrorWithExtra::class.java) as ErrorWithExtra)
        }
    }
}
