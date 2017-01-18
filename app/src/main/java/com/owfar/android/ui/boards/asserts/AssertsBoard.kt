package com.owfar.android.ui.boards.asserts

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.owfar.android.R
import com.owfar.android.helpers.MediaIntentHelper

class AssertsBoard : LinearLayout, View.OnClickListener, View.OnTouchListener {

    //region widgets
    private var vTakePhoto: View? = null
    private var vPhotoFromGallery: View? = null
    private var vRecordVoice: View? = null
    private var vVideo: View? = null
    //endregion

    //region fields
    var mediaIntentHelper: MediaIntentHelper? = null
    //endregion

    //region Constructors
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    //endregion

    //region Initialization
    init {
        LayoutInflater.from(context).inflate(R.layout.board_asserts, this, true)

        vTakePhoto = findViewById(R.id.board_asserts_vTakePhoto)
        vPhotoFromGallery = findViewById(R.id.board_asserts_vPhotoFromGallery)
        vRecordVoice = findViewById(R.id.board_asserts_vRecordVoice)
        vVideo = findViewById(R.id.board_asserts_vVideo)

        vTakePhoto?.setOnClickListener(this)
        vPhotoFromGallery?.setOnClickListener(this)
        vRecordVoice?.setOnTouchListener(this)
        vVideo?.setOnClickListener(this)
    }
    //endregion

    //region UI Listeners Implementation
    override fun onClick(v: View) {
        when (v.id) {
            R.id.board_asserts_vTakePhoto -> mediaIntentHelper?.requestTakePhoto()
            R.id.board_asserts_vPhotoFromGallery -> mediaIntentHelper?.requestPickPhoto()
            R.id.board_asserts_vVideo -> mediaIntentHelper?.requestTakeVideo()
        }
    }

    override fun onTouch(v: View, event: MotionEvent) = when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            mediaIntentHelper?.requestStartRecordingAudio()
            true
        }
        MotionEvent.ACTION_UP,
        MotionEvent.ACTION_CANCEL -> {
            mediaIntentHelper?.requestStopRecordingAudio()
            true
        }
        else -> false
    }
    //endregion
}