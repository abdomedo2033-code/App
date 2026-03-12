package com.medpopquiz.data.dao

import androidx.room.*
import com.medpopquiz.data.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.Calendar

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY sessionDate DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE termId = :termId ORDER BY sessionDate DESC")
    fun getSessionsForTerm(termId: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE sessionDate >= :startDate AND sessionDate <= :endDate")
    suspend fun getSessionCountInRange(startDate: Date, endDate: Date): Int

    @Query("SELECT COUNT(*) FROM study_sessions WHERE isHard = 1 AND sessionDate >= :startDate AND sessionDate <= :endDate")
    suspend fun getHardCountInRange(startDate: Date, endDate: Date): Int

    @Query("SELECT COUNT(*) FROM study_sessions WHERE isHard = 0 AND sessionDate >= :startDate AND sessionDate <= :endDate")
    suspend fun getEasyCountInRange(startDate: Date, endDate: Date): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySessionEntity): Long

    @Query("SELECT COUNT(*) FROM study_sessions WHERE date(sessionDate/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getSessionsForDate(date: Date): Int

    @Query("SELECT COUNT(*) FROM study_sessions WHERE sessionDate >= :startOfDay AND sessionDate < :endOfDay")
    suspend fun getTodaySessions(startOfDay: Date, endOfDay: Date): Int

    @Query("SELECT COUNT(DISTINCT termId) FROM study_sessions WHERE sessionDate >= :startOfDay AND sessionDate < :endOfDay")
    suspend fun getUniqueTermsStudiedToday(startOfDay: Date, endOfDay: Date): Int

    @Query("DELETE FROM study_sessions WHERE sessionDate < :date")
    suspend fun deleteOldSessions(date: Date)

    companion object {
        fun getStartOfDay(): Date {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.time
        }

        fun getEndOfDay(): Date {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar.time
        }
    }
}
