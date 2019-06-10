package com.lihao.locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lihao.locker.utils.TimeUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_OP = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvBottomTip.text = TimeUtils.getNowDatetime() + "    ${TimeUtils.getWeekOfDate(Date())}"

        tvOpenLocker.setOnClickListener {
            val it = Intent(this@MainActivity, DetecterActivity::class.java)
            startActivityForResult(it, REQUEST_CODE_OP)
        }
    }
}
