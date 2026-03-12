package com.medpopquiz.ui.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medpopquiz.data.entity.CategoryEntity
import com.medpopquiz.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onEditClick: (CategoryEntity) -> Unit,
    private val onDeleteClick: (CategoryEntity) -> Unit,
    private val onItemClick: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryEntity) {
            binding.apply {
                tvCategoryName.text = category.name
                
                // Set category color
                try {
                    cardCategory.setCardBackgroundColor(Color.parseColor(category.color))
                } catch (e: IllegalArgumentException) {
                    cardCategory.setCardBackgroundColor(Color.GRAY)
                }

                // Show/hide inactive overlay
                viewInactiveOverlay.visibility = if (category.isActive) View.GONE else View.VISIBLE

                // Click listeners
                btnEdit.setOnClickListener { onEditClick(category) }
                btnDelete.setOnClickListener { onDeleteClick(category) }
                root.setOnClickListener { onItemClick(category) }
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryEntity>() {
        override fun areItemsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
