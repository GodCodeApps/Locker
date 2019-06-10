package com.lihao.locker

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lihao.locker.utils.TimeUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_OP = 1
    var PERMISSION_REQ = 0x123456

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
        tvBottomTip.text = TimeUtils.getNowDatetime() + "    ${TimeUtils.getWeekOfDate(Date())}"

        tvOpenLocker.setOnClickListener {
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
        if (requestCode == PERMISSION_REQ) {
        }
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
}
