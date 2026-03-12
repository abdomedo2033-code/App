package com.medpopquiz.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.medpopquiz.MedPopQuizApp
import com.medpopquiz.R
import com.medpopquiz.databinding.ActivityMainBinding
import com.medpopquiz.receiver.BootReceiver
import com.medpopquiz.service.PopupMonitorService
import com.medpopquiz.ui.categories.CategoryListActivity
import com.medpopquiz.ui.popup.QuizPopupActivity
import com.medpopquiz.ui.settings.SettingsActivity
import com.medpopquiz.ui.stats.StatisticsActivity
import com.medpopquiz.ui.terms.TermListActivity
import com.medpopquiz.utils.PreferenceManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferenceManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startPopupService()
        } else {
            Toast.makeText(this, "Notification permission needed for popups", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferenceManager(this)

        setupToolbar()
        setupClickListeners()
        observeData()
        checkFirstLaunch()
    }

    override fun onResume() {
        super.onResume()
        updateStats()
        updateToggleState()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupClickListeners() {
        // Toggle popup on unlock
        binding.switchPopupUnlock.setOnCheckedChangeListener { _, isChecked ->
            prefs.setPopupOnUnlockEnabled(isChecked)
            if (isChecked) {
                checkAndRequestPermissions()
            } else {
                stopPopupService()
            }
        }

        // Quick practice button
        binding.btnQuickPractice.setOnClickListener {
            startActivity(Intent(this, QuizPopupActivity::class.java))
        }

        // Manage terms button
        binding.cardManageTerms.setOnClickListener {
            startActivity(Intent(this, TermListActivity::class.java))
        }

        // Categories button
        binding.cardCategories.setOnClickListener {
            startActivity(Intent(this, CategoryListActivity::class.java))
        }

        // Statistics button
        binding.cardStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        // Settings button
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Test popup button
        binding.btnTestPopup.setOnClickListener {
            startActivity(Intent(this, QuizPopupActivity::class.java))
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            
            // Observe total terms
            database.termDao().getAllActiveTerms().collectLatest { terms ->
                binding.tvTotalTerms.text = terms.size.toString()
            }
        }

        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            
            // Observe due terms
            database.termDao().getTermsForReview().let { dueTerms ->
                binding.tvDueTerms.text = dueTerms.size.toString()
            }
        }

        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            
            // Observe categories
            database.categoryDao().getActiveCategories().collectLatest { categories ->
                binding.tvCategories.text = categories.size.toString()
            }
        }
    }

    private fun updateStats() {
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            
            // Update due count
            val dueCount = database.termDao().getDueTermCount()
            binding.tvDueTerms.text = dueCount.toString()

            // Update hard terms count
            val hardCount = database.termDao().getHardTermsCount()
            binding.tvHardTerms.text = hardCount.toString()
        }
    }

    private fun updateToggleState() {
        binding.switchPopupUnlock.isChecked = prefs.isPopupOnUnlockEnabled()
    }

    private fun checkFirstLaunch() {
        if (prefs.isFirstLaunch()) {
            showWelcomeDialog()
            prefs.setFirstLaunchComplete()
        }
    }

    private fun showWelcomeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Welcome to MedPop Quiz!")
            .setMessage("This app will help you study medical terms by showing quiz popups when you unlock your phone.\n\nKey features:\n• Popup on unlock\n• Easy/Hard spaced repetition\n• Track your progress\n• Organize by categories")
            .setPositiveButton("Get Started") { _, _ ->
                checkAndRequestPermissions()
            }
            .setCancelable(false)
            .show()
    }

    private fun checkAndRequestPermissions() {
        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == 
                    PackageManager.PERMISSION_GRANTED -> {
                    startPopupService()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showNotificationPermissionRationale()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startPopupService()
        }

        // Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            }
        }
    }

    private fun showNotificationPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission")
            .setMessage("MedPop Quiz needs notification permission to show quiz popups when you unlock your phone.")
            .setPositiveButton("Grant") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder(this)
                .setTitle("Overlay Permission")
                .setMessage("To show quiz popups on unlock, please allow MedPop Quiz to display over other apps.")
                .setPositiveButton("Settings") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun startPopupService() {
        val intent = Intent(this, PopupMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Quiz popups enabled!", Toast.LENGTH_SHORT).show()
    }

    private fun stopPopupService() {
        stopService(Intent(this, PopupMonitorService::class.java))
        Toast.makeText(this, "Quiz popups disabled", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About MedPop Quiz")
            .setMessage("Version 1.0\n\nA medical quiz app that helps you study by showing popups when you unlock your phone.\n\nUses spaced repetition to optimize your learning.")
            .setPositiveButton("OK", null)
            .show()
    }
}
