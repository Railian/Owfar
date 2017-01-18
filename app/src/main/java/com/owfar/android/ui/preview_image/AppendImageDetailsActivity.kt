package com.owfar.android.ui.preview_image

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.extensions.orNullIfBlank
import com.owfar.android.media.MediaHelper
import com.owfar.android.ui.dialogs.AudioDialog
import com.squareup.picasso.Callback
import uk.co.senab.photoview.PhotoViewAttacher
import java.io.File

class AppendImageDetailsActivity : AppCompatActivity(), View.OnClickListener {

    companion object {

        val TAG: String? = AppendImageDetailsActivity::class.java.simpleName

        val EXTRA_IMAGE_FILE_PATH = "$TAG.EXTRA_AUDIO_FILE_PATH"
        val EXTRA_HEADER = "$TAG.EXTRA_HEADER"
        val EXTRA_CONTENT = "$TAG.EXTRA_CONTENT"

        fun startForResult(targetFragment: Fragment, requestCode: Int, imageFilePath: String) {
            Intent(targetFragment.activity, AppendImageDetailsActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_FILE_PATH, imageFilePath)
            }.let { targetFragment.startActivityForResult(it, requestCode) }
        }
    }

    //region widgets
    private var ivImage: ImageView? = null
    private var etHeader: EditText? = null
    private var etContent: EditText? = null
    private var tvCancel: TextView? = null
    private var tvSend: TextView? = null
    //endregion

    //region fields
    private var imageFilePath: String? = null
    private var attacher: PhotoViewAttacher? = null
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_append_image_details)

        ivImage = findViewById(R.id.activity_append_image_details_ivImage) as? ImageView
        etHeader = findViewById(R.id.activity_append_image_details_etHeader) as? EditText
        etContent = findViewById(R.id.activity_append_image_details_etContent) as? EditText
        tvCancel = findViewById(R.id.activity_append_image_details_tvCancel) as? TextView
        tvSend = findViewById(R.id.activity_append_image_details_tvSend) as? TextView

        imageFilePath = intent.getStringExtra(EXTRA_IMAGE_FILE_PATH)

        ivImage?.apply {
            attacher = PhotoViewAttacher(this)
            MediaHelper
                    .load(Uri.fromFile(File(imageFilePath)))
                    .into(this, mediaCallback)
        }

        tvCancel?.setOnClickListener(this)
        tvSend?.setOnClickListener(this)
    }

    //region OnClickListener Implementation
    override fun onClick(v: View) {
        when (v.id) {
            R.id.activity_append_image_details_tvSend -> RESULT_OK
            R.id.activity_append_image_details_tvCancel -> RESULT_CANCELED
            else -> null
        }?.let { resultCode ->
            val data = Intent().apply {
                putExtra(EXTRA_IMAGE_FILE_PATH, imageFilePath)
                if (resultCode == RESULT_OK) {
                    putExtra(EXTRA_HEADER, etHeader?.text?.toString()?.orNullIfBlank())
                    putExtra(EXTRA_CONTENT, etContent?.text?.toString()?.orNullIfBlank())
                }
            }
            setResult(resultCode, data)
            finish()
        }
    }
    //endregion

    private var mediaCallback = object : Callback {
        override fun onSuccess() {
            attacher?.update()
        }

        override fun onError() {
            attacher?.update()
        }
    }
}
