package com.owfar.android.media

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.support.annotation.DrawableRes
import android.widget.ImageView
import com.bumptech.glide.DrawableTypeRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.Target
import com.owfar.android.DelegatesSet
import com.owfar.android.api.file.FileDelegate
import com.owfar.android.api.file.FileManager
import com.owfar.android.models.api.classes.Media
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.io.File
import java.util.*

object MediaHelper {

    //region constants
    private const val MAIN_STORAGE_NAME = "Owfar"
    private const val MEDIA_STORAGE_NAME = "media"
    //endregion

    //region Private Tools
    private val rootMediaStorageDir: File
        get() = File(Environment.getExternalStorageDirectory().toString()
                + "/" + MAIN_STORAGE_NAME, MEDIA_STORAGE_NAME).apply { mkdirs() }

    private fun getMediaStorageDir(type: MediaStorageType): File
            = File(rootMediaStorageDir, type.storageName).apply { mkdirs() }
    //endregion

    //region fields
    private var handler: Handler? = null
    private var picasso: Picasso? = null
    private var glide: RequestManager? = null
    private val glideTargetsMap = HashMap<ImageView, Target<*>>()
    private val downloadingFilesKeys = HashMap<ImageView, String>()
    //endregion

    //region Initialization
    fun init(context: Context) {
        handler = Handler(context.mainLooper)
        picasso = Picasso.with(context)
        glide = Glide.with(context)
    }
    //endregion

    val delegatesSet = DelegatesSet(FileDelegate::class.java)

    //region Public Tools
    fun load(media: Media?) = OptionsCreator(media)

    fun load(uri: Uri) = RequestCreator(uri)

    fun load(@DrawableRes drawableRes: Int) = RequestCreator(drawableRes)

    fun cancelRequest(targetImage: ImageView) {
        Glide.clear(targetImage)
        picasso?.cancelRequest(targetImage)
        if (glideTargetsMap.containsKey(targetImage))
            Glide.clear(glideTargetsMap.remove(targetImage))
        downloadingFilesKeys[targetImage]?.let {
            FileManager.get().cancelDownload(it)
            downloadingFilesKeys.remove(targetImage)
        }
    }

    fun isDefaultLocalFileExists(type: MediaStorageType, media: Media?)
            = media?.let { getDefaultLocalFile(type, media).exists() } ?: false

    fun getDefaultLocalFilePath(type: MediaStorageType, fileName: String)
            = "${getMediaStorageDir(type)}/$fileName"

    fun getDefaultLocalFileName(media: Media) = "media_file_${media.mediaFileId}" +
            "${MediaSize._DEFAULT.name}${Extension.getFromFileName(media.name).suffix}"

    fun getDefaultLocalFile(type: MediaStorageType, media: Media)
            = File(getMediaStorageDir(type), getDefaultLocalFileName(media))
    //endregion

    //region class OptionsCreator
    class OptionsCreator(private val media: Media?) {
        fun withOptions(type: MediaStorageType, mediaSize: MediaSize)
                = RequestCreator(media, type, mediaSize)
    }
    //endregion

    private enum class InitDataType { MEDIA_FILE, DRAWABLE_RES, URI }

