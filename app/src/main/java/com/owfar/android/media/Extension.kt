package com.owfar.android.media

import okhttp3.MediaType
import java.io.File

enum class Extension(mediaType: String?, vararg _suffixes: String) {

    JPEG("image/jpeg", ".jpeg", ".jpg"),
    PNG("image/png", ".png"),
    GIF("image/gif", ".gif"),
    MOV("video/quicktime", ".mov"),
    M4A("audio/x-m4a", ".m4a"),
    UNKNOWN(null, "");

    private val suffixes: Array<out String> = _suffixes
    val mediaType = mediaType?.let { MediaType.parse(it) }
    val suffix: String get() = suffixes.first()

    fun checkSuffix(suffix: String?) =
            suffixes.filter { suffix?.equals(it, ignoreCase = true) ?: false }.isNotEmpty()

    companion object {

        fun identify(suffix: String?) = values().find { it.checkSuffix(suffix) } ?: UNKNOWN

        fun getSuffixFromFileName(fileName: String?) =
                fileName?.lastIndexOf('.')?.let { if (it >= 0) fileName.substring(it) else null }

        fun getFromFileName(fileName: String?) = identify(getSuffixFromFileName(fileName))

        fun getFromFile(file: File?) = file?.let { getFromFileName(file.name) } ?: UNKNOWN
    }
}