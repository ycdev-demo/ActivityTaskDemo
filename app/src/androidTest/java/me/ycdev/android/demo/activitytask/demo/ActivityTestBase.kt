package me.ycdev.android.demo.activitytask.demo

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class ActivityTestBase {
    @Before
    fun setup() {
        ActivityTaskTracker.enableDebugLog(true)
        ActivityTestHelper.finishAndRemoveAllTasks(getContext())
        Truth.assertThat(ActivityTaskTracker.getAllTasks()).hasSize(0)
    }

    @After
    fun tearDown() {
        ActivityTestHelper.finishAndRemoveAllTasks(getContext())
    }

    protected fun getContext(): Context = ApplicationProvider.getApplicationContext()
}
