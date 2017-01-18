package com.owfar.android.helpers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.MediaRecorder
import android.support.v4.app.Fragment
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.enums.MediaStorageType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.ui.dialogs.AudioDialog
import java.io.IOException

class Record2AudioHelper {

    companion object {
//        val TAG = RecordAudioHelper::class.java.simpleName
        val REQUEST_RECORD_AUDIO = 5
    }

    //region fields
    private var activity: Activity? = null
    private var fragment: Fragment? = null

    private var permissionHelper: PermissionHelper? = null

    private var fileName: String? = null
    private var recorder: MediaRecorder? = null
    //endregion

    //region Constructors
    constructor(activity: Activity) {
        this.activity = activity
        permissionHelper = PermissionHelper(activity)
    }

    constructor(fragment: Fragment) {
        this.fragment = fragment
        permissionHelper = PermissionHelper(fragment)
    }
    //endregion

    //region Public Tools
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> onAudioRecordedListener?.apply {
                data?.let {
                    val audioFilePath = it.getStringExtra(AudioDialog.EXTRA_AUDIO_FILE_PATH)
                    val header = it.getStringExtra(AudioDialog.EXTRA_HEADER)
                    val content = it.getStringExtra(AudioDialog.EXTRA_CONTENT)
                    onAudioRecorded(audioFilePath, header, content)
                }
            }
        }
    }
    //endregion

    private var onAudioRecordedListener: OnAudioRecordedListener? = null

    fun setOnAudioRecordedListener(listener: OnAudioRecordedListener) {
        onAudioRecordedListener = listener
    }

    interface OnAudioRecordedListener {
        fun onAudioRecorded(audioPath: String, header: String?, content: String?)
    }

    fun startRecording(streamType: String?, streamId: Long?) {
        permissionHelper?.verifyPermission(REQUEST_RECORD_AUDIO, arrayOf(Manifest.permission.RECORD_AUDIO), object : PermissionHelper.PermissionCallback {
            override fun grantedAllPermissions(requestCode: Int, permissions: Array<out String>?) {
                stopRecording()
                val sid = Message.generateSID(streamType, streamId)
                fileName = MediaHelper.getDefaultLocalFilePath(MediaStorageType.AUDIOS, "$sid.m4a")
//                fileName = "${Environment.getExternalStorageDirectory().absolutePath}/audio_record_${System.currentTimeMillis()}.m4a"
                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setOutputFile(fileName)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    try {
                        prepare()
                        start()
                    } catch (e: IOException) {
//                        Log.e(TAG, "prepare() failed")
                    }
                }
            }

            override fun deniedPermissions(requestCode: Int, permissions: Array<out String>?) {
            }
        })
    }

    fun stopRecording(): String? {
        recorder?.apply {
            stop()
            release()
            recorder = null
        }
        return fileName
    }
}