    //region class RequestCreator
    class RequestCreator private constructor(
            private val initDataType: InitDataType,
            private val media: Media? = null,
            private val type: MediaStorageType? = null,
            private val mediaSize: MediaSize? = null,
            private val drawableRes: Int? = null,
            private val uri: Uri? = null
    ) {

        //region fields
        private var placeholderResId: Int? = null
        private var placeholderDrawable: Drawable? = null

        private var errorResId: Int? = null
        private var errorDrawable: Drawable? = null

        private var transformations: MutableList<Transformation>? = null

        private var withProgressUpdates: Boolean = false
        //endregion

        //region Constructors
        constructor(media: Media?, type: MediaStorageType, mediaSize: MediaSize)
                : this(InitDataType.MEDIA_FILE, media, type, mediaSize)

        constructor(@DrawableRes drawableRes: Int)
                : this(InitDataType.DRAWABLE_RES, drawableRes = drawableRes)

        constructor(uri: Uri)
                : this(InitDataType.URI, uri = uri)
        //endregion

        //region Public Tools
        fun placeholder(@DrawableRes placeholderResId: Int): RequestCreator {
            if (this.placeholderResId != null || this.placeholderDrawable != null)
                throw RuntimeException("placeholder has already set")
            this.placeholderResId = placeholderResId
            return this
        }

        fun placeholder(placeholderDrawable: Drawable): RequestCreator {
            if (this.placeholderResId != null || this.placeholderDrawable != null)
                throw RuntimeException("placeholder has already set")
            this.placeholderDrawable = placeholderDrawable
            return this
        }

        fun error(@DrawableRes errorResId: Int): RequestCreator {
            if (this.errorResId != null || this.errorDrawable != null)
                throw RuntimeException("error has already set")
            this.errorResId = errorResId
            return this
        }

        fun error(errorDrawable: Drawable): RequestCreator {
            if (this.errorResId != null || this.errorDrawable != null)
                throw RuntimeException("error has already set")
            this.errorDrawable = errorDrawable
            return this
        }

        fun transform(vararg transformations: Transformation?): RequestCreator {
            if (this.transformations != null)
                throw RuntimeException("transformations has already set")
            if (transformations.isNotEmpty()) {
                Arrays.sort(transformations) { t1, t2 ->
                    if (t1 == null || t2 == null)
                        throw NullPointerException("transformations must not be null")
                    t1.hashCode() - t2.hashCode()
                }
                this.transformations = Arrays.asList(*transformations)
            } else throw NullPointerException("transformations must not be null")
            return this
        }

        fun withProgressUpdates(): RequestCreator {
            withProgressUpdates = true
            return this
        }

        fun into(target: ImageView)
                = into(target, null)

        fun into(target: ImageView, onResult: (isLoaded: Boolean) -> Unit)
                = into(target, object : com.squareup.picasso.Callback {
            override fun onSuccess() = onResult(true)
            override fun onError() = onResult(false)
        })

        fun into(targetImage: ImageView, callback: com.squareup.picasso.Callback?) {
            val handler = handler!!
            val picasso = picasso!!
            val glide = glide!!
            cancelRequest(targetImage)
            when (initDataType) {
                MediaHelper.InitDataType.MEDIA_FILE -> media?.getPath(mediaSize)?.let { path ->
                    val extension = Extension.getFromFileName(media.name)
                    val fileName = "media_file_${media.mediaFileId}${mediaSize?.name}" +
                            (transformations?.joinToString(prefix = "_", separator = "_") {
                                it.javaClass.simpleName
                            } ?: "") + extension.suffix
                    val file = File(getMediaStorageDir(type!!), fileName)
                    if (file.exists())
                        if (extension == Extension.GIF)
                            glideTargetsMap.put(targetImage, prepareRequest(glide.load(file)).asGif().into(targetImage))
                        else prepareRequest(picasso.load(file)).into(targetImage, callback)
                    else {
                        targetImage.apply {
                            placeholderResId?.let { setImageResource(it) }
                                    ?: placeholderDrawable?.let { setImageDrawable(it) }
                                    ?: setImageDrawable(null)
                        }
                        val key = "${media.mediaFileId}$mediaSize"
                        downloadingFilesKeys.put(targetImage, key)
                        FileManager.get().downloadFile(path, file, key, object : FileManager.Callback {

                            override fun onCompleted() {
                                handler.post {
                                    if (extension === Extension.GIF)
                                        prepareRequest(glide.load(file)).asGif().into(targetImage)
                                    else prepareRequest(picasso.load(file)).into(targetImage, callback)
                                }
                            }

                            override fun onFinished() {
                                downloadingFilesKeys.remove(targetImage)
                            }
                        })
                    }
                } ?: targetImage.setImageDrawable(null)
                MediaHelper.InitDataType.DRAWABLE_RES ->
                    prepareRequest(picasso.load(drawableRes!!)).into(targetImage, callback)
                MediaHelper.InitDataType.URI ->
                    prepareRequest(picasso.load(uri!!)).into(targetImage, callback)
            }
        }

        fun forResult(result: (File) -> Unit) {
//            cancelRequest(targetImage)
            media?.getPath(MediaSize._DEFAULT)?.let { path ->
                val file = getDefaultLocalFile(type!!, media)
                if (file.exists()) result(file)
                else {
                    val key = "${media.mediaFileId}$mediaSize"
//                    downloadingFilesKeys.put(targetImage, key)
                    FileManager.get().downloadFile(path, file, key, object : FileManager.Callback {
                        override fun onCompleted() {
                            handler?.post { result(file) }
                        }

                        override fun onFinished() {
//                            downloadingFilesKeys.remove(targetImage)
                        }
                    })
                }
            }
        }
        //endregion

        private fun prepareRequest(requestCreator: com.squareup.picasso.RequestCreator) = requestCreator.apply {
            placeholderResId?.let { placeholder(it) } ?: placeholderDrawable?.let { placeholder(it) }
            errorResId?.let { error(it) } ?: errorDrawable?.let { error(it) }
            transformations?.let { if (it.isNotEmpty()) transform(it) }
        }

        private fun prepareRequest(requestCreator: DrawableTypeRequest<*>) = requestCreator.apply {
            placeholderResId?.let { placeholder(it) } ?: placeholderDrawable?.let { placeholder(it) }
            errorResId?.let { error(it) } ?: errorDrawable?.let { error(it) }
        }
    }

    interface Callback {
        fun onResult(isLoaded: Boolean)
    }
}