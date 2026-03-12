package com.medpopquiz.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TermEntity::class,
            parentColumns = ["id"],
            childColumns = ["termId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("termId"), Index("sessionDate")]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val termId: Long,
    val isHard: Boolean, // true = Hard, false = Easy
    val sessionDate: Date = Date(),
    val wasShownOnUnlock: Boolean = true
)
