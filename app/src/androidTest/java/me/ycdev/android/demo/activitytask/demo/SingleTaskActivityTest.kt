package me.ycdev.android.demo.activitytask.demo

import android.content.Intent
import android.os.SystemClock
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
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask2Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard1Activity
import me.ycdev.android.lib.common.activity.ActivityRunningState
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import me.ycdev.android.lib.test.ui.ScrollViewsAction
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Test

class SingleTaskActivityTest : ActivityTestBase() {
    /**
     * 'singleTask' Activity will always start in the task specified by it's 'taskAffinity'.
     */
    @Test
    fun launchedByOnce() {
        // task1
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Task 1\nonCreate"))))
        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].taskAffinity).isEqualTo(ManifestAttributesTest.TASK_AFFINITY_DEFAULT)
            assertThat(it[0].getActivityStack()).hasSize(2)
        }

        // task2
        onView(withId(R.id.single_task2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Task 2\nonCreate"))))
        assertThat(ActivityTaskTracker.getAllTasks()).hasSize(2)

        // task2
        onView(withId(R.id.single_task3)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Task 3\nonCreate"))))
        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].taskAffinity).isEqualTo(ManifestAttributesTest.TASK_AFFINITY_TASK2)
            assertThat(it[0].getActivityStack()).hasSize(2)
        }

        // clean up
        pressBack()
        pressBack()
        pressBack()
    }

    /**
     * The second start of 'singleTask' will clear the other top Activities
     * and onNewIntent() will be called.
     */
    @Test
    fun launchedByMultipleTimes() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())

        val buttons = arrayListOf(R.id.single_task1, R.id.single_task2, R.id.single_task3)
        buttons.forEachIndexed { index, buttonId ->
            val instanceId = index + 1

            // first time from MainActivity
            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task $instanceId\nonCreate"))))

            onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 1\nonCreate"))))

            onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 2\nonCreate"))))

            onView(withId(R.id.standard3)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 3\nonCreate"))))

            val taskCount = if (index == 0) 1 else 2
            ActivityTaskTracker.getAllTasks().let {
                assertThat(it).hasSize(taskCount)
                assertThat(it[0].getActivityStack()).hasSize(6 - taskCount)
            }

            // launch it again
            onView(withId(buttonId)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task $instanceId\nonNewIntent"))))

            ActivityTaskTracker.getAllTasks().let {
                assertThat(it).hasSize(taskCount)
                assertThat(it[0].getActivityStack()).hasSize(3 - taskCount)
            }

            // clean up
            pressBack()
        }
    }

    @Test
    fun launchOther_sameTask_differentAffinity() {
        ActivityTestHelper.startDemoActivity(
            getContext(),
            SingleTask2Activity::class.java,
            "Single Task 2\nonCreate"
        )
        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))

        val allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(1)
        val activities = allTasks[0].getActivityStack()
        assertThat(activities).hasSize(2)
        assertThat(activities[0].componentName.className).isEqualTo(SingleTask2Activity::class.java.name)
        assertThat(activities[0].state).isAnyOf(
            ActivityRunningState.State.Stopped,
            ActivityRunningState.State.Paused
        )
        assertThat(activities[1].componentName.className).isEqualTo(Standard1Activity::class.java.name)
        assertThat(activities[1].state).isEqualTo(ActivityRunningState.State.Resumed)

        // clean up
        pressBack()
    }

    @Test
    fun launchOther_twoTasks() {
        ActivityTestHelper.startDemoActivity(
            getContext(),
            SingleTask2Activity::class.java,
            "Single Task 2\nonCreate"
        )
        onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Task 1\nonCreate"))))

        val allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(2)
        allTasks[0].getActivityStack().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].componentName.className).isEqualTo(SingleTask1Activity::class.java.name)
            assertThat(it[0].state).isEqualTo(ActivityRunningState.State.Resumed)
        }
        allTasks[1].getActivityStack().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].componentName.className).isEqualTo(SingleTask2Activity::class.java.name)
            assertThat(it[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
        }

        // clean up
        pressBack()
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + same affinity + top: clear it
     *
     * Explain:
     * a) The target task exists, so clear it (FLAG_ACTIVITY_CLEAR_TASK).
     * b) Create the Activity
     */
    @Test
    fun flags_clearTask_sameTask_top() {
        arrayOf(false, true).forEach { newTask ->
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            // first time from MainActivity
            onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 1\nonCreate"))))
            ActivityTaskTracker.getAllTasks().let {
                assertThat(it).hasSize(1)
                assertThat(it[0].getActivityStack()).hasSize(2)
            }

            // launch it again with flags: CLEAR_TASK
            if (newTask) {
                onView(withId(R.id.flags_new_task)).perform(click())
                    .check(matches(isChecked()))
            } else {
                onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
            }
            onView(withId(R.id.flags_clear_task)).perform(click())
                .check(matches(isChecked()))
            onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 1\nonCreate"))))

            // wait for the old SingleTask1Activity to destroy
            val startTime = SystemClock.elapsedRealtime()
            while (true) {
                if (ActivityTaskTracker.getTotalActivitiesCount() == 1 ||
                    (SystemClock.elapsedRealtime() - startTime > 5_000)
                ) {
                    break
                }
                SystemClock.sleep(50)
            }

            ActivityTaskTracker.getAllTasks().let { allTasks ->
                assertThat(allTasks).hasSize(1)
                allTasks[0].getActivityStack().let { allActivities ->
                    assertThat(allActivities).hasSize(1)
                    assertThat(allActivities[0].componentName.className).isEqualTo(
                        SingleTask1Activity::class.java.name
                    )
                }
            }
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + same affinity + middle: clear it
     *
     * Explain:
     * a) The target task exists, so clear it (FLAG_ACTIVITY_CLEAR_TASK).
     * b) Create the Activity
     */
    @Test
    fun flags_clearTask_sameTask_middle() {
        arrayOf(false, true).forEach { newTask ->
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            // first time from MainActivity
            onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 1\nonCreate"))))
            onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 1\nonCreate"))))
            ActivityTaskTracker.getAllTasks().let {
                assertThat(it).hasSize(1)
                assertThat(it[0].getActivityStack()).hasSize(3)
            }

            // launch it again with flags: CLEAR_TASK
            if (newTask) {
                onView(withId(R.id.flags_new_task)).perform(click())
                    .check(matches(isChecked()))
            } else {
                onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
            }
            onView(withId(R.id.flags_clear_task)).perform(click())
                .check(matches(isChecked()))
            onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 1\nonCreate"))))

            ActivityTaskTracker.getAllTasks().let { allTasks ->
                assertThat(allTasks).hasSize(1)
                allTasks[0].getActivityStack().let { allActivities ->
                    assertThat(allActivities).hasSize(1)
                    assertThat(allActivities[0].componentName.className).isEqualTo(
                        SingleTask1Activity::class.java.name
                    )
                }
            }
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TASK + two affinities: clear it
     *
     * Explain:
     * a) The target task exists, so clear it (FLAG_ACTIVITY_CLEAR_TASK).
     * b) Create the Activity
     */
    @Test
    fun flags_clearTask_twoTasks() {
        arrayOf(false, true).forEach { newTask ->
            // task1
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 1\nonCreate"))))

            ActivityTaskTracker.getAllTasks().let {
                assertThat(it).hasSize(1)
                assertThat(it[0].getActivityStack()).hasSize(2)
            }

            // task2
            onView(withId(R.id.single_task2)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 2\nonCreate"))))
            onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 1\nonCreate"))))

            assertThat(ActivityTaskTracker.getAllTasks()).hasSize(2)
            assertThat(ActivityTaskTracker.getFocusedTask()!!.getActivityStack()).hasSize(2)

            // launch it again with flags: CLEAR_TASK
            // the task was cleared
            if (newTask) {
                onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
            } else {
                onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
            }
            onView(withId(R.id.flags_clear_task)).perform(click())
                .check(matches(isChecked()))
            onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 1\nonCreate"))))

            // launch it again to clear the previous task
            ActivityTestHelper.startDemoActivity(
                getContext(),
                SingleTask1Activity::class.java,
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK,
                "Single Task 1\nonCreate"
            )

            // wait for the old SingleTask1Activity to destroy
            val startTime = SystemClock.elapsedRealtime()
            while (true) {
                if (ActivityTaskTracker.getTotalActivitiesCount() == 2 ||
                    (SystemClock.elapsedRealtime() - startTime > 5_000)
                ) {
                    break
                }
                SystemClock.sleep(50)
            }

            ActivityTaskTracker.getAllTasks().let { allTasks ->
                assertThat(allTasks).hasSize(2)
                allTasks[0].getActivityStack().let { allActivities ->
                    assertThat(allActivities).hasSize(1)
                    assertThat(allActivities[0].componentName.className).isEqualTo(
                        SingleTask1Activity::class.java.name
                    )
                }
                allTasks[1].getActivityStack().let { allActivities ->
                    assertThat(allActivities).hasSize(2)
                    assertThat(allActivities[0].componentName.className).isEqualTo(
                        SingleTask2Activity::class.java.name
                    )
                    assertThat(allActivities[1].componentName.className).isEqualTo(Standard1Activity::class.java.name)
                }
            }

            // clean up: workaround to quit task2
            ActivityTestHelper.startDemoActivity(
                getContext(),
                SingleTask2Activity::class.java,
                Intent.FLAG_ACTIVITY_CLEAR_TASK,
                "Single Task 2\nonCreate"
            )
            pressBack()

            // only task1 left
            ActivityTaskTracker.getAllTasks().let { allTasks ->
                assertThat(allTasks).hasSize(1)
                allTasks[0].getActivityStack().let { allActivities ->
                    assertThat(allActivities).hasSize(1)
                    assertThat(allActivities[0].componentName.className).isEqualTo(
                        SingleTask1Activity::class.java.name
                    )
                }
            }
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + same affinity: clear the top Activities and reuse
     *
     * Explain:
     * a) the target Activity instance will be reused
     */
    @Test
    fun flags_clearTop_sameTask() {
        arrayOf(false, true).forEach { newTask ->
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 1\nonCreate"))))

            onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 1\nonCreate"))))

            onView(withId(R.id.standard2)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 2\nonCreate"))))

            onView(withId(R.id.standard3)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 3\nonCreate"))))

            ActivityTaskTracker.getAllTasks().let {
                assertThat(it).hasSize(1)
                assertThat(it[0].getActivityStack()).hasSize(5)
            }

            // restart SingleTask1Activity
            if (newTask) {
                onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
            } else {
                onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
            }
            onView(withId(R.id.flags_clear_top)).perform(click()).check(matches(isChecked()))
            onView(withId(R.id.single_task1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 1\nonNewIntent"))))

            ActivityTaskTracker.getAllTasks().let { allTasks ->
                assertThat(allTasks).hasSize(1)
                allTasks[0].getActivityStack().let { allActivities ->
                    assertThat(allActivities).hasSize(2)
                    assertThat(allActivities[0].componentName.className).isEqualTo(MainActivity::class.java.name)
                    assertThat(allActivities[1].componentName.className).isEqualTo(
                        SingleTask1Activity::class.java.name
                    )
                }
            }

            // clean up
            pressBack()
        }
    }

    /**
     * FLAG_ACTIVITY_CLEAR_TOP + same affinity: clear the top Activities and reuse
     *
     * Explain:
     * a) the target Activity instance will be reused
    */
    @Test
    fun flags_clearTop_twoTasks() {
        arrayOf(false, true).forEach { newTask ->
            ActivityTestHelper.clearAndStartMainActivity(getContext())

            onView(withId(R.id.single_task2)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 2\nonCreate"))))

            onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Standard 1\nonCreate"))))

            ActivityTaskTracker.getAllTasks().let {
                assertThat(it).hasSize(2)
                assertThat(it[0].getActivityStack()).hasSize(2)
            }

            // restart SingleTask2Activity
            if (newTask) {
                onView(withId(R.id.flags_new_task)).perform(click()).check(matches(isChecked()))
            } else {
                onView(withId(R.id.flags_new_task)).check(matches(isNotChecked()))
            }
            onView(withId(R.id.flags_clear_top)).perform(click()).check(matches(isChecked()))
            onView(withId(R.id.single_task2)).perform(ScrollViewsAction(), click())
            onView(withId(R.id.content))
                .check(matches(withText(startsWith("Single Task 2\nonNewIntent"))))

            ActivityTaskTracker.getAllTasks().let { allTasks ->
                assertThat(allTasks).hasSize(2)
                allTasks[0].getActivityStack().let { allActivities ->
                    assertThat(allActivities).hasSize(1)
                    assertThat(allActivities[0].componentName.className).isEqualTo(
                        SingleTask2Activity::class.java.name
                    )
                }
            }

            // clean up
            pressBack()
        }
    }
}
