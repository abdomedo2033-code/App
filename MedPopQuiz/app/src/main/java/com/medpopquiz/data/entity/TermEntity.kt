package com.medpopquiz.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "terms",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class TermEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val answer: String,
    val categoryId: Long? = null,
    val difficulty: Int = 1, // 1 = Easy, 2 = Medium, 3 = Hard
    val intervalDays: Int = 1, // Spaced repetition interval
    val nextReviewDate: Date = Date(),
    val reviewCount: Int = 0,
    val easyCount: Int = 0,
    val hardCount: Int = 0,
    val createdAt: Date = Date(),
    val isActive: Boolean = true,
    val isRandom: Boolean = true // For display order preference
) {
    fun calculateNextInterval(isHard: Boolean): Int {
        return if (isHard) {
            // Hard: review sooner - reset or decrease interval
            maxOf(1, intervalDays / 2)
        } else {
            // Easy: increase interval using spaced repetition
            when (intervalDays) {
                1 -> 3
                3 -> 7
                7 -> 14
                14 -> 30
                30 -> 60
                60 -> 90
                else -> minOf(intervalDays + 30, 365)
            }
        }
    }
}
