package com.medpopquiz.ui.terms

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medpopquiz.MedPopQuizApp
import com.medpopquiz.R
import com.medpopquiz.data.entity.TermEntity
import com.medpopquiz.databinding.ActivityTermListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TermListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermListBinding
    private lateinit var adapter: TermAdapter
    private var allTerms: List<TermEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeTerms()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.manage_terms)
    }

    private fun setupRecyclerView() {
        adapter = TermAdapter(
            onEditClick = { term -> editTerm(term) },
            onDeleteClick = { term -> deleteTerm(term) },
            onItemClick = { term -> viewTerm(term) }
        )
        
        binding.recyclerViewTerms.apply {
            layoutManager = LinearLayoutManager(this@TermListActivity)
            adapter = this@TermListActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTerm.setOnClickListener {
            startActivity(Intent(this, AddEditTermActivity::class.java))
        }
    }

    private fun observeTerms() {
        lifecycleScope.launch {
            val database = (application as MedPopQuizApp).database
            database.termDao().getAllActiveTerms().collectLatest { terms ->
                allTerms = terms
                adapter.submitList(terms)
                updateEmptyState(terms.isEmpty())
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewTerms.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun editTerm(term: TermEntity) {
        val intent = Intent(this, AddEditTermActivity::class.java).apply {
            putExtra(AddEditTermActivity.EXTRA_TERM_ID, term.id)
        }
        startActivity(intent)
    }

    private fun deleteTerm(term: TermEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Term")
            .setMessage("Are you sure you want to delete this term?\n\n${term.question}")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val database = (application as MedPopQuizApp).database
                    database.termDao().deleteById(term.id)
                    Toast.makeText(this@TermListActivity, "Term deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun viewTerm(term: TermEntity) {
        AlertDialog.Builder(this)
            .setTitle("Term Details")
            .setMessage("Q: ${term.question}\n\nA: ${term.answer}\n\nDifficulty: ${getDifficultyText(term.difficulty)}")
            .setPositiveButton("Edit") { _, _ -> editTerm(term) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun getDifficultyText(difficulty: Int): String {
        return when (difficulty) {
            1 -> "Easy"
            2 -> "Medium"
            3 -> "Hard"
            else -> "Unknown"
        }
    }

    private fun searchTerms(query: String) {
        val filteredTerms = if (query.isEmpty()) {
            allTerms
        } else {
            allTerms.filter {
                it.question.contains(query, ignoreCase = true) ||
                it.answer.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filteredTerms)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_term_list, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchTerms(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchTerms(it) }
                return true
            }
        })
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_delete_all -> {
                showDeleteAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Terms")
            .setMessage("Are you sure you want to delete ALL terms? This cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                lifecycleScope.launch {
                    allTerms.forEach { term ->
                        (application as MedPopQuizApp).database.termDao().deleteById(term.id)
                    }
                    Toast.makeText(this@TermListActivity, "All terms deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
