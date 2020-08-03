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
import me.ycdev.android.demo.activitytask.ui.demo.Standard1Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard2Activity
import me.ycdev.android.demo.activitytask.ui.demo.Standard3Activity

class ActivityFragment : Fragment() {

    private lateinit var activityViewModel: ActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        val binding = FragmentActivityBinding.inflate(inflater)

        binding.standard1.setOnClickListener {
            val intent = Intent(requireActivity(), Standard1Activity::class.java)
            startActivity(intent)
        }
        binding.standard2.setOnClickListener {
            val intent = Intent(requireActivity(), Standard2Activity::class.java)
            startActivity(intent)
        }
        binding.standard3.setOnClickListener {
            val intent = Intent(requireActivity(), Standard3Activity::class.java)
            startActivity(intent)
        }

        binding.singleTop1.setOnClickListener {
            val intent = Intent(requireActivity(), SingleTop1Activity::class.java)
            startActivity(intent)
        }
        binding.singleTop2.setOnClickListener {
            val intent = Intent(requireActivity(), SingleTop2Activity::class.java)
            startActivity(intent)
        }
        binding.singleTop3.setOnClickListener {
            val intent = Intent(requireActivity(), SingleTop3Activity::class.java)
            startActivity(intent)
        }

        binding.singleTask1.setOnClickListener {
            val intent = Intent(requireActivity(), SingleTask1Activity::class.java)
            startActivity(intent)
        }
        binding.singleTask2.setOnClickListener {
            val intent = Intent(requireActivity(), SingleTask2Activity::class.java)
            startActivity(intent)
        }
        binding.singleTask3.setOnClickListener {
            val intent = Intent(requireActivity(), SingleTask3Activity::class.java)
            startActivity(intent)
        }

        binding.singleInstance1.setOnClickListener {
            val intent = Intent(requireActivity(), SingleInstance1Activity::class.java)
            startActivity(intent)
        }
        binding.singleInstance2.setOnClickListener {
            val intent = Intent(requireActivity(), SingleInstance2Activity::class.java)
            startActivity(intent)
        }
        binding.singleInstance3.setOnClickListener {
            val intent = Intent(requireActivity(), SingleInstance3Activity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}
