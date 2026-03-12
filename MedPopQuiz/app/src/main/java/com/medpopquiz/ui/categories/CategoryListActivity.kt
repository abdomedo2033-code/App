package com.medpopquiz.ui.categories

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.medpopquiz.MedPopQuizApp
import com.medpopquiz.R
import com.medpopquiz.data.entity.CategoryEntity
import com.medpopquiz.databinding.ActivityCategoryListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryListBinding
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeCategories()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.categories)
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onEditClick = { category -> editCategory(category) },
            onDeleteClick = { category -> deleteCategory(category) },
            onItemClick = { category -> toggleCategory(category) }
        )
        
        binding.recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(this@CategoryListActivity, 2)
            adapter = this@CategoryListActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            database.categoryDao().getAllCategories().collectLatest { categories ->
                adapter.submitList(categories)
                updateEmptyState(categories.isEmpty())
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewCategories.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        
        AlertDialog.Builder(this)
            .setTitle("Add Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    addCategory(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addCategory(name: String) {
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            val colors = listOf(
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
                "#FFEAA7", "#DDA0DD", "#98D8C8", "#F7DC6F"
            )
            val randomColor = colors.random()
            
            val category = CategoryEntity(
                name = name,
                color = randomColor,
                icon = "default"
            )
            database.categoryDao().insert(category)
            Toast.makeText(this@CategoryListActivity, "Category added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editCategory(category: CategoryEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        etName.setText(category.name)
        
        AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        val updatedCategory = category.copy(name = name)
                        (application as MedPopQuizApp).database.categoryDao().update(updatedCategory)
                        Toast.makeText(this@CategoryListActivity, "Category updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCategory(category: CategoryEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete \"${category.name}\"? Terms in this category will become uncategorized.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    (application as MedPopQuizApp).database.categoryDao().delete(category)
                    Toast.makeText(this@CategoryListActivity, "Category deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleCategory(category: CategoryEntity) {
        lifecycleScope.launch {
            val updatedCategory = category.copy(isActive = !category.isActive)
            (application as MedPopQuizApp).database.categoryDao().update(updatedCategory)
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
