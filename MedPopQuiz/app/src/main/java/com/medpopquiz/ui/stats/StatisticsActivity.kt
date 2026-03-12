package com.medpopquiz.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.medpopquiz.MedPopQuizApp
import com.medpopquiz.data.dao.StudySessionDao
import com.medpopquiz.databinding.ActivityStatisticsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadStatistics()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Statistics"
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            
            // Load overall stats
            val totalTerms = database.termDao().getTotalTermCount()
            val dueTerms = database.termDao().getDueTermCount()
            val hardTerms = database.termDao().getHardTermsCount()
            val easyTerms = database.termDao().getEasyTermsCount()
            
            binding.tvTotalTerms.text = totalTerms.toString()
            binding.tvDueTerms.text = dueTerms.toString()
            binding.tvHardTerms.text = hardTerms.toString()
            binding.tvEasyTerms.text = easyTerms.toString()
            
            // Calculate mastery percentage
            val masteryPercent = if (totalTerms > 0) {
                (easyTerms * 100) / totalTerms
            } else 0
            binding.tvMastery.text = "$masteryPercent%"
            binding.progressMastery.progress = masteryPercent
            
            // Load today's stats
            val todayStart = StudySessionDao.getStartOfDay()
            val todayEnd = StudySessionDao.getEndOfDay()
            val todaySessions = database.studySessionDao().getTodaySessions(todayStart, todayEnd)
            val uniqueTermsToday = database.studySessionDao().getUniqueTermsStudiedToday(todayStart, todayEnd)
            
            binding.tvTodaySessions.text = todaySessions.toString()
            binding.tvUniqueTerms.text = uniqueTermsToday.toString()
            
            // Load weekly stats
            loadWeeklyStats(database)
            
            // Load category distribution
            loadCategoryDistribution(database)
        }
    }

    private suspend fun loadWeeklyStats(database: com.medpopquiz.data.AppDatabase) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        
        val labels = mutableListOf<String>()
        val easyData = mutableListOf<BarEntry>()
        val hardData = mutableListOf<BarEntry>()
        
        for (i in 6 downTo 0) {
            val dayCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            
            val startOfDay = dayCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            
            val endOfDay = dayCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            
            val easyCount = database.studySessionDao().getEasyCountInRange(startOfDay, endOfDay)
            val hardCount = database.studySessionDao().getHardCountInRange(startOfDay, endOfDay)
            
            labels.add(dateFormat.format(dayCalendar.time))
            easyData.add(BarEntry((6 - i).toFloat(), easyCount.toFloat()))
            hardData.add(BarEntry((6 - i).toFloat(), hardCount.toFloat()))
        }
        
        // Create bar dataset
        val easySet = BarDataSet(easyData, "Easy").apply {
            color = Color.parseColor("#4CAF50")
            valueTextSize = 10f
        }
        
        val hardSet = BarDataSet(hardData, "Hard").apply {
            color = Color.parseColor("#F44336")
            valueTextSize = 10f
        }
        
        val barData = BarData(easySet, hardSet).apply {
            barWidth = 0.35f
        }
        
        binding.chartWeekly.apply {
            data = barData
            description.isEnabled = false
            legend.isEnabled = true
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.apply {
                granularity = 1f
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false
            groupBars(0f, 0.2f, 0.05f)
            invalidate()
        }
    }

    private suspend fun loadCategoryDistribution(database: com.medpopquiz.data.AppDatabase) {
        val categories = database.categoryDao().getActiveCategories()
        val categoryData = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        
        categories.collectLatest { cats ->
            cats.forEach { category ->
                val count = database.termDao().getTermsByCategory(category.id).collectLatest { terms ->
                    if (terms.isNotEmpty()) {
                        categoryData.add(PieEntry(terms.size.toFloat(), category.name))
                        colors.add(Color.parseColor(category.color))
                    }
                }
            }
            
            if (categoryData.isNotEmpty()) {
                val pieDataSet = PieDataSet(categoryData, "Categories").apply {
                    this.colors = colors
                    valueTextSize = 12f
                    valueTextColor = Color.WHITE
                }
                
                binding.chartCategories.apply {
                    data = PieData(pieDataSet)
                    description.isEnabled = false
                    legend.isEnabled = true
                    centerText = "Terms by Category"
                    setEntryLabelColor(Color.WHITE)
                    invalidate()
                }
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
