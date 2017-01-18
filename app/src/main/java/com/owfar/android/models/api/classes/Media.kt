package com.owfar.android.models.api.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.owfar.android.extensions.asStringOrNull
import com.owfar.android.models.api.enums.MediaSize
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.lang.reflect.Type

open class Media(
        @SerializedName("media_id") var mediaId: Long? = null,
        @SerializedName("header") var header: String? = null,
        @SerializedName("content") var content: String? = null,
        @SerializedName("media_file_id") @PrimaryKey var mediaFileId: Long = -1,
        @SerializedName("name") var name: String? = null,
        @SerializedName("bucket") var bucket: String? = null,
        @SerializedName("model_type") var modelType: String? = null,
        @SerializedName("path_default") private var pathDefault: String? = null,
        @SerializedName("path_1x") private var path1x: String? = null,
        @SerializedName("path_2x") private var path2x: String? = null,
        @SerializedName("path_3x") private var path3x: String? = null
) : RealmObject(), Parcelable {

    fun getPath(mediaSize: MediaSize? = MediaSize._DEFAULT) = when (mediaSize) {
        MediaSize._1X -> path1x
        MediaSize._2X -> path2x
        MediaSize._3X -> path3x
        MediaSize._DEFAULT -> pathDefault
        else -> pathDefault
    }

    fun setPath(path: String?, mediaSize: MediaSize? = MediaSize._DEFAULT) {
        when (mediaSize) {
            MediaSize._1X -> path1x = path
            MediaSize._2X -> path2x = path
            MediaSize._3X -> path3x = path
            MediaSize._DEFAULT -> pathDefault = path
            else -> pathDefault = path
        }
    }

    //region toString
    override fun toString() = "Media(mediaId=$mediaId, header=$header, content=$content" +
            ", mediaFileId=$mediaFileId, name=$name, bucket=$bucket, modelType=$modelType" +
            ", pathDefault=$pathDefault, path1x=$path1x, path2x=$path2x, path3x=$path3x)"
    //endregion

    //region Parcelable Implementation
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeValue(mediaId)
            writeString(header)
            writeString(content)
            writeLong(mediaFileId)
            writeString(name)
            writeString(bucket)
            writeString(modelType)
            writeString(pathDefault)
            writeString(path1x)
            writeString(path2x)
            writeString(path3x)
        }
    }

    constructor(source: Parcel) : this(
            source.readValue(Long::class.java.classLoader) as? Long,
            source.readString(),
            source.readString(),
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Media> = object : Parcelable.Creator<Media> {
            override fun createFromParcel(source: Parcel): Media = Media(source)
            override fun newArray(size: Int): Array<Media?> = arrayOfNulls(size)
        }
    }
    //endregion

    //region JsonTypeAdapter
    class JsonTypeAdapter : JsonSerializer<Media>, JsonDeserializer<Media> {

        override fun serialize(src: Media?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            src?.apply {
                val mediaFile = JsonObject().apply {
                    addProperty("id", mediaFileId)
                    addProperty("name", name)
                    addProperty("bucket", bucket)
                    addProperty("model_type", modelType)
                    add("path", JsonObject().apply {
                        addProperty("1x", getPath(MediaSize._1X))
                        addProperty("2x", getPath(MediaSize._2X))
                        addProperty("3x", getPath(MediaSize._3X))
                        addProperty("default", getPath(MediaSize._DEFAULT))
                    })
                }
                mediaId?.let {
                    return JsonObject().apply {
                        addProperty("id", src.mediaId)
                        addProperty("header", src.header)
                        addProperty("content", src.content)
                        add("media_file", mediaFile)
                    }
                } ?: return mediaFile
            }
            return JsonNull.INSTANCE
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?) = Media().apply {
            json?.asJsonObject?.let { json ->
                if (json.has("media_file")) {
                    mediaId = json.get("id")?.asLong
                    header = json.get("header")?.asStringOrNull
                    content = json.get("content")?.asStringOrNull
                }
                (json.get("media_file")?.asJsonObject ?: json).let { json ->
                    mediaFileId = json.get("id")?.asLong ?: -1
                    name = json.get("name")?.asStringOrNull
                    bucket = json.get("bucket")?.asStringOrNull
                    modelType = json.get("model_type")?.asStringOrNull
                    json.get("path")?.asJsonObject?.let { json ->
                        setPath(json.get("1x")?.asStringOrNull, MediaSize._1X)
                        setPath(json.get("2x")?.asStringOrNull, MediaSize._2X)
                        setPath(json.get("3x")?.asStringOrNull, MediaSize._3X)
                        setPath(json.get("default")?.asStringOrNull, MediaSize._DEFAULT)
                    }
                }
            }
        }
    }
    //endregion
}
