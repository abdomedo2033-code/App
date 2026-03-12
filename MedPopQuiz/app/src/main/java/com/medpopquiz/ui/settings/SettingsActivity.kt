package com.medpopquiz.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medpopquiz.MedPopQuizApp
import com.medpopquiz.databinding.ActivitySettingsBinding
import com.medpopquiz.service.PopupMonitorService
import com.medpopquiz.utils.PreferenceManager
import com.medpopquiz.worker.DailyReviewWorker
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferenceManager(this)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }

    private fun loadSettings() {
        // Popup on unlock
        binding.switchPopupUnlock.isChecked = prefs.isPopupOnUnlockEnabled()
        
        // Random order
        binding.switchRandomOrder.isChecked = prefs.isRandomOrder()
        
        // Daily review
        binding.switchDailyReview.isChecked = prefs.isDailyReviewEnabled()
        binding.layoutDailyReviewSettings.visibility = if (prefs.isDailyReviewEnabled()) 
            android.view.View.VISIBLE else android.view.View.GONE
        
        // Daily review count
        binding.sliderDailyCount.value = prefs.getDailyReviewCount().toFloat()
        binding.tvDailyCount.text = prefs.getDailyReviewCount().toString()
        
        // Daily review time
        val hour = prefs.getDailyReviewHour()
        val minute = prefs.getDailyReviewMinute()
        binding.timePickerDailyReview.hour = hour
        binding.timePickerDailyReview.minute = minute
        
        // Min interval
        binding.sliderMinInterval.value = prefs.getMinIntervalSeconds().toFloat()
        binding.tvMinInterval.text = "${prefs.getMinIntervalSeconds()}s"
    }

    private fun setupListeners() {
        // Popup on unlock toggle
        binding.switchPopupUnlock.setOnCheckedChangeListener { _, isChecked ->
            prefs.setPopupOnUnlockEnabled(isChecked)
            if (isChecked) {
                startPopupService()
            } else {
                stopPopupService()
            }
        }

        // Random order toggle
        binding.switchRandomOrder.setOnCheckedChangeListener { _, isChecked ->
            prefs.setRandomOrder(isChecked)
        }

        // Daily review toggle
        binding.switchDailyReview.setOnCheckedChangeListener { _, isChecked ->
            prefs.setDailyReviewEnabled(isChecked)
            binding.layoutDailyReviewSettings.visibility = if (isChecked) 
                android.view.View.VISIBLE else android.view.View.GONE
            
            if (isChecked) {
                DailyReviewWorker.schedule(this)
                Toast.makeText(this, "Daily review scheduled", Toast.LENGTH_SHORT).show()
            } else {
                DailyReviewWorker.cancel(this)
            }
        }

        // Daily review count slider
        binding.sliderDailyCount.addOnChangeListener { _, value, _ ->
            val count = value.toInt()
            prefs.setDailyReviewCount(count)
            binding.tvDailyCount.text = count.toString()
        }

        // Time picker
        binding.timePickerDailyReview.setOnTimeChangedListener { _, hour, minute ->
            prefs.setDailyReviewHour(hour)
            prefs.setDailyReviewMinute(minute)
            if (prefs.isDailyReviewEnabled()) {
                DailyReviewWorker.schedule(this)
            }
        }

        // Min interval slider
        binding.sliderMinInterval.addOnChangeListener { _, value, _ ->
            val seconds = value.toInt()
            prefs.setMinIntervalSeconds(seconds)
            binding.tvMinInterval.text = "${seconds}s"
        }

        // Reset progress button
        binding.btnResetProgress.setOnClickListener {
            showResetProgressDialog()
        }

        // Clear all data button
        binding.btnClearAllData.setOnClickListener {
            showClearAllDataDialog()
        }

        // Request overlay permission
        binding.btnRequestOverlay.setOnClickListener {
            requestOverlayPermission()
        }
    }

    private fun startPopupService() {
        val intent = Intent(this, PopupMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopPopupService() {
        stopService(Intent(this, PopupMonitorService::class.java))
    }

    private fun showResetProgressDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Progress")
            .setMessage("This will reset all study progress (Easy/Hard counts, review dates). Your terms will remain. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                lifecycleScope.launch {
                    val database = (application as MedPopQuizApp).database
                    val terms = database.termDao().getAllActiveTerms()
                    terms.collectLatest { termList ->
                        termList.forEach { term ->
                            val resetTerm = term.copy(
                                easyCount = 0,
                                hardCount = 0,
                                reviewCount = 0,
                                intervalDays = 1,
                                nextReviewDate = java.util.Date()
                            )
                            database.termDao().update(resetTerm)
                        }
                    }
                    Toast.makeText(this@SettingsActivity, "Progress reset", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("This will delete ALL terms, categories, and progress. This cannot be undone!")
            .setPositiveButton("Delete Everything") { _, _ ->
                lifecycleScope.launch {
                    val database = (application as MedPopQuizApp).database
                    // Clear all data
                    database.termDao().getAllActiveTerms().collectLatest { terms ->
                        terms.forEach { database.termDao().delete(it) }
                    }
                    prefs.resetAll()
                    Toast.makeText(this@SettingsActivity, "All data cleared", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
