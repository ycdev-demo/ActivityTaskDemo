package me.ycdev.android.demo.activitytask.demo

import android.content.ComponentName
import android.content.pm.ActivityInfo
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance3Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask3Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop3Activity
import me.ycdev.android.demo.activitytask.ui.demo.SpecialReparentingActivity
import me.ycdev.android.demo.activitytask.ui.demo.Standard1Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard2Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard3Activity
import me.ycdev.android.lib.common.activity.ActivityMeta
import org.junit.Test

class ManifestAttributesTest : ActivityTestBase() {
    @Test
    fun standardActivities() {
        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), Standard1Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_MULTIPLE)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), Standard2Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_TASK2)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_MULTIPLE)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), Standard3Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_MULTIPLE)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SpecialReparentingActivity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
            assertThat(it.allowTaskReparenting).isEqualTo(true)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_MULTIPLE)
        }
    }

    @Test
    fun singleTopActivities() {
        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleTop1Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_TOP)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleTop2Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_TASK2)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_TOP)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleTop3Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_TOP)
        }
    }

    @Test
    fun singleTaskActivities() {
        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleTask1Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_TASK)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleTask2Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_TASK2)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_TASK)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleTask3Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_TASK2)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_TASK)
        }
    }

    @Test
    fun singleInstanceActivities() {
        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleInstance1Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_DEFAULT)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_INSTANCE)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleInstance2Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_TASK2)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_INSTANCE)
        }

        ActivityMeta.get(
            getContext(),
            ComponentName(getContext(), SingleInstance3Activity::class.java)
        ).let {
            assertThat(it.taskAffinity).isEqualTo(TASK_AFFINITY_TASK2)
            assertThat(it.allowTaskReparenting).isEqualTo(false)
            assertThat(it.launchMode).isEqualTo(ActivityInfo.LAUNCH_SINGLE_INSTANCE)
        }
    }

    companion object {
        const val PACKAGE_NAME = "me.ycdev.android.demo.activitytask"
        const val TASK_AFFINITY_DEFAULT = PACKAGE_NAME
        const val TASK_AFFINITY_TASK2 = "me.ycdev.task2"
    }
}
