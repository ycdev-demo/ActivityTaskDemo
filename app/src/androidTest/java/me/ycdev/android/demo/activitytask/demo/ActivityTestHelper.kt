package me.ycdev.android.demo.activitytask.demo

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.activitytask.MainActivity
import me.ycdev.android.demo.activitytask.R
import me.ycdev.android.lib.common.activity.ActivityTaskTracker
import org.hamcrest.CoreMatchers.startsWith

/**
 * * Don't use ActivityScenario to start the Activity in task related test cases.
 *   It may cause unexpected behaviours.
 * * TODO: When do the task re-parenting test, if go to launcher and then go back to the app,
 *   there will be 5 seconds to wait for the Activity displayed.
 * * Using intent to start Activity may not work
 */
object ActivityTestHelper {
    const val LAUNCH_TIMEOUT: Long = 10_000 // 10 seconds

    private fun getUiDevice(): UiDevice {
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    fun finishAndRemoveAllTasks(context: Context, waitForDone: Boolean = true) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.appTasks.forEach {
            it.finishAndRemoveTask()
        }
        if (waitForDone) {
            val timeStart = SystemClock.elapsedRealtime()
            while (true) {
                if (ActivityTaskTracker.getTotalActivitiesCount() == 0 ||
                    (SystemClock.elapsedRealtime() - timeStart > LAUNCH_TIMEOUT)
                ) {
                    break
                }
                SystemClock.sleep(50)
            }
        }
    }

    fun switchTask(context: Context, taskId: Int) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val task = am.appTasks.find { getTaskId(it.taskInfo) == taskId }
            ?: throw RuntimeException("Task[$taskId] not found")
        task.moveToFront()
    }

    private fun getTaskId(info: ActivityManager.RecentTaskInfo): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return info.taskId
        } else {
            @Suppress("DEPRECATION")
            return info.id
        }
    }

    fun gotoLauncher() {
        val uiDevice = getUiDevice()
        uiDevice.pressHome()
        // Wait for the launcher displayed
        val launcherPackage: String = uiDevice.launcherPackageName
        assertThat(launcherPackage).isNotNull()
        uiDevice.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            LAUNCH_TIMEOUT
        )
    }

    /**
     * Use UiDevice to wait for app ready.
     */
    fun startMainActivityFromLauncher(context: Context) {
        // use flags which launcher is using to start app
        startMainActivityFromLauncher(
            context,
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        )
    }

    fun startMainActivityFromLauncher(context: Context, flags: Int) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = flags
        context.startActivity(intent)

        // wait for the Activity displayed
        getUiDevice().wait(
            Until.hasObject(By.pkg(ManifestAttributesTest.PACKAGE_NAME).depth(0)),
            LAUNCH_TIMEOUT
        )
    }

    fun startMainActivity(context: Context) {
        // use flags which launcher is using to start app
        startMainActivity(
            context,
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        )
    }

    fun clearAndStartMainActivity(context: Context) {
        // use flags which launcher is using to start app
        startMainActivity(
            context,
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
    }

    /**
     * RootViewPicker used by Espresso has such a policy to wait for the View ready:
     * [10ms, 50ms, 100ms, 500ms, 2_000ms, 30_000ms].
     * If you start an Activity from launcher, it may has a long delay. In that case, you'd better
     * use the [startMainActivityFromLauncher].
     */
    fun startMainActivity(context: Context, flags: Int) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = flags
        context.startActivity(intent)

        // wait for the Activity displayed
        onView(withId(R.id.nav_view)).check(matches(withId(R.id.nav_view)))
    }

    fun startDemoActivity(context: Context, clazz: Class<*>, textPrefix: String) {
        startDemoActivity(context, clazz, 0, textPrefix)
    }

    fun startDemoActivity(context: Context, clazz: Class<*>, flags: Int, textPrefix: String) {
        val intent = Intent(context, clazz)
        intent.flags = flags
        context.startActivity(intent)

        // wait for the activity displayed
        onView(withId(R.id.content)).check(matches(withText(startsWith(textPrefix))))
    }
}
