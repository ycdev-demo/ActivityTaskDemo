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
import me.ycdev.android.demo.activitytask.MainActivity
import me.ycdev.android.demo.activitytask.R
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop3Activity
import me.ycdev.android.lib.common.activity.ActivityRunningState
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import me.ycdev.android.lib.test.ui.ScrollViewsAction
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Test

class SingleTopActivityTest : ActivityTestBase() {
    /**
     * 'singleTask' Activity will always start in the current task.
     */
    @Test
    fun launchedByOnce() {
        val buttons = arrayListOf(R.id.single_top1, R.id.single_top2, R.id.single_top3)
        val clazzs = arrayListOf(
            SingleTop1Activity::class.java,
            SingleTop2Activity::class.java,
            SingleTop3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Top $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1)
            val activities = allTasks[0].getActivityStack()
            assertThat(activities).hasSize(2)
            assertThat(activities[0].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(activities[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
            assertThat(activities[1].componentName.className).isEqualTo(clazzs[index].name)
            assertThat(activities[1].state).isEqualTo(ActivityRunningState.State.Resumed)
        }
    }

    /**
     * The same top 'singleTop' Activity will be reused and onNewIntent() will be called.
     */
    @Test
    fun launchedByMultipleTimes() {
        val buttons = arrayListOf(R.id.single_top1, R.id.single_top2, R.id.single_top3)
        val clazzs = arrayListOf(
            SingleTop1Activity::class.java,
            SingleTop2Activity::class.java,
            SingleTop3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            // first time from MainActivity
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Top $instanceId\nonCreate"))))

            // launch it again
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Top $instanceId\nonNewIntent"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1)
            val activities = allTasks[0].getActivityStack()
            assertThat(activities).hasSize(2)
            assertThat(activities[0].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(activities[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
            assertThat(activities[1].componentName.className).isEqualTo(clazzs[index].name)
            assertThat(activities[1].state).isEqualTo(ActivityRunningState.State.Resumed)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + no FLAG_ACTIVITY_NEW_TASK + top: no clear
     *
     * Explain:
     * a) No 'FLAG_ACTIVITY_NEW_TASK' flag, don't clear the task
     * b) The current task's top Activity matches the target Activity, so reuse it.
     */
    @Test
    fun flags_clearTask_noNewTask_top() {
        val buttons = arrayListOf(R.id.single_top1, R.id.single_top2, R.id.single_top3)
        val clazzs = arrayListOf(
            SingleTop1Activity::class.java,
            SingleTop2Activity::class.java,
            SingleTop3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Top $instanceId\nonCreate"))))

            // launch it again with flags: CLEAR_TASK
            onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
            onView(withId(R.id.flags_clear_task)).perform(click()).check(matches(isChecked()))
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Top $instanceId\nonNewIntent"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1)
            val activities = allTasks[0].getActivityStack()
            assertThat(activities).hasSize(2)
            assertThat(activities[0].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(activities[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
            assertThat(activities[1].componentName.className).isEqualTo(clazzs[index].name)
            assertThat(activities[1].state).isEqualTo(ActivityRunningState.State.Resumed)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + no FLAG_ACTIVITY_NEW_TASK + top: no clear
     *
     * Explain:
     * a) No 'FLAG_ACTIVITY_NEW_TASK' flag, don't clear the task
     * b) The current task's top Activity doesn't match the target Activity, need to create a new one.
     */
    @Test
    fun flags_clearTask_noNewTask_middle() {
        val buttons = arrayListOf(R.id.single_top1, R.id.single_top2, R.id.single_top3)
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Top $instanceId\nonCreate"))))
            onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 1\nonCreate"))))

            // launch it again with flags: CLEAR_TASK
            onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
            onView(withId(R.id.flags_clear_task)).perform(click())
                .check(matches(isChecked()))
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Top $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1)
            val activities = allTasks[0].getActivityStack()
            assertThat(activities).hasSize(4)

            // clean up
            pressBack()
            pressBack()
            pressBack()
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + FLAG_ACTIVITY_NEW_TASK + same affinity: task will be cleared
     *
     * Explain:
     * a) The target task exist, so clear it (FLAG_ACTIVITY_CLEAR_TASK).
     * b) Create the Activity
     */
    @Test
    fun flags_clearTask_newTask_sameAffinity() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(2)
        }

        // launch it again with flags: CLEAR_TASK
        onView(withId(R.id.flags_new_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.flags_clear_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.single_top1))
            .perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))

        val allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(1)
        val activities = allTasks[0].getActivityStack()
        assertThat(activities).hasSize(1)
        assertThat(activities[0].componentName.className).isEqualTo(SingleTop1Activity::class.java.name)
        assertThat(activities[0].state).isEqualTo(ActivityRunningState.State.Resumed)
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + FLAG_ACTIVITY_NEW_TASK + two affinities + top: no task clear (onNewIntent)
     *
     * Explain:
     * a) The target task doesn't exist, so no clear (FLAG_ACTIVITY_CLEAR_TASK).
     * b) The current task's top Activity matches the target Activity, so reuse it.
     */
    @Test
    fun flags_clearTask_newTask_twoAffinities_top() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(2)
        }

