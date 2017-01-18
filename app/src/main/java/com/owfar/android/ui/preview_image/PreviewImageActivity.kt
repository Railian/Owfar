package com.owfar.android.ui.preview_image

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.owfar.android.R
import com.owfar.android.api.file.FileManager
import com.owfar.android.api.users.ProgressListener
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.Media
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType
import com.squareup.picasso.Callback
import uk.co.senab.photoview.PhotoViewAttacher

class PreviewImageActivity : AppCompatActivity() {

    companion object {
        private val TAG = PreviewImageActivity::class.java.simpleName
        val EXTRA_MEDIA_FILE = TAG + ".EXTRA_MEDIA_FILE"

        fun start(context: Context, media: Media?) {
            val intent = Intent(context, PreviewImageActivity::class.java)
            media?.let { intent.putExtra(EXTRA_MEDIA_FILE, it) }
            context.startActivity(intent)
        }
    }

    //region widgets
    private var ivImage: ImageView? = null
    private var vProgress: View? = null
    private var tvProgress: TextView? = null
    //endregion

    //region fields
    private var attacher: PhotoViewAttacher? = null
    //endregion

    //region extras
    private var media: Media? = null
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)

        ivImage = findViewById(R.id.activity_preview_image_ivImage) as? ImageView
        vProgress = findViewById(R.id.activity_preview_image_vProgress)
        tvProgress = findViewById(R.id.activity_preview_image_tvProgress) as? TextView

        media = intent.getParcelableExtra<Media>(EXTRA_MEDIA_FILE)

        ivImage?.apply {
            attacher = PhotoViewAttacher(this)
            val mediaSize = MediaSize._DEFAULT
            media?.let {
                FileManager.get().downloadDelegatesSet.addDelegate("${it.mediaFileId}$mediaSize", progressListener)
                MediaHelper
                        .load(it)
                        .withOptions(MediaStorageType.IMAGES, mediaSize)
                        .into(this, mediaCallback)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initFullscreen()
    }

    override fun onDestroy() {
        FileManager.get().downloadDelegatesSet.removeDelegate(progressListener)
        super.onDestroy()
    }

    private fun initFullscreen() {
        window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT < 19) View.GONE
        else View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private var mediaCallback = object : Callback {
        override fun onSuccess() {
            attacher?.update()
        }

        override fun onError() {
            attacher?.update()
        }
    }

    private val progressListener = object : ProgressListener.Simple() {
        override fun onStarted() {
            runOnUiThread { vProgress?.visibility = View.VISIBLE }
        }

        override fun onUpdated(bytesRead: Long, contentLength: Long) {
            runOnUiThread { tvProgress?.text = "Image loading...${bytesRead * 100 / contentLength}%" }
        }

        override fun onFinished() {
            runOnUiThread { vProgress?.visibility = View.GONE }
        }
    }
}
