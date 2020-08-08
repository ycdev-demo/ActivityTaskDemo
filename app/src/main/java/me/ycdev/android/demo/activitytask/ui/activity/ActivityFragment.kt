package me.ycdev.android.demo.activitytask.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.ycdev.android.demo.activitytask.databinding.FragmentActivityBinding
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleInstance3Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTask3Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop1Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop2Activity
import me.ycdev.android.demo.activitytask.ui.demo.SingleTop3Activity
import me.ycdev.android.demo.activitytask.ui.demo.SpecialClearOnLaunchActivity
import me.ycdev.android.demo.activitytask.ui.demo.SpecialFinishOnLaunchActivity
import me.ycdev.android.demo.activitytask.ui.demo.SpecialReparentingActivity
import me.ycdev.android.demo.activitytask.ui.demo.Standard1Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard2Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard3Activity

class ActivityFragment : Fragment() {
    private lateinit var binding: FragmentActivityBinding
    private lateinit var activityViewModel: ActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        binding = FragmentActivityBinding.inflate(inflater)

        binding.standard1.setOnClickListener {
            startActivity(Standard1Activity::class.java)
        }
        binding.standard2.setOnClickListener {
            startActivity(Standard2Activity::class.java)
        }
        binding.standard3.setOnClickListener {
            startActivity(Standard3Activity::class.java)
        }

        binding.singleTop1.setOnClickListener {
            startActivity(SingleTop1Activity::class.java)
        }
        binding.singleTop2.setOnClickListener {
            startActivity(SingleTop2Activity::class.java)
        }
        binding.singleTop3.setOnClickListener {
            startActivity(SingleTop3Activity::class.java)
        }

        binding.singleTask1.setOnClickListener {
            startActivity(SingleTask1Activity::class.java)
        }
        binding.singleTask2.setOnClickListener {
            startActivity(SingleTask2Activity::class.java)
        }
        binding.singleTask3.setOnClickListener {
            startActivity(SingleTask3Activity::class.java)
        }

        binding.singleInstance1.setOnClickListener {
            startActivity(SingleInstance1Activity::class.java)
        }
        binding.singleInstance2.setOnClickListener {
            startActivity(SingleInstance2Activity::class.java)
        }
        binding.singleInstance3.setOnClickListener {
            startActivity(SingleInstance3Activity::class.java)
        }

        binding.reparenting.setOnClickListener {
            startActivity(SpecialReparentingActivity::class.java)
        }
        binding.finishOnLaunch.setOnClickListener {
            startActivity(SpecialFinishOnLaunchActivity::class.java)
        }
        binding.clearOnLaunch.setOnClickListener {
            startActivity(SpecialClearOnLaunchActivity::class.java)
        }

        return binding.root
    }

    private fun startActivity(cls: Class<*>) {
        val intent = Intent(requireActivity(), cls)
        var flags = 0
        if (binding.flagsNewTask.isChecked) {
            flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (binding.flagsSingleTop.isChecked) {
            flags = flags or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        if (binding.flagsClearTop.isChecked) {
            flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (binding.flagsClearTask.isChecked) {
            flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        if (flags != 0) {
            intent.flags = flags
        }
        startActivity(intent)
    }
}