        // launch it again with flags: CLEAR_TASK
        onView(withId(R.id.flags_new_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.flags_clear_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.single_top2))
            .perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonNewIntent"))))

        val allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(1)
        val activities = allTasks[0].getActivityStack()
        assertThat(activities).hasSize(2)
        assertThat(activities[0].componentName.className).isEqualTo(MainActivity::class.java.name)
        assertThat(activities[0].state).isAnyOf(
            ActivityRunningState.State.Stopped,
            ActivityRunningState.State.Paused
        )
        assertThat(activities[1].componentName.className).isEqualTo(SingleTop2Activity::class.java.name)
        assertThat(activities[1].state).isEqualTo(ActivityRunningState.State.Resumed)

        // clean up
        pressBack()
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + FLAG_ACTIVITY_NEW_TASK + two affinities + middle: no task clear (new task)
     *
     * Explain:
     * a) The target task doesn't exist, so no clear (FLAG_ACTIVITY_CLEAR_TASK).
     * b) The current task's top Activity doesn't match the target Activity, need to create a new one.
     * c) The target task doesn't exist, so create it (FLAG_ACTIVITY_NEW_TASK).
     */
    @Test
    fun flags_clearTask_newTask_twoAffinities_middle() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(3)
        }

        // launch it again with flags: CLEAR_TASK
        onView(withId(R.id.flags_new_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.flags_clear_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.single_top2))
            .perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        val allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(2)
        val activities = allTasks[0].getActivityStack()
        assertThat(activities).hasSize(1)
        assertThat(activities[0].componentName.className).isEqualTo(SingleTop2Activity::class.java.name)
        assertThat(activities[0].state).isEqualTo(ActivityRunningState.State.Resumed)
        assertThat(allTasks[1].getActivityStack()).hasSize(3)

        // clean up
        pressBack()
        pressBack()
        pressBack()
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + same affinity: clear the top Activities and reuse
     *
     * Explain:
     * a) 'singleTop' launch mode, the first target Activity instance from top to down
     *    in the stack will be selected
     * b) 'singleTop' launch mode, the target Activity will be reused
     */
    @Test
    fun flags_clearTop_noNewTask_sameTask() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        // the following two Activity will be cleared
        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(7)
        }

        // launch it again with flags: CLEAR_TOP
        onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
        onView(withId(R.id.flags_clear_top)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonNewIntent"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(6)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + FLAG_ACTIVITY_NEW_TASK + same affinity: clear the top Activities and reuse
     *
     * Explain:
     * a) 'singleTop' launch mode, the first target Activity instance from top to down
     *    in the stack will be selected
     * b) 'singleTop' launch mode, the target Activity will be reused
     */
    @Test
    fun flags_clearTop_newTask_sameTask() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        // the following two Activity will be cleared
        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(7)
        }

        // launch it again with flags: CLEAR_TOP
        onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.flags_clear_top)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonNewIntent"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(6)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + FLAG_ACTIVITY_NEW_TASK + two affinities: clear the top Activities and reuse
     *
     * Explain:
     * a) 'singleTop' launch mode, the first target Activity instance from top to down
     *    in the stack will be selected
     * b) 'singleTop' launch mode, the target Activity will be reused
     */
    @Test
    fun flags_clearTop_newTask_twoTasks() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        // task1
        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        // the following 2 activities in task 1 will be cleared
        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonCreate"))))
        onView(withId(R.id.single_top2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(7)
        }

        // task2
        onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].getActivityStack()).hasSize(1)
            assertThat(it[1].getActivityStack()).hasSize(7)
        }

        // launch it again with flags: CLEAR_TOP
        onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.flags_clear_top)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.single_top1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Top 1\nonNewIntent"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].getActivityStack()).hasSize(6)
            assertThat(it[1].getActivityStack()).hasSize(1)
        }
    }
}
