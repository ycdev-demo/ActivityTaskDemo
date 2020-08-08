package me.ycdev.android.demo.activitytask.demo

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.activitytask.MainActivity
import me.ycdev.android.demo.activitytask.R
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SpecialClearOnLaunchActivity
import me.ycdev.android.demo.activitytask.ui.demo.SpecialReparentingActivity
import me.ycdev.android.lib.common.activity.ActivityRunningState
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import me.ycdev.android.lib.test.ui.ScrollViewsAction
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpecialActivityTest : ActivityTestBase() {
    @Test
    fun taskReparenting() {
        ActivityTestHelper.startMainActivity(getContext())

        onView(withId(R.id.single_task2))
            .perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Task 2\nonCreate"))))
        assertThat(ActivityTaskTracker.getAllTasks()).hasSize(2)

        // start a "allowTaskReparenting" Activity
        onView(withId(R.id.reparenting))
            .perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Reparenting\nonCreate"))))
        assertThat(ActivityTaskTracker.getAllTasks()).hasSize(2)

        var allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(2)

        assertThat(allTasks[0].taskAffinity).isEqualTo(TASK_AFFINITY_TASK2)
        allTasks[0].getActivityStack().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].componentName.className).isEqualTo(SingleTask2Activity::class.java.name)
            assertThat(it[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
            assertThat(it[1].componentName.className).isEqualTo(SpecialReparentingActivity::class.java.name)
            assertThat(it[1].state).isEqualTo(ActivityRunningState.State.Resumed)
        }

        assertThat(allTasks[1].taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
        allTasks[1].getActivityStack().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(it[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
        }

        // trigger task re-parenting
        // Must use 'FLAG_ACTIVITY_RESET_TASK_IF_NEEDED' to enable re-parenting feature
        ActivityTestHelper.startMainActivityFromLauncher(getContext())

        // check task re-parenting result
        allTasks = ActivityTaskTracker.getAllTasks()
        assertThat(allTasks).hasSize(2)

        assertThat(allTasks[0].taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
        allTasks[0].getActivityStack().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].componentName.className).isEqualTo(MainActivity::class.java.name)
            assertThat(it[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
            assertThat(it[1].componentName.className).isEqualTo(SpecialReparentingActivity::class.java.name)
            assertThat(it[1].state).isEqualTo(ActivityRunningState.State.Resumed)
        }

        assertThat(allTasks[1].taskAffinity).isEqualTo(TASK_AFFINITY_TASK2)
        allTasks[1].getActivityStack().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].componentName.className).isEqualTo(SingleTask2Activity::class.java.name)
            assertThat(it[0].state).isAnyOf(
                ActivityRunningState.State.Stopped,
                ActivityRunningState.State.Paused
            )
        }
    }

    @Test
    fun finishOnLaunch() {
        ActivityTestHelper.clearAndStartMainActivity(getContext())
        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(1)
        }
        val oldHashCode = ActivityTaskTracker.getFocusedTask()!!.topActivity().hashCode

        onView(withId(R.id.finish_on_launch)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Finish On Launch\nonCreate"))))
        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(2)
        }

        // task2
        onView(withId(R.id.single_task2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Task 2\nonCreate"))))

        // go back to it
        ActivityTestHelper.startMainActivity(getContext())

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].getActivityStack()).hasSize(1)
            assertThat(it[0].topActivity().hashCode).isEqualTo(oldHashCode)
            assertThat(it[0].topActivity().componentName.className).isEqualTo(
                MainActivity::class.java.name)
            assertThat(it[1].getActivityStack()).hasSize(1)
            assertThat(it[1].topActivity().componentName.className).isEqualTo(SingleTask2Activity::class.java.name)
        }
    }

    @Test
    fun clearOnLaunch() {
        ActivityTestHelper.startDemoActivity(
            getContext(), SpecialClearOnLaunchActivity::class.java,
            "Clear On Launch\nonCreate"
        )
        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(1)
        }
        val oldHashCode = assertThat(ActivityTaskTracker.getFocusedTask()!!.topActivity().hashCode)

        onView(withId(R.id.standard1)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Standard 1\nonCreate"))))
        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(1)
            assertThat(it[0].getActivityStack()).hasSize(2)
        }

        // task2
        onView(withId(R.id.single_task2)).perform(ScrollViewsAction(), click())
        onView(withId(R.id.content))
            .check(matches(withText(startsWith("Single Task 2\nonCreate"))))
        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].getActivityStack()).hasSize(1)
            assertThat(it[1].getActivityStack()).hasSize(2)
        }

        // go back to it
        ActivityTestHelper.startDemoActivity(
            getContext(), SpecialClearOnLaunchActivity::class.java,
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED,
            "Clear On Launch\nonCreate"
        )

        ActivityTaskTracker.getAllTasks().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].getActivityStack()).hasSize(1)
            assertThat(it[0].topActivity().hashCode).isNotEqualTo(oldHashCode)
            assertThat(it[0].topActivity().componentName.className).isEqualTo(
                SpecialClearOnLaunchActivity::class.java.name)
            assertThat(it[1].getActivityStack()).hasSize(1)
            assertThat(it[1].topActivity().componentName.className).isEqualTo(SingleTask2Activity::class.java.name)
        }
    }

    companion object {
        private const val TASK_AFFINITY_DEFAULT = ManifestAttributesTest.TASK_AFFINITY_DEFAULT
        private const val TASK_AFFINITY_TASK2 = ManifestAttributesTest.TASK_AFFINITY_TASK2
    }
}
