package com.lihao.locker.utils

import java.text.SimpleDateFormat
import java.util.*

class TimeUtils {
    companion object {
        var YYYY_MM_DD_HH_MM_SS = "yyyy年MM月dd日  HH:mm:ss"

        fun getNowDatetime(): String {
            val formatter = SimpleDateFormat(YYYY_MM_DD_HH_MM_SS, Locale.getDefault())
            return formatter.format(Date())
        }

        /**
         * 根据日期获得星期
         *
         * @param date
         * @return
         */
        fun getWeekOfDate(date: Date): String {
            val weekDaysName = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
            val calendar = Calendar.getInstance()
            calendar.time = date
            val intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            return weekDaysName[intWeek]
        }
    }
}