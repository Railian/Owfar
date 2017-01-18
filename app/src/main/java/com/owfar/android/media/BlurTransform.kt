package com.owfar.android.media

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.renderscript.*
import com.squareup.picasso.Transformation

class BlurTransform(context: Context, private val radius: Int) : Transformation {

    private val rs: RenderScript

    init {
        rs = RenderScript.create(context)
    }

    //region Transformation Implementation
    override fun key() = "blur"

    override fun transform(bitmapOriginal: Bitmap): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            try {
                rsBlur(radius, bitmapOriginal)
            } catch (e: RSRuntimeException) {
                fastBlur(radius, bitmapOriginal)
            }
        else fastBlur(radius, bitmapOriginal)
    }
    //endregion

    //region Private Tools
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun rsBlur(radius: Int, bitmapOriginal: Bitmap): Bitmap {
        val bitmapBlurred = Bitmap.createBitmap(bitmapOriginal.width, bitmapOriginal.height, Bitmap.Config.ARGB_8888)

        // Bitmap bitmapBlurred = bitmapOriginal.copy(bitmapOriginal.getConfig(), true);
        val input = Allocation.createFromBitmap(rs, bitmapOriginal)
        val output = Allocation.createFromBitmap(rs, bitmapBlurred)

        // final Allocation output = Allocation.createTyped(rs, input.getType());
        val script: ScriptIntrinsicBlur
        script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(radius.toFloat())
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmapBlurred)
        bitmapOriginal.recycle()
        script.destroy()
        return bitmapBlurred
    }

    private fun fastBlur(radius: Int, bitmapOriginal: Bitmap): Bitmap {
        val bitmapBlurred = bitmapOriginal.copy(bitmapOriginal.config, true)
        bitmapOriginal.recycle()
        val w = bitmapBlurred.width
        val h = bitmapBlurred.height
        val pix = IntArray(w * h)

        bitmapBlurred.getPixels(pix, 0, w, 0, 0, w, h)

        var r = radius
        while (r >= 1) {
            for (i in r..h - r - 1) {
                for (j in r..w - r - 1) {
                    val tl = pix[(i - r) * w + j - r]
                    val tr = pix[(i - r) * w + j + r]
                    val tc = pix[(i - r) * w + j]
                    val bl = pix[(i + r) * w + j - r]
                    val br = pix[(i + r) * w + j + r]
                    val bc = pix[(i + r) * w + j]
                    val cl = pix[i * w + j - r]
                    val cr = pix[i * w + j + r]

                    pix[i * w + j] = 0xFF000000.toInt() or (
                            (tl and 0xFF) + (tr and 0xFF) + (tc and 0xFF) + (bl and 0xFF) + (br and 0xFF) + (bc and 0xFF) + (cl and 0xFF) + (cr and 0xFF) shr 3 and 0xFF) or (
                            (tl and 0xFF00) + (tr and 0xFF00) + (tc and 0xFF00) + (bl and 0xFF00) + (br and 0xFF00) + (bc and 0xFF00) + (cl and 0xFF00) + (cr and 0xFF00) shr 3 and 0xFF00) or (
                            (tl and 0xFF0000) + (tr and 0xFF0000) + (tc and 0xFF0000) + (bl and 0xFF0000) + (br and 0xFF0000) + (bc and 0xFF0000) + (cl and 0xFF0000) + (cr and 0xFF0000) shr 3 and 0xFF0000)
                }
            }
            r /= 2
        }
        bitmapBlurred.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmapBlurred
    }
    //endregion
}