package com.lihao.locker

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lihao.locker.App.Companion.getCaptureImage
import com.lihao.locker.utils.TimeUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_OP = 1
    var PERMISSION_REQ = 0x123456
    private val REQUEST_CODE_IMAGE_CAMERA = 1

    private val mPermission =
        arrayOf(Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val mRequestPermission = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (one in mPermission) {
                if (PackageManager.PERMISSION_GRANTED != this.checkPermission(one, Process.myPid(), Process.myUid())) {
                    mRequestPermission.add(one)
                }
            }
            if (!mRequestPermission.isEmpty()) {
                this.requestPermissions(mRequestPermission.toTypedArray(), PERMISSION_REQ)
                return
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    tvBottomTip.text = TimeUtils.getNowDatetime() + "    ${TimeUtils.getWeekOfDate(Date())}"
                }
            }
        }




        tvSave.setOnClickListener {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            App.setCaptureImage(uri!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA)
        }
        tvOpen.setOnClickListener {
            val it = Intent(this@MainActivity, DetecterActivity::class.java)
            startActivityForResult(it, REQUEST_CODE_OP)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // 版本兼容
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return
        }
        if (requestCode == PERMISSION_REQ) {
            for (i in grantResults.indices) {
                for (one in mPermission) {
                    if (permissions[i] == one && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mRequestPermission.remove(one)
                    }
                }
            }
            startActiviy()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            val mPath = App.getCaptureImage()
            val file = getPath(mPath!!)
            val bmp = App.decodeImage(file!!)
            val it = Intent(this@MainActivity, RegisterActivity::class.java)
            val bundle = Bundle()
            bundle.putString("imagePath", file)
            it.putExtras(bundle)
            startActivityForResult(it, REQUEST_CODE_OP)
        }
    }


    /**
     * @param uri
     * @return
     */
    private fun getPath(uri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {

                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                    )

                    return getDataColumn(this, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(this, contentUri, selection, selectionArgs)
                }
            }
        }
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val actualimagecursor = this.contentResolver.query(uri, proj, null, null, null)
        val actual_image_column_index = actualimagecursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        actualimagecursor.moveToFirst()
        val img_path = actualimagecursor.getString(actual_image_column_index)
        val end = img_path.substring(img_path.length - 4)
        return if (0 != end.compareTo(".jpg", ignoreCase = true) && 0 != end.compareTo(".png", ignoreCase = true)) {
            null
        } else img_path
    }


    fun startActiviy() {
        if (mRequestPermission.isEmpty()) {
            val mProgressDialog = ProgressDialog(this)
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            mProgressDialog.setTitle("loading register data...")
            mProgressDialog.setCancelable(false)
            mProgressDialog.show()
            Thread(Runnable {
                val app = this@MainActivity.getApplicationContext() as App
                app.mFaceDB.loadFaces()
                this@MainActivity.runOnUiThread(Runnable {
                    mProgressDialog.cancel()
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    startActivityForResult(intent, PERMISSION_REQ)
                })
            }).start()
        } else {
            Toast.makeText(this, "PERMISSION DENIED!", Toast.LENGTH_LONG).show()
            Handler().postDelayed({ this@MainActivity.finish() }, 3000)
        }
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param mBitmap
     */
    private fun startRegister(mBitmap: Bitmap, file: String) {
        val it = Intent(this@MainActivity, RegisterActivity::class.java)
        val bundle = Bundle()
        bundle.putString("imagePath", file)
        it.putExtras(bundle)
        startActivityForResult(it, REQUEST_CODE_OP)
    }
}
