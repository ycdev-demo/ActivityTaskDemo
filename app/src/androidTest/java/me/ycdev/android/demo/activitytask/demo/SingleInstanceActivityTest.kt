package me.ycdev.android.demo.activitytask.demo

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.activitytask.R
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance3Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard1Activity
import me.ycdev.android.lib.common.activity.ActivityRunningState
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import me.ycdev.android.lib.test.ui.ScrollViewsAction
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Test

class SingleInstanceActivityTest : ActivityTestBase() {
    /**
     * There will be 4 tasks. Every 'singleInstance' Activity has its own task.
     */
    @Test
    fun separateTask_self() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        val buttons = arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
        buttons.forEachIndexed { index, id ->
            val instanceId = index + 1

            onView(withId(id)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1 + instanceId)
        }
    }

    @Test
    fun onNewIntent() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        val buttons = arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
        buttons.forEachIndexed { index, id ->
            val instanceId = index + 1

            // first time
            onView(withId(id)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))
            // second time
            onView(withId(id)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonNewIntent"))))
            // finish and open again
            pressBack() // go back to MainActivity
            onView(withId(id)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(2)

            pressBack() // to back to MainActivity
        }
    }

    /**
     * Other normal Activity will start in its own task.
     */
    @Test
    fun launchOther_separateTask() {
        val clazzs = arrayListOf(
            SingleInstance1Activity::class.java,
            SingleInstance2Activity::class.java,
            SingleInstance3Activity::class.java
        )
        clazzs.forEachIndexed { index, clazz ->
            val instanceId = index + 1
            ActivityTestHelper.startDemoActivity(getContext(), clazz, "Single Instance $instanceId\nonCreate")
            onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content)).check(matches(withText(startsWith("Standard 1\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1 + instanceId)
            val activitiesMain = allTasks[0].getActivityStack()
            assertThat(activitiesMain).hasSize(1)
            assertThat(activitiesMain[0].componentName.className).isEqualTo(Standard1Activity::class.java.name)
            assertThat(activitiesMain[0].state).isEqualTo(ActivityRunningState.State.Resumed)

            pressBack() // quit "Standard 1"
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK will clear the task.
     */
    @Test
    fun flags_clearTask_focused() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        arrayOf(false, true).forEach { newTask ->
            val buttons =
                arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
            buttons.forEachIndexed { index, buttonId ->
                val instanceId = index + 1

                onView(withId(buttonId)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

                // launch it again with flags: CLEAR_TASK
                if (newTask) {
                    onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
                } else {
                    onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
                }
                onView(withId(R.id.flags_clear_task)).perform(click()).check(matches(isChecked()))
                onView(withId(buttonId)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

                assertThat(ActivityTaskTracker.getAllTasks()).hasSize(2)

                // go back to MainActivity
                pressBack()
            }
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK will clear the task.
     */
    @Test
    fun flags_clearTask_notFocused() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        arrayOf(false, true).forEach { newTask ->
            val buttons =
                arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
            buttons.forEachIndexed { index, buttonId ->
                val instanceId = index + 1

                onView(withId(buttonId)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

                onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Standard 1\nonCreate"))))

                // launch it again with flags: CLEAR_TASK
                if (newTask) {
                    onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
                } else {
                    onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
                }
                onView(withId(R.id.flags_clear_task)).perform(click()).check(matches(isChecked()))
                onView(withId(buttonId)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

                assertThat(ActivityTaskTracker.getAllTasks()).hasSize(2)

                // go back to MainActivity
                pressBack()
                pressBack()
            }
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP will NOT clear the task. #onNewIntent() will be called.
     */
    @Test
    fun flags_clearTop_focused() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        arrayOf(false, true).forEach { newTask ->
            val buttons =
                arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
            buttons.forEachIndexed { index, buttonId ->
                val instanceId = index + 1

                onView(withId(buttonId)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

                // launch it again with flags: CLEAR_TOP
                if (newTask) {
                    onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
                } else {
                    onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
                }
                onView(withId(R.id.flags_clear_top)).perform(click()).check(matches(isChecked()))
                onView(withId(buttonId)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Single Instance $instanceId\nonNewIntent"))))

                assertThat(ActivityTaskTracker.getAllTasks()).hasSize(2)

                // go back to MainActivity
                pressBack()
            }
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP will NOT clear the task. #onNewIntent() will be called.
     */
    @Test
    fun flags_clearTop_notFocused() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        arrayOf(false, true).forEach { newTask ->
            val buttons =
                arrayListOf(R.id.single_instance1, R.id.single_instance2, R.id.single_instance3)
            buttons.forEachIndexed { index, buttonId ->
                val instanceId = index + 1

                onView(withId(buttonId)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Single Instance $instanceId\nonCreate"))))

                onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Standard 1\nonCreate"))))

                // launch it again with flags: CLEAR_TOP
                if (newTask) {
                    onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
                } else {
                    onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
                }
                onView(withId(R.id.flags_clear_top)).perform(click()).check(matches(isChecked()))
                onView(withId(buttonId)).perform(ScrollViewsAction(), click())
                onView(withId(R.id.content))
                    .check(matches(withText(startsWith("Single Instance $instanceId\nonNewIntent"))))

                assertThat(ActivityTaskTracker.getAllTasks()).hasSize(2)

                // go back to MainActivity
                pressBack()
                pressBack()
            }
        }
    }
}
