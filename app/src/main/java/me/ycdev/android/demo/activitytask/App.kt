package me.ycdev.android.demo.activitytask

import android.app.Application
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.tag(TAG).i("app start...")

        ActivityTaskTracker.init(this)
    }

    companion object {
        private const val TAG = "ActivityTaskDemo"
    }
}
