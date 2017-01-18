package com.owfar.android.ui.main

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.owfar.android.R
import com.owfar.android.data.logFun
import com.owfar.android.helpers.KeyboardHelper
import com.owfar.android.helpers.MediaIntentHelper
import com.owfar.android.models.api.StickerList
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.ui.boards.BoardsHelper
import com.owfar.android.ui.boards.asserts.AssertsBoard
import com.owfar.android.ui.boards.stickers.OnStickerSelectedListener
import com.owfar.android.ui.boards.stickers.StickersBoard
import com.owfar.android.ui.dialogs.AudioDialog
import com.owfar.android.ui.preview_image.AppendImageDetailsActivity
import java.io.File

class MessengerHelper(
        view: View,
        private val targetFragment: Fragment,
        private val boardsHelper: BoardsHelper?,
        streamType: StreamType?,
        streamId: Long?
) : View.OnClickListener, TextWatcher, OnStickerSelectedListener,
        MediaIntentHelper.OnImageTakenListener,
        MediaIntentHelper.OnRecordAudioListener,
        MediaIntentHelper.OnVideoTakenListener {

    //region widgets
    private val vMessenger: View
    private val ivAddAsserts: ImageView
    private val etMessage: EditText
    private val ivAddStickers: ImageView
    private val ivSend: ImageView
    private val vAssertsBoard: AssertsBoard
    private val vStickersBoard: StickersBoard
    //endregion

    //region fields
    private val context: Context
    private val mediaIntentHelper: MediaIntentHelper
    //endregion

    //region Initialization
    init {
        context = view.context

        vMessenger = view.findViewById(R.id.merge_messenger_vMessenger)
        ivAddAsserts = view.findViewById(R.id.merge_messenger_ivAddAsserts) as ImageView
        etMessage = view.findViewById(R.id.merge_messenger_etMessage) as EditText
        ivAddStickers = view.findViewById(R.id.merge_messenger_ivAddSticker) as ImageView
        ivSend = view.findViewById(R.id.merge_messenger_ivSend) as ImageView
        vAssertsBoard = view.findViewById(R.id.fragment_messenger_vAssertsBoard) as AssertsBoard
        vStickersBoard = view.findViewById(R.id.fragment_messenger_vStickersBoard) as StickersBoard

        vMessenger.visibility = View.GONE
        ivAddAsserts.setOnClickListener(this)
        etMessage.addTextChangedListener(this)
        ivAddStickers.setOnClickListener(this)
        ivSend.setOnClickListener(this)
        ivSend.visibility = View.GONE

        mediaIntentHelper = MediaIntentHelper(targetFragment, streamType, streamId)
        vAssertsBoard.mediaIntentHelper = mediaIntentHelper
        mediaIntentHelper.onImageTakenListener = this
        mediaIntentHelper.onRecordAudioListener = this
        mediaIntentHelper.onVideoTakenListener = this
        vStickersBoard.setOnStickerSelectedListener(this)

        boardsHelper?.apply {
            addBoardView(vAssertsBoard)
            addBoardView(vStickersBoard)
        }
    }
    //endregion

    companion object {
        @JvmStatic private val TAG = MessengerHelper::class.java.simpleName
        private val REQUEST_APPEND_IMAGE_DETAILS = 11
        private val REQUEST_APPEND_AUDIO_DETAILS = 12
        private val REQUEST_APPEND_VIDEO_DETAILS = 13
    }

    //region Public Tools
    var text: String?
        get() = etMessage.text.toString()
        set(value) = etMessage.let { it.text = value?.let(::SpannableStringBuilder) }

    fun showMessenger() = apply { vMessenger.visibility = View.VISIBLE }

    fun hideMessenger() = apply {
        etMessage.text = null
        vMessenger.visibility = View.GONE
    }

    fun setMessengerVisible(visible: Boolean) = if (visible) showMessenger() else hideMessenger()

    fun hideBoards() = boardsHelper?.hideBoards() ?: false

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mediaIntentHelper.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_APPEND_IMAGE_DETAILS -> data?.apply {
                getStringExtra(AppendImageDetailsActivity.EXTRA_IMAGE_FILE_PATH)?.let { imagePath ->
                    if (resultCode == Activity.RESULT_OK) {
                        val header = getStringExtra(AppendImageDetailsActivity.EXTRA_HEADER)
                        val content = getStringExtra(AppendImageDetailsActivity.EXTRA_CONTENT)
                        listener?.onSendPhoto(imagePath, header, content)
                    } else File(imagePath).delete()
                }
            }
            REQUEST_APPEND_AUDIO_DETAILS -> data?.apply {
                val audioPath = getStringExtra(AudioDialog.EXTRA_AUDIO_FILE_PATH)
                if (resultCode == Activity.RESULT_OK) {
                    val header = getStringExtra(AudioDialog.EXTRA_HEADER)
                    val content = getStringExtra(AudioDialog.EXTRA_CONTENT)
                    listener?.onSendAudio(audioPath, header, content)
                } else File(audioPath).delete()
            }
            REQUEST_APPEND_VIDEO_DETAILS -> data?.apply {
//                val videoPath = getStringExtra(AppendVideoDetailsActivity.EXTRA_AUDIO_FILE_PATH)
//                if (requestCode == Activity.RESULT_OK) {
//                    val header = getStringExtra(AppendVideoDetailsActivity.EXTRA_HEADER)
//                    val content = getStringExtra(AppendVideoDetailsActivity.EXTRA_CONTENT)
//                    listener?.onSendVideo(videoPath, header, content)
//                } else File(videoPath).delete()
            }
        }
    }
    //endregion

    //region OnClickListener Implementation
    override fun onClick(v: View) {
        when (v.id) {
            R.id.merge_messenger_ivAddAsserts -> {
                if (KeyboardHelper.keyboardVisible)
                    KeyboardHelper.hideKeyboard()
                boardsHelper?.toggleBoard(vAssertsBoard)
            }
            R.id.merge_messenger_ivAddSticker -> {
                if (KeyboardHelper.keyboardVisible)
                    KeyboardHelper.hideKeyboard()
                boardsHelper?.toggleBoard(vStickersBoard)
            }
            R.id.merge_messenger_ivSend -> {
                val text = etMessage.text.toString()
                listener?.onSendComment(text)
                etMessage.text = null
            }
        }
    }
    //endregion

    //region TextWatcher Implementation
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable) {
        ivSend.visibility = if (TextUtils.isEmpty(s)) View.GONE else View.VISIBLE
    }
    //endregion

    //region OnStickerSelectedListener Implementation
    override fun onStickerSelected(sticker: Sticker)
            = listener?.onSendSticker(sticker) ?: Unit
    //endregion

    //region OnImageTakenListener Implementation
    override fun onImageTaken(imagePath: String) {
        logFun(TAG, MessengerHelper::onImageTaken, imagePath)
        AppendImageDetailsActivity.startForResult(targetFragment, REQUEST_APPEND_IMAGE_DETAILS, imagePath)
    }
    //endregion

    //region OnRecordAudioListener Implementation
    override fun onStartAudioRecording() {
        recordDialog.apply { if (!isShowing) show() }
    }

    override fun onStopAudioRecording() {
        recordDialog.apply { if (isShowing) dismiss() }
    }

    override fun onAudioRecorded(audioPath: String) {
        AudioDialog.newInstance(targetFragment, REQUEST_APPEND_AUDIO_DETAILS, audioPath).apply {
            targetFragment.fragmentManager?.let { show(it, AudioDialog.TAG) }
        }
    }
    //endregion

    //region OnVideoTakenListener Implementation
    override fun onVideoTaken(videoPath: String)
            = listener?.onSendVideo(videoPath) ?: Unit
    //endregion

    //region MessengerHelperListener
    private var listener: MessengerHelperListener? = null

    fun setMessengerHelperListener(listener: MessengerHelperListener) {
        this.listener = listener
    }

    fun setStickerList(stickerList: StickerList) {
        vStickersBoard.setStickerList(stickerList)
    }

    interface MessengerHelperListener {

        fun onSendComment(text: String)

        fun onSendSticker(sticker: Sticker)

        fun onSendPhoto(photoPath: String, header: String? = null, content: String? = null)

        fun onSendAudio(audioPath: String, header: String? = null, content: String? = null)

        fun onSendVideo(videoPath: String, header: String? = null, content: String? = null)
    }
    //endregion

    private val recordDialog = ProgressDialog(context).apply {
        isIndeterminate = true
        setMessage("Recording audio")
    }
}