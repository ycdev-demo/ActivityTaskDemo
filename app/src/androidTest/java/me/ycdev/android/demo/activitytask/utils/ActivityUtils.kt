package me.ycdev.android.demo.activitytask.utils

import android.app.ActivityManager
import android.content.Context

object ActivityUtils {
    fun finishAndRemoveAllTasks(context: Context) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.appTasks.forEach {
            it.finishAndRemoveTask()
        }
    }
}
