package com.csci4480.regiftcard.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.*

class ImageManager {
    companion object {
        private const val LOG_TAG = "448.ImageManager"

        fun getBitmap(img_url: String?): Bitmap? {
            val image_file = File(img_url)
            var fis: FileInputStream? = null
            var bitmap: Bitmap? = null
            try {
                fis = FileInputStream(image_file)
                bitmap = BitmapFactory.decodeStream(fis)
            } catch (e: FileNotFoundException) {
                Log.e(LOG_TAG, "getBitmap: FileNotFoundException: " + e.message)
            } finally {
                try {
                    fis!!.close()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "getBitmap: IOException: " + e.message)
                }
            }
            return bitmap
        }

        fun getBytesFromBitmap(bm: Bitmap, quality: Int): ByteArray {
            val stream = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            return stream.toByteArray()
        }
    }


}