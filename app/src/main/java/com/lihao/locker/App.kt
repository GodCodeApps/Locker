package com.lihao.locker

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log


class App : Application() {
    private val TAG = this.javaClass.toString()
    lateinit var mFaceDB: FaceDB

    override fun onCreate() {
        super.onCreate()
        mFaceDB = FaceDB(this.externalCacheDir!!.path)
        mImage = null
    }

    companion object {
        var mImage: Uri? = null
        fun setCaptureImage(uri: Uri) {
            mImage = uri
        }

        fun getCaptureImage(): Uri? {
            return mImage
        }

        /**
         * @param path
         * @return
         */
        fun decodeImage(path: String): Bitmap? {
            val res: Bitmap
            try {
                val exif = ExifInterface(path)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                val op = BitmapFactory.Options()
                op.inSampleSize = 1
                op.inJustDecodeBounds = false
                //op.inMutable = true;
                res = BitmapFactory.decodeFile(path, op)
                //rotate and scale.
                val matrix = Matrix()

                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    matrix.postRotate(90f)
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    matrix.postRotate(180f)
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    matrix.postRotate(270f)
                }

                val temp = Bitmap.createBitmap(res, 0, 0, res.width, res.height, matrix, true)
                Log.d("com.arcsoft", "check target Image:" + temp.width + "X" + temp.height)

                if (temp != res) {
                    res.recycle()
                }
                return temp
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }
}
