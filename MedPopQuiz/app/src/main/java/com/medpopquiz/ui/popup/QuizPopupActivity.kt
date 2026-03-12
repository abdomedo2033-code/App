package com.medpopquiz.ui.popup

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medpopquiz.MedPopQuizApp
import com.medpopquiz.R
import com.medpopquiz.data.entity.StudySessionEntity
import com.medpopquiz.data.entity.TermEntity
import com.medpopquiz.databinding.ActivityQuizPopupBinding
import com.medpopquiz.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class QuizPopupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QuizPopupActivity"
        private const val AUTO_DISMISS_DELAY_MS = 30000L // 30 seconds
    }

    private lateinit var binding: ActivityQuizPopupBinding
    private lateinit var prefs: PreferenceManager
    private var currentTerm: TermEntity? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isAnswerRevealed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup window for showing on lock screen
        setupWindowFlags()
        
        binding = ActivityQuizPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferenceManager(this)

        setupClickListeners()
        loadNextTerm()
        
        // Auto-dismiss after delay
        handler.postDelayed({ finish() }, AUTO_DISMISS_DELAY_MS)
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Make it appear above lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
    }

    private fun setupClickListeners() {
        // Show answer button
        binding.btnShowAnswer.setOnClickListener {
            revealAnswer()
        }

        // Easy button
        binding.btnEasy.setOnClickListener {
            handleResponse(isHard = false)
        }

        // Hard button
        binding.btnHard.setOnClickListener {
            handleResponse(isHard = true)
        }

        // Skip button
        binding.btnSkip.setOnClickListener {
            finish()
        }

        // Close button
        binding.btnClose.setOnClickListener {
            finish()
        }

        // Tap on card to reveal answer
        binding.cardQuestion.setOnClickListener {
            if (!isAnswerRevealed) {
                revealAnswer()
            }
        }
    }

    private fun loadNextTerm() {
        lifecycleScope.launch {
            try {
                val database = (application as MedPopQuizApp).database
                
                // Get next term based on preferences
                val term = if (prefs.isRandomOrder()) {
                    database.termDao().getRandomTerm()
                } else {
                    database.termDao().getNextTermForReview()
                }

                if (term != null) {
                    currentTerm = term
                    displayTerm(term)
                } else {
                    // No terms available
                    showNoTermsMessage()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading term", e)
                Toast.makeText(this@QuizPopupActivity, "Error loading quiz", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayTerm(term: TermEntity) {
        binding.tvQuestion.text = term.question
        binding.tvAnswer.text = term.answer
        
        // Show category if available
        if (term.categoryId != null) {
            lifecycleScope.launch {
                val category = (application as MedPopQuizApp).database
                    .categoryDao().getCategoryById(term.categoryId)
                category?.let {
                    binding.tvCategory.text = it.name
                    binding.tvCategory.visibility = View.VISIBLE
                }
            }
        } else {
            binding.tvCategory.visibility = View.GONE
        }

        // Reset answer visibility
        isAnswerRevealed = false
        binding.tvAnswer.visibility = View.GONE
        binding.btnShowAnswer.visibility = View.VISIBLE
        binding.layoutButtons.visibility = View.GONE

        // Animate card entry
        animateCardEntry()
    }

    private fun revealAnswer() {
        isAnswerRevealed = true
        binding.tvAnswer.visibility = View.VISIBLE
        binding.btnShowAnswer.visibility = View.GONE
        binding.layoutButtons.visibility = View.VISIBLE

        // Animate answer reveal
        ObjectAnimator.ofFloat(binding.tvAnswer, View.ALPHA, 0f, 1f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        // Animate buttons
        ObjectAnimator.ofFloat(binding.layoutButtons, View.TRANSLATION_Y, 100f, 0f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun handleResponse(isHard: Boolean) {
        val term = currentTerm ?: return

        lifecycleScope.launch {
            try {
                val database = (application as MedPopQuizApp).database
                
                // Calculate next interval
                val nextInterval = term.calculateNextInterval(isHard)
                
                // Calculate next review date
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, nextInterval)
                val nextReviewDate = calendar.time

                // Update term
                if (isHard) {
                    database.termDao().markAsHard(term.id, nextInterval, nextReviewDate)
                } else {
                    database.termDao().markAsEasy(term.id, nextInterval, nextReviewDate)
                }

                // Record study session
                val session = StudySessionEntity(
                    termId = term.id,
                    isHard = isHard,
                    wasShownOnUnlock = true
                )
                database.studySessionDao().insert(session)

                // Show feedback
                val message = if (isHard) {
                    "Marked as Hard - Will review sooner"
                } else {
                    "Marked as Easy - Next review in $nextInterval days"
                }
                Toast.makeText(this@QuizPopupActivity, message, Toast.LENGTH_SHORT).show()

                // Finish after short delay
                handler.postDelayed({ finish() }, 500)

            } catch (e: Exception) {
                Log.e(TAG, "Error saving response", e)
                finish()
            }
        }
    }

    private fun showNoTermsMessage() {
        binding.tvQuestion.text = getString(R.string.no_terms_available)
        binding.tvAnswer.text = getString(R.string.add_terms_message)
        binding.btnShowAnswer.visibility = View.GONE
        binding.layoutButtons.visibility = View.GONE
        binding.tvCategory.visibility = View.GONE
    }

    private fun animateCardEntry() {
        ObjectAnimator.ofFloat(binding.cardQuestion, View.TRANSLATION_Y, -200f, 0f).apply {
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(binding.cardQuestion, View.ALPHA, 0f, 1f).apply {
            duration = 300
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        // Allow back press to dismiss
        finish()
    }
}
