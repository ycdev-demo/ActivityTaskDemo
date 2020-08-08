package me.ycdev.android.demo.activitytask.demo

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.activitytask.MainActivity
import me.ycdev.android.demo.activitytask.R
import me.ycdev.android.demo.activitytask.ui.demo.Standard1Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard2Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard3Activity
import me.ycdev.android.lib.common.activity.ActivityRunningState
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import me.ycdev.android.lib.test.ui.ScrollViewsAction
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandardActivityTest {
    @Before
    fun setup() {
        ActivityTaskTracker.enableDebugLog(true)
        ActivityTestHelper.finishAndRemoveAllTasks(getContext())
        assertThat(ActivityTaskTracker.getAllTasks()).hasSize(0)
    }

    private fun getContext(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun launchedByOnce() {
        val buttons = arrayListOf(R.id.standard1, R.id.standard2, R.id.standard3)
        val clazzs = arrayListOf(
            Standard1Activity::class.java,
            Standard2Activity::class.java,
            Standard3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard $instanceId\nonCreate"))))

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

    @Test
    fun launchedByMultipleTimes() {
        val buttons = arrayListOf(R.id.standard1, R.id.standard2, R.id.standard3)
        val clazzs = arrayListOf(
            Standard1Activity::class.java,
            Standard2Activity::class.java,
            Standard3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            // first time from MainActivity
            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard $instanceId\nonCreate"))))

            // launch it again
            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1)
            val activities = allTasks[0].getActivityStack()
            assertThat(activities).hasSize(3)
            assertThat(activities[0].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(activities[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
            assertThat(activities[1].componentName.className).isEqualTo(clazzs[index].name)
            assertThat(activities[1].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
            assertThat(activities[2].componentName.className).isEqualTo(clazzs[index].name)
            assertThat(activities[2].state).isEqualTo(ActivityRunningState.State.Resumed)
        }
    }

    @Test
    fun flags_singleTop() {
        val buttons = arrayListOf(R.id.standard1, R.id.standard2, R.id.standard3)
        val clazzs = arrayListOf(
            Standard1Activity::class.java,
            Standard2Activity::class.java,
            Standard3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            // first time from MainActivity
            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard $instanceId\nonCreate"))))

            // launch it again with flag SINGLE_TOP
            onView(withId(R.id.flags_single_top)).perform(click()).check(matches(isChecked()))
            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard $instanceId\nonNewIntent"))))

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

    @Test
    fun flags_newTask_affinityDefault() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())
        onView(withId(R.id.standard1))
            .perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))

        val allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(1)
        val activities = allTasks[0].getActivityStack()
        assertThat(activities).hasSize(2)
        assertThat(activities[0].componentName.className).isEqualTo(MainActivity::class.java.name)
        assertThat(activities[0].state).isAnyOf(
            ActivityRunningState.State.Stopped,
            ActivityRunningState.State.Paused
        )
        assertThat(activities[1].componentName.className).isEqualTo(Standard1Activity::class.java.name)
        assertThat(activities[1].state).isEqualTo(ActivityRunningState.State.Resumed)
    }

    @Test
    fun flags_newTask_affinityOther() {
        val buttons = arrayListOf(R.id.standard2, R.id.standard3)
        val clazzs = arrayListOf(
            Standard2Activity::class.java,
            Standard3Activity::class.java
        )
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 2
            val clazz = clazzs[index]
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(2)
            val activitiesMain = allTasks[1].getActivityStack()
            assertThat(activitiesMain).hasSize(1)
            assertThat(activitiesMain[0].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(activitiesMain[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
            val activitiesOther = allTasks[0].getActivityStack()
            assertThat(activitiesOther).hasSize(1)
            assertThat(activitiesOther[0].componentName.className).isEqualTo(clazz.name)
            assertThat(activitiesOther[0].state).isEqualTo(ActivityRunningState.State.Resumed)

            // quit the current Activity
            pressBack()
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + no FLAG_ACTIVITY_NEW_TASK: no clear
     *
     * Explain:
     * a) No 'FLAG_ACTIVITY_NEW_TASK' flag, don't clear the task
     * b) Create the Activity
     */
    @Test
    fun flags_clearTask_noNewTask() {
        val buttons = arrayListOf(R.id.standard1, R.id.standard2, R.id.standard3)
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard $instanceId\nonCreate"))))

            // launch it again with flags: CLEAR_TASK
            onView(withId(R.id.flags_new_task)).check(matches(ViewMatchers.isNotChecked()))
            onView(withId(R.id.flags_clear_task)).perform(click())
                .check(matches(isChecked()))
            onView(withId(buttonId))
                .perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard $instanceId\nonCreate"))))

            val allTasks = ActivityTaskTracker.getAllTasks()
            assertThat(allTasks).hasSize(1)
            val activities = allTasks[0].getActivityStack()
            assertThat(activities).hasSize(3)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + FLAG_ACTIVITY_NEW_TASK + same affinity: task will be cleared
     *
     * Explain:
     * a) The target task exists, so clear it (FLAG_ACTIVITY_CLEAR_TASK).
     * b) Create the Activity
     */
    @Test
    fun flags_clearTask_newTask_sameAffinity() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(2)
        }

        // launch it again with flags: CLEAR_TASK
        onView(withId(R.id.flags_new_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.flags_clear_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.standard1))
            .perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))

        val allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(1)
        val activities = allTasks[0].getActivityStack()
        assertThat(activities).hasSize(1)
        assertThat(activities[0].componentName.className).isEqualTo(Standard1Activity::class.java.name)
        assertThat(activities[0].state).isEqualTo(ActivityRunningState.State.Resumed)
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + FLAG_ACTIVITY_NEW_TASK + two affinities: no task clear (onNewIntent)
     *
     * Explain:
     * a) The target task doesn't exist, so no clear (FLAG_ACTIVITY_CLEAR_TASK).
     * b) The target task doesn't exist, so create it (FLAG_ACTIVITY_NEW_TASK).
     */
    @Test
    fun flags_clearTask_newTask_twoAffinities() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(2)
        }

        // launch it again with flags: CLEAR_TASK
        onView(withId(R.id.flags_new_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.flags_clear_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.standard2))
            .perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        val allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(2)
        assertThat(allTasks[0].getActivityStack()).hasSize(1)
        assertThat(allTasks[1].getActivityStack()).hasSize(2)

        // clean up
        pressBack()
        pressBack()
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + same affinity: clear the top Activities and recreate
     *
     * Explain:
     * a) No 'FLAG_ACTIVITY_NEW_TASK' flag, the first target Activity instance from top to down
     *    in the stack will be selected
     * b) No 'FLAG_ACTIVITY_SINGLE_TOP' flag, the target Activity will be recreate
     */
    @Test
    fun flags_clearTop_noSingleTop_noNewTask_sameTask() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        // the following two Activity will be cleared
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        val targetHashCode = ActivityTaskTracker.getAllTasks()[0].topActivity().hashCode

        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(7)
        }

        // launch it again with flags: CLEAR_TOP
        onView(withId(R.id.flags_new_task)).check(matches(ViewMatchers.isNotChecked()))
        onView(withId(R.id.flags_clear_top)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(6)
            assertThat(it[0].topActivity().hashCode).isNotEqualTo(targetHashCode)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + FLAG_ACTIVITY_SINGLE_TOP + same affinity: clear the top Activities and reuse
     *
     * Explain:
     * a) No 'FLAG_ACTIVITY_NEW_TASK' flag, the first target Activity instance from top to down
     *    in the stack will be selected
     * b) Has 'FLAG_ACTIVITY_SINGLE_TOP' flag, the target Activity instance will be reused
     */
    @Test
    fun flags_clearTop_singleTop_noNewTask_sameTask() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        // the following two Activity will be cleared
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        val targetHashCode = ActivityTaskTracker.getAllTasks()[0].topActivity().hashCode

        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(7)
        }

        // launch it again with flags: CLEAR_TOP
        onView(withId(R.id.flags_new_task)).check(matches(ViewMatchers.isNotChecked()))
        onView(withId(R.id.flags_single_top)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.flags_clear_top)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonNewIntent"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(6)
            assertThat(it[0].topActivity().hashCode).isEqualTo(targetHashCode)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + FLAG_ACTIVITY_NEW_TASK + same affinity: clear the top Activities and recreate
     *
     * Explain:
     * a) Has 'FLAG_ACTIVITY_NEW_TASK' but no 'FLAG_ACTIVITY_SINGLE_TOP' flags,
     *    the second target Activity instance from top to down in the stack will be selected
     * b) No 'FLAG_ACTIVITY_SINGLE_TOP' flag, the target Activity instance will be recreate
     */
    @Test
    fun flags_clearTop_noSingleTop_newTask_sameTask() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        // the following four Activity will be cleared
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        val targetHashCode = ActivityTaskTracker.getAllTasks()[0].topActivity().hashCode

        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(7)
        }

        // launch it again with flags: CLEAR_TOP
        onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.flags_clear_top)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(4)
            assertThat(it[0].topActivity().hashCode).isNotEqualTo(targetHashCode)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + FLAG_ACTIVITY_SINGLE_TOP + FLAG_ACTIVITY_NEW_TASK + same affinity:
     *   clear the top Activities and reuse
     *
     * Explain:
     * a) Has 'FLAG_ACTIVITY_NEW_TASK' and 'FLAG_ACTIVITY_SINGLE_TOP' flags,
     *    the first target Activity instance from top to down in the stack will be selected
     * b) Has 'FLAG_ACTIVITY_SINGLE_TOP' flag, the target Activity instance will be reused
     */
    @Test
    fun flags_clearTop_singleTop_newTask_sameTask() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        // the following 2 Activity will be cleared
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        val targetHashCode = ActivityTaskTracker.getAllTasks()[0].topActivity().hashCode

        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(7)
        }

        // launch it again with flags: CLEAR_TOP
        onView(withId(R.id.flags_new_task)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.flags_single_top)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.flags_clear_top)).perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonNewIntent"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(6)
            assertThat(it[0].topActivity().hashCode).isEqualTo(targetHashCode)
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + FLAG_ACTIVITY_NEW_TASK + two affinities: clear the top Activities and recreate
     *
     * Explain:
     * a) Has 'FLAG_ACTIVITY_NEW_TASK' but no 'FLAG_ACTIVITY_SINGLE_TOP' flags,
     *    the second target Activity instance from top to down in the stack will be selected
     * b) No 'FLAG_ACTIVITY_SINGLE_TOP' flag, the target Activity instance will be recreate
     */
    @Test
    fun flags_clearTop_newTask_twoTasks() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        // task1
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        // the following 4 activities in task 1 will be cleared
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 2\nonCreate"))))

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
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].getActivityStack()).hasSize(4)
            assertThat(it[1].getActivityStack()).hasSize(1)
        }
    }
}
