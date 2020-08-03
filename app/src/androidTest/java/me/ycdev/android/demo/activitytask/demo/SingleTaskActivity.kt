package me.ycdev.android.demo.activitytask.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.activitytask.MainActivity
import me.ycdev.android.demo.activitytask.R
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask3Activity
import me.ycdev.android.demo.activitytask.utils.ActivityUtils
import me.ycdev.android.lib.common.activity.ActivityInfo
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import me.ycdev.android.lib.test.ui.ScrollViewsAction
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SingleTaskActivity {
    @Before
    fun setup() {
        ActivityTaskTracker.enableDebugLog(true)
        ActivityUtils.finishAndRemoveAllTasks(getContext())
        assertThat(ActivityTaskTracker.getAllTasks()).hasSize(0)
    }

    private fun getContext(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun launchedByOnce() {
        val buttons = arrayListOf(R.id.single_task1, R.id.single_task2, R.id.single_task3)
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            val scenario = launchActivity<MainActivity>()
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1 + instanceId)

            scenario.close()
        }
    }

    @Test
    fun launchedByMultipleTimes() {
        val buttons = arrayListOf(R.id.single_task1, R.id.single_task2, R.id.single_task3)
        val clazzs = arrayListOf(
            SingleTask1Activity::class.java,
            SingleTask2Activity::class.java,
            SingleTask3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            val clazz = clazzs[index]

            // first time from MainActivity
            val scenario = launchActivity<MainActivity>()
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task $instanceId\nonCreate"))))

            // launch it again from other place
            scenario.onActivity { activity ->
                // Cannot use a new ActivityScenario to launch Activity!
                val intent = Intent(activity, clazz)
                activity.startActivity(intent)
            }
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task $instanceId\nonNewIntent"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1 + instanceId)

            scenario.close()
        }
    }

    @Test
    fun launchOtherActivity() {
        val clazzs = arrayListOf(
            SingleTask1Activity::class.java,
            SingleTask2Activity::class.java,
            SingleTask3Activity::class.java
        )
        clazzs.forEachIndexed { index, clazz ->
            val instanceId = index + 1
            val scenario = launchActivity<Activity>(Intent(getContext(), clazz))
            // wait for the Activity ready
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Task $instanceId\nonCreate"))))
            scenario.onActivity {
                val intent = Intent(it, MainActivity::class.java)
                it.startActivity(intent)
            }
            // wait for MainActivity ready
            onView(withId(R.id.standard1)).check(matches(withText(startsWith("Standard"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1)
            val activities = allTasks[0].getActivityStack()
            assertThat(activities).hasSize(2)
            assertThat(activities[0].componentName.className).isEqualTo(clazzs[index].name)
            assertThat(activities[0].state).isAnyOf(
                ActivityInfo.State.Stopped,
                ActivityInfo.State.Paused
            )
            assertThat(activities[1].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(activities[1].state).isEqualTo(ActivityInfo.State.Resumed)

            // quit MainActivity
            Espresso.pressBack()

            scenario.close()
        }
    }

    @Test
    fun flags_clearTask_notWork() {
        val buttons = arrayListOf(R.id.single_task1, R.id.single_task2, R.id.single_task3)
        val clazzs = arrayListOf(
            SingleTask1Activity::class.java,
            SingleTask2Activity::class.java,
            SingleTask3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            val clazz = clazzs[index]

            // first time from MainActivity
            val scenario = launchActivity<MainActivity>()
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task $instanceId\nonCreate"))))

            // launch it again from other place
            scenario.onActivity { activity ->
                val intent = Intent(activity, clazz)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(intent)
            }
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task $instanceId\nonCreate"))))
            assertThat(ActivityTaskTracker.getAllTasks()).hasSize(1 + instanceId)

            scenario.close()
        }
    }
}