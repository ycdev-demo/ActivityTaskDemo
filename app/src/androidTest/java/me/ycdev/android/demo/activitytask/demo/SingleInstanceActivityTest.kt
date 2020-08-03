package me.ycdev.android.demo.activitytask.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.activitytask.MainActivity
import me.ycdev.android.demo.activitytask.R
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance3Activity
import me.ycdev.android.demo.activitytask.utils.ActivityUtils
import me.ycdev.android.lib.common.activity.ActivityInfo
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import me.ycdev.android.lib.test.ui.ScrollViewsAction
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SingleInstanceActivityTest {
    @Before
    fun setup() {
        ActivityTaskTracker.enableDebugLog(true)
        ActivityUtils.finishAndRemoveAllTasks(getContext())
        assertThat(ActivityTaskTracker.getAllTasks()).hasSize(0)
    }

    private fun getContext(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun launchedByOnce() {
        val buttons = arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
        buttons.forEachIndexed { index, id ->
            val instanceId = index + 1
            launchActivity<MainActivity>()
            onView(withId(id)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1 + instanceId)
        }
    }

    @Test
    fun launchedByMultipleTimes() {
        val buttons = arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
        buttons.forEachIndexed { index, id ->
            val instanceId = index + 1
            // first time
            launchActivity<MainActivity>()
            onView(withId(id)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))
            // second time
            launchActivity<MainActivity>()
            onView(withId(id)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonNewIntent"))))
            // finish and open again
            pressBack() // go back to MainActivity
            onView(withId(id)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1 + instanceId)
        }
    }

    @Test
    fun launchOtherActivity() {
        val clazzs = arrayListOf(
            SingleInstance1Activity::class.java,
            SingleInstance2Activity::class.java,
            SingleInstance3Activity::class.java
        )
        clazzs.forEachIndexed { index, clazz ->
            val instanceId = index + 1
            val scenario = launchActivity<Activity>(Intent(getContext(), clazz))
            // wait for the Activity ready
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))
            scenario.onActivity {
                val intent = Intent(it, MainActivity::class.java)
                it.startActivity(intent)
            }
            // wait for MainActivity ready
            onView(withId(R.id.standard1)).check(matches(withText(startsWith("Standard"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(2)
            val activitiesMain = allTasks[0].getActivityStack()
            assertThat(activitiesMain).hasSize(1)
            assertThat(activitiesMain[0].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(activitiesMain[0].state).isEqualTo(ActivityInfo.State.Resumed)
            val activitiesOther = allTasks[1].getActivityStack()
            assertThat(activitiesOther).hasSize(1)
            assertThat(activitiesOther[0].componentName.className).isEqualTo(clazzs[index].name)
            assertThat(activitiesOther[0].state).isAnyOf(
                ActivityInfo.State.Stopped,
                ActivityInfo.State.Paused
            )

            // quit MainActivity
            pressBack()
            // the #close() had bug which will need to a 45 seconds timeout
            scenario.onActivity {
                it.finish()
            }
//            scenario.close()
        }
    }

    @Test
    fun flags_clearTask() {
        val buttons = arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
        val clazzs = arrayListOf(
            SingleInstance1Activity::class.java,
            SingleInstance2Activity::class.java,
            SingleInstance3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            val clazz = clazzs[index]

            // first time from MainActivity
            val scenario = launchActivity<MainActivity>()
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

            // launch it again from other place
            scenario.onActivity { activity ->
                val intent = Intent(getContext(), clazz)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(intent)
            }
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))
            assertThat(ActivityTaskTracker.getAllTasks()).hasSize(1 + instanceId)

            scenario.close()
        }
    }
}
