package com.medpopquiz.ui.terms

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medpopquiz.MedPopQuizApp
import com.medpopquiz.R
import com.medpopquiz.data.entity.CategoryEntity
import com.medpopquiz.data.entity.TermEntity
import com.medpopquiz.databinding.ActivityAddEditTermBinding
import kotlinx.coroutines.launch
import java.util.Date

class AddEditTermActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TERM_ID = "extra_term_id"
    }

    private lateinit var binding: ActivityAddEditTermBinding
    private var termId: Long = -1
    private var isEditMode = false
    private var categories: List<CategoryEntity> = emptyList()
    private var selectedCategoryId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTermBinding.inflate(layoutInflater)
        setContentView(binding.root)

        termId = intent.getLongExtra(EXTRA_TERM_ID, -1)
        isEditMode = termId != -1L

        setupToolbar()
        setupCategorySpinner()
        setupClickListeners()
        
        if (isEditMode) {
            loadTerm()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditMode) getString(R.string.edit_term) else getString(R.string.add_term)
    }

    private fun setupCategorySpinner() {
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            database.categoryDao().getActiveCategories().collectLatest { cats ->
                categories = cats
                val categoryNames = cats.map { it.name }.toMutableList()
                categoryNames.add(0, "No Category")
                
                val adapter = ArrayAdapter(
                    this@AddEditTermActivity,
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter
                
                binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedCategoryId = if (position == 0) null else categories[position - 1].id
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedCategoryId = null
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveTerm()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadTerm() {
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            val term = database.termDao().getTermById(termId)
            
            term?.let {
                binding.etQuestion.setText(it.question)
                binding.etAnswer.setText(it.answer)
                
                // Set difficulty
                when (it.difficulty) {
                    1 -> binding.radioEasy.isChecked = true
                    2 -> binding.radioMedium.isChecked = true
                    3 -> binding.radioHard.isChecked = true
                }
                
                // Set category
                it.categoryId?.let { catId ->
                    val position = categories.indexOfFirst { c -> c.id == catId }
                    if (position >= 0) {
                        binding.spinnerCategory.setSelection(position + 1)
                    }
                }
            }
        }
    }

    private fun saveTerm() {
        val question = binding.etQuestion.text.toString().trim()
        val answer = binding.etAnswer.text.toString().trim()
        
        if (question.isEmpty()) {
            binding.etQuestion.error = "Question is required"
            return
        }
        
        if (answer.isEmpty()) {
            binding.etAnswer.error = "Answer is required"
            return
        }
        
        val difficulty = when (binding.radioGroupDifficulty.checkedRadioButtonId) {
            R.id.radioEasy -> 1
            R.id.radioHard -> 3
            else -> 2 // Medium
        }
        
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            
            val term = if (isEditMode) {
                // Update existing term
                val existingTerm = database.termDao().getTermById(termId)
                existingTerm?.copy(
                    question = question,
                    answer = answer,
                    categoryId = selectedCategoryId,
                    difficulty = difficulty
                )
            } else {
                // Create new term
                TermEntity(
                    question = question,
                    answer = answer,
                    categoryId = selectedCategoryId,
                    difficulty = difficulty,
                    nextReviewDate = Date()
                )
            }
            
            term?.let {
                database.termDao().insert(it)
                val message = if (isEditMode) "Term updated" else "Term added"
                Toast.makeText(this@AddEditTermActivity, message, Toast.LENGTH_SHORT).show()
                finish()
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
