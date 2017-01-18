package com.owfar.android.api

import com.owfar.android.models.api.classes.Media
import java.io.File

interface MediaFileDownloadCallback {
    fun onDownloadStarted(media: Media)
    fun onDownloadProgressChanged(media: Media, fileSize: Long, fileSizeDownloaded: Long)
    fun onDownloadFailed(media: Media, throwable: Throwable)
    fun onDownloadCanceled(media: Media)
    fun onFileDownloaded(media: Media, file: File)
}