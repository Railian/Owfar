package com.owfar.android.helpers

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.ExifInterface.*
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import com.owfar.android.data.logFun
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.enums.MediaStorageType.*
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.utils.UriUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MediaIntentHelper : PermissionHelper.PermissionCallback {

    //region fields
    private var activity: Activity? = null
    private var fragment: Fragment? = null

    private var streamType: StreamType? = null
    private var streamId: Long? = null

    private var permissionHelper: PermissionHelper? = null

    private var filePath: String? = null

    private var recorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    //endregion

    //region Constructors
    constructor(activity: Activity, streamType: StreamType? = null, streamId: Long? = null) {
        this.activity = activity
        permissionHelper = PermissionHelper(activity)
        this.streamType = streamType
        this.streamId = streamId
    }

    constructor(fragment: Fragment, streamType: StreamType? = null, streamId: Long? = null) {
        this.fragment = fragment
        permissionHelper = PermissionHelper(fragment)
        this.streamType = streamType
        this.streamId = streamId
    }

    //endregion

    //region Life-Cycle
    fun saveState(outState: Bundle) = outState.putString(STATE_FILE_PATH, filePath)

    fun restoreState(savedInstanceState: Bundle) {
        filePath = savedInstanceState.getString(STATE_FILE_PATH)
    }
    //endregion

    //region Requests
    fun requestTakePhoto() {
        permissionHelper?.let {
            var requiredPermissions = arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                requiredPermissions += READ_EXTERNAL_STORAGE
            it.verifyPermission(REQUEST_TAKE_PHOTO, requiredPermissions, this)
        }
    }

    fun requestPickPhoto() {
        permissionHelper?.let {
            var requiredPermissions = emptyArray<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                requiredPermissions += READ_EXTERNAL_STORAGE
            it.verifyPermission(REQUEST_PICK_PHOTO, requiredPermissions, this)
        }
    }

    fun requestPickImageContent() {
        permissionHelper?.let {
            var requiredPermissions = emptyArray<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                requiredPermissions += READ_EXTERNAL_STORAGE
            it.verifyPermission(REQUEST_PICK_IMAGE_CONTENT, requiredPermissions, this)
        }
    }

    fun requestTakeVideo() {
        permissionHelper?.let {
            var requiredPermissions = arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                requiredPermissions += READ_EXTERNAL_STORAGE
            it.verifyPermission(REQUEST_TAKE_VIDEO, requiredPermissions, this)
        }
    }

    fun requestStartRecordingAudio() {
        permissionHelper?.let {
            var requiredPermissions = arrayOf(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                requiredPermissions += READ_EXTERNAL_STORAGE
            it.verifyPermission(REQUEST_RECORD_AUDIO, requiredPermissions, this)
        }
    }

    fun requestStopRecordingAudio() {
        if (isRecording) stopRecording()?.let { onRecordAudioListener?.onAudioRecorded(it) }
    }
    //endregion

    //region PermissionCallback Implementation
    override fun grantedAllPermissions(requestCode: Int, permissions: Array<String>) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> Message.generateSID(streamType?.jsonName, streamId).let { sid ->
                filePath = MediaHelper.getDefaultLocalFilePath(IMAGES, "$sid.jpeg")
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(filePath)))
                }.let {
                    activity?.startActivityForResult(it, REQUEST_TAKE_PHOTO)
                            ?: fragment?.startActivityForResult(it, REQUEST_TAKE_PHOTO)
                            ?: throw NullPointerException("activity or fragment is required")
                }
            }
            REQUEST_PICK_PHOTO ->
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).let {
                    activity?.startActivityForResult(it, REQUEST_PICK_PHOTO)
                            ?: fragment?.startActivityForResult(it, REQUEST_PICK_PHOTO)
                            ?: throw NullPointerException("activity or fragment is required")
                }
            REQUEST_PICK_IMAGE_CONTENT ->
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/jpeg"
                }.let {
                    activity?.startActivityForResult(it, REQUEST_PICK_IMAGE_CONTENT)
                            ?: fragment?.startActivityForResult(it, REQUEST_PICK_IMAGE_CONTENT)
                            ?: throw NullPointerException("activity or fragment is required")
                }
            REQUEST_TAKE_VIDEO -> Message.generateSID(streamType?.jsonName, streamId).let { sid ->
                filePath = MediaHelper.getDefaultLocalFilePath(VIDEOS, "$sid.mov")
                Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(filePath)))
                    putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                }.let {
                    activity?.startActivityForResult(it, REQUEST_TAKE_VIDEO)
                            ?: fragment?.startActivityForResult(it, REQUEST_TAKE_VIDEO)
                            ?: throw NullPointerException("activity or fragment is required")
                }
            }
            REQUEST_RECORD_AUDIO -> Message.generateSID(streamType?.jsonName, streamId).let { sid ->
                filePath = MediaHelper.getDefaultLocalFilePath(AUDIOS, "$sid.m4a")
                startRecording(filePath)
            }
        }
    }

    override fun deniedPermissions(requestCode: Int, permissions: Array<String>) = Unit
    //endregion

    //region Public Tools
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
            = permissionHelper?.onRequestPermissionsResult(requestCode, permissions, grantResults)

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            REQUEST_TAKE_PHOTO -> filePath?.let {
                rotatePhotoIfRequired(it)
                simplifyImageFile(it)
                onImageTakenListener?.onImageTaken(it)
            }
            REQUEST_PICK_PHOTO, REQUEST_PICK_IMAGE_CONTENT -> data?.let {
                UriUtils.getPath(context, it.data)?.let {
                    Message.generateSID(streamType?.jsonName, streamId).let { sid ->
                        MediaHelper.getDefaultLocalFilePath(IMAGES, "$sid.jpeg").let { filePath ->
                            simplifyImageFile(it, filePath)
                            onImageTakenListener?.onImageTaken(filePath)
                        }
                    }
                }
            }
            REQUEST_TAKE_VIDEO -> filePath?.let {
                onVideoTakenListener?.onVideoTaken(it)
            }
        }
    }
    //endregion

    //region Private Tools
    private fun startRecording(filePath: String?) {
        logFun(TAG, MediaIntentHelper::startRecording, filePath)
        stopRecording()
        filePath?.let {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(it)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                    start()
                    isRecording = true
                    onRecordAudioListener?.onStartAudioRecording()
                } catch (e: IOException) {
                    Log.e(TAG, "prepare() failed")
                }
            }
        }
    }

    private fun stopRecording() = let {
        if (isRecording) {
            onRecordAudioListener?.onStopAudioRecording()
            isRecording = false
            recorder?.apply {
                stop()
                release()
                recorder = null
            }
            filePath
        } else null
    }

    private val context: Context?
        get() = activity ?: fragment?.activity
    //endregion

    //region Interface OnImageTakenListener
    var onImageTakenListener: OnImageTakenListener? = null

    interface OnImageTakenListener {
        fun onImageTaken(imagePath: String)
    }
    //endregion

    //region Interface OnVideoTakenListener
    var onVideoTakenListener: OnVideoTakenListener? = null

    interface OnVideoTakenListener {
        fun onVideoTaken(videoPath: String)
    }
    //endregion

    //region Interface OnAudioTakenListener
    var onRecordAudioListener: OnRecordAudioListener? = null

    interface OnRecordAudioListener {
        fun onStartAudioRecording()
        fun onStopAudioRecording()
        fun onAudioRecorded(audioPath: String)
    }
    //endregion

    companion object {

        @JvmStatic private val TAG = MediaIntentHelper::class.java.simpleName

        private val STATE_FILE_PATH = TAG + ".STATE_FILE_PATH"

        private val REQUEST_TAKE_PHOTO = 1
        private val REQUEST_PICK_PHOTO = 2
        private val REQUEST_PICK_IMAGE_CONTENT = 3
        private val REQUEST_TAKE_VIDEO = 4
        private val REQUEST_RECORD_AUDIO = 5

        private fun simplifyImageFile(pathOriginalImage: String, pathDestinationImage: String = pathOriginalImage) {
            getResizedBitmap(1280, 1280, pathOriginalImage)?.apply {
                try {
                    FileOutputStream(File(pathDestinationImage)).apply {
                        compress(Bitmap.CompressFormat.JPEG, 80, this)
                        flush()
                        close()
                        recycle()
                    }
                } catch (ignored: Exception) {
                }
            }
        }

        private fun getResizedBitmap(targetW: Int, targetH: Int, imagePath: String): Bitmap?
                = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, this)
            inJustDecodeBounds = false
            inSampleSize = Math.min(outWidth / targetW, outHeight / targetH)
        }.let { BitmapFactory.decodeFile(imagePath, it) }

        private fun rotatePhotoIfRequired(photoPath: String) {
            try {
                rotateImageIfRequired(photoPath)?.apply {
                    FileOutputStream(File(photoPath)).apply {
                        compress(Bitmap.CompressFormat.JPEG, 100, this)
                        flush()
                        close()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        @Throws(IOException::class)
        private fun rotateImageIfRequired(imagePath: String) = BitmapFactory.decodeFile(imagePath).let {
            when (ExifInterface(imagePath).getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL)) {
                ORIENTATION_ROTATE_90 -> rotateImage(it, 90)
                ORIENTATION_ROTATE_180 -> rotateImage(it, 180)
                ORIENTATION_ROTATE_270 -> rotateImage(it, 270)
                else -> it
            }
        }

        private fun rotateImage(img: Bitmap?, degree: Int) = Matrix().let {
            it.postRotate(degree.toFloat())
            img?.let { img ->
                Bitmap.createBitmap(img, 0, 0, img.width, img.height, it, true).apply {
                    img.recycle()
                }
            }
        }
    }
}