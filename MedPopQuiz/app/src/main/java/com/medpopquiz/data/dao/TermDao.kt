package com.medpopquiz.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.medpopquiz.data.entity.TermEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TermDao {

    @Query("SELECT * FROM terms WHERE isActive = 1 ORDER BY nextReviewDate ASC")
    fun getAllActiveTerms(): Flow<List<TermEntity>>

    @Query("SELECT * FROM terms WHERE isActive = 1 AND nextReviewDate <= :currentDate ORDER BY nextReviewDate ASC, RANDOM()")
    suspend fun getTermsForReview(currentDate: Date = Date()): List<TermEntity>

    @Query("SELECT * FROM terms WHERE isActive = 1 AND nextReviewDate <= :currentDate ORDER BY nextReviewDate ASC LIMIT 1")
    suspend fun getNextTermForReview(currentDate: Date = Date()): TermEntity?

    @Query("SELECT * FROM terms WHERE id = :termId")
    suspend fun getTermById(termId: Long): TermEntity?

    @Query("SELECT * FROM terms WHERE categoryId = :categoryId AND isActive = 1")
    fun getTermsByCategory(categoryId: Long): Flow<List<TermEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(term: TermEntity): Long

    @Update
    suspend fun update(term: TermEntity)

    @Delete
    suspend fun delete(term: TermEntity)

    @Query("DELETE FROM terms WHERE id = :termId")
    suspend fun deleteById(termId: Long)

    @Query("SELECT COUNT(*) FROM terms WHERE isActive = 1")
    suspend fun getTotalTermCount(): Int

    @Query("SELECT COUNT(*) FROM terms WHERE isActive = 1 AND nextReviewDate <= :currentDate")
    suspend fun getDueTermCount(currentDate: Date = Date()): Int

    @Query("SELECT COUNT(*) FROM terms WHERE hardCount > easyCount AND isActive = 1")
    suspend fun getHardTermsCount(): Int

    @Query("SELECT COUNT(*) FROM terms WHERE easyCount > hardCount AND isActive = 1")
    suspend fun getEasyTermsCount(): Int

    @Query("SELECT * FROM terms WHERE isActive = 1 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomTerm(): TermEntity?

    @Query("SELECT * FROM terms WHERE isActive = 1 ORDER BY id ASC LIMIT 1 OFFSET :offset")
    suspend fun getTermBySequence(offset: Int): TermEntity?

    @Query("UPDATE terms SET nextReviewDate = :date WHERE id = :termId")
    suspend fun updateNextReviewDate(termId: Long, date: Date)

    @Query("UPDATE terms SET hardCount = hardCount + 1, reviewCount = reviewCount + 1, intervalDays = :interval, nextReviewDate = :nextDate WHERE id = :termId")
    suspend fun markAsHard(termId: Long, interval: Int, nextDate: Date)

    @Query("UPDATE terms SET easyCount = easyCount + 1, reviewCount = reviewCount + 1, intervalDays = :interval, nextReviewDate = :nextDate WHERE id = :termId")
    suspend fun markAsEasy(termId: Long, interval: Int, nextDate: Date)

    @Query("SELECT * FROM terms WHERE question LIKE '%' || :query || '%' OR answer LIKE '%' || :query || '%'")
    suspend fun searchTerms(query: String): List<TermEntity>
}
