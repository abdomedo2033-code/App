package com.medpopquiz.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String = "#4CAF50", // Default green color
    val icon: String = "default",
    val isActive: Boolean = true
)
