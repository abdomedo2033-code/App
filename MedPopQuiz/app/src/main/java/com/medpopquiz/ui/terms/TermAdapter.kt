package com.medpopquiz.ui.terms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medpopquiz.data.entity.TermEntity
import com.medpopquiz.databinding.ItemTermBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TermAdapter(
    private val onEditClick: (TermEntity) -> Unit,
    private val onDeleteClick: (TermEntity) -> Unit,
    private val onItemClick: (TermEntity) -> Unit
) : ListAdapter<TermEntity, TermAdapter.TermViewHolder>(TermDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermViewHolder {
        val binding = ItemTermBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TermViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TermViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TermViewHolder(
        private val binding: ItemTermBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(term: TermEntity) {
            binding.apply {
                tvQuestion.text = term.question
                tvAnswer.text = term.answer
                
                // Show review stats
                tvReviewStats.text = "Easy: ${term.easyCount} | Hard: ${term.hardCount}"
                
                // Show difficulty indicator
                val difficultyColor = when (term.difficulty) {
                    1 -> android.graphics.Color.parseColor("#4CAF50") // Green
                    2 -> android.graphics.Color.parseColor("#FF9800") // Orange
                    3 -> android.graphics.Color.parseColor("#F44336") // Red
                    else -> android.graphics.Color.GRAY
                }
                viewDifficulty.setBackgroundColor(difficultyColor)

                // Show next review date
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                tvNextReview.text = "Next: ${dateFormat.format(term.nextReviewDate)}"

                // Click listeners
                btnEdit.setOnClickListener { onEditClick(term) }
                btnDelete.setOnClickListener { onDeleteClick(term) }
                root.setOnClickListener { onItemClick(term) }
            }
        }
    }

    class TermDiffCallback : DiffUtil.ItemCallback<TermEntity>() {
        override fun areItemsTheSame(oldItem: TermEntity, newItem: TermEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TermEntity, newItem: TermEntity): Boolean {
            return oldItem == newItem
        }
    }
}
