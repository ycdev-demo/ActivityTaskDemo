package me.ycdev.android.demo.activitytask

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    fun getTimeStamp(): String {
        return SimpleDateFormat("HH:mm:ss:SSS", Locale.US).format(Date(System.currentTimeMillis()))
    }
}
