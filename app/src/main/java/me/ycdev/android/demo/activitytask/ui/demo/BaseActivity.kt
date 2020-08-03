package me.ycdev.android.demo.activitytask.ui.demo

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import me.ycdev.android.demo.activitytask.Utils
import me.ycdev.android.demo.activitytask.databinding.ActivityDemoBinding

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDemoBinding
    private lateinit var who: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        who = getString(getWhoResId())
        updateContent("onCreate")

        binding.content.setOnLongClickListener {
            copyToClipboard()
            return@setOnLongClickListener true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        updateContent("onNewIntent")
    }

    @SuppressLint("SetTextI18n")
    private fun updateContent(from: String) {
        binding.content.text = "$who\n$from at ${Utils.getTimeStamp()}"
    }

    private fun copyToClipboard() {
        val clipboard = getSystemService(ClipboardManager::class.java) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("content", binding.content.text)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(binding.root, "Copied to clipboard", Snackbar.LENGTH_SHORT).show()
    }

    @StringRes
    protected abstract fun getWhoResId(): Int
}
