package com.medpopquiz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medpopquiz.data.dao.CategoryDao
import com.medpopquiz.data.dao.TermDao
import com.medpopquiz.data.dao.StudySessionDao
import com.medpopquiz.data.entity.CategoryEntity
import com.medpopquiz.data.entity.TermEntity
import com.medpopquiz.data.entity.StudySessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TermEntity::class, CategoryEntity::class, StudySessionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun termDao(): TermDao
    abstract fun categoryDao(): CategoryDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medpop_quiz_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultCategories(database.categoryDao())
                    populateDefaultTerms(database.termDao())
                }
            }
        }

        private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Labs", color = "#FF6B6B", icon = "lab"),
                CategoryEntity(name = "Anatomy", color = "#4ECDC4", icon = "body"),
                CategoryEntity(name = "Drugs", color = "#45B7D1", icon = "pill"),
                CategoryEntity(name = "Numbers", color = "#96CEB4", icon = "numbers"),
                CategoryEntity(name = "Pathology", color = "#FFEAA7", icon = "disease"),
                CategoryEntity(name = "Procedures", color = "#DDA0DD", icon = "procedure")
            )
            defaultCategories.forEach { categoryDao.insert(it) }
        }

        private suspend fun populateDefaultTerms(termDao: TermDao) {
            val defaultTerms = listOf(
                // Labs
                TermEntity(
                    question = "Normal range for Hemoglobin (male)?",
                    answer = "13.5 - 17.5 g/dL",
                    categoryId = 1,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal WBC count?",
                    answer = "4,500 - 11,000 /μL",
                    categoryId = 1,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal Platelet count?",
                    answer = "150,000 - 400,000 /μL",
                    categoryId = 1,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal Creatinine range?",
                    answer = "0.7 - 1.3 mg/dL",
                    categoryId = 1,
                    difficulty = 2
                ),
                TermEntity(
                    question = "Normal BUN range?",
                    answer = "7 - 20 mg/dL",
                    categoryId = 1,
                    difficulty = 2
                ),
                TermEntity(
                    question = "Normal Potassium (K+) range?",
                    answer = "3.5 - 5.0 mEq/L",
                    categoryId = 1,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal Sodium (Na+) range?",
                    answer = "135 - 145 mEq/L",
                    categoryId = 1,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal Glucose (fasting) range?",
                    answer = "70 - 100 mg/dL",
                    categoryId = 1,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal pH of blood?",
                    answer = "7.35 - 7.45",
                    categoryId = 1,
                    difficulty = 2
                ),
                TermEntity(
                    question = "Normal pCO2 range?",
                    answer = "35 - 45 mmHg",
                    categoryId = 1,
                    difficulty = 2
                ),

                // Anatomy
                TermEntity(
                    question = "Largest organ in the human body?",
                    answer = "Skin",
                    categoryId = 2,
                    difficulty = 1
                ),
                TermEntity(
                    question = "How many bones in adult human body?",
                    answer = "206 bones",
                    categoryId = 2,
                    difficulty = 1
                ),
                TermEntity(
                    question = "How many vertebrae in the spine?",
                    answer = "33 vertebrae (7 cervical, 12 thoracic, 5 lumbar, 5 sacral fused, 4 coccygeal fused)",
                    categoryId = 2,
                    difficulty = 2
                ),
                TermEntity(
                    question = "Largest artery in the body?",
                    answer = "Aorta",
                    categoryId = 2,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Smallest blood vessels?",
                    answer = "Capillaries",
                    categoryId = 2,
                    difficulty = 1
                ),

                // Drugs
                TermEntity(
                    question = "Drug of choice for anaphylaxis?",
                    answer = "Epinephrine (Adrenaline)",
                    categoryId = 3,
                    difficulty = 1
                ),
                TermEntity(
                    question = "First-line treatment for status epilepticus?",
                    answer = "Lorazepam (Ativan) or Diazepam (Valium)",
                    categoryId = 3,
                    difficulty = 2
                ),
                TermEntity(
                    question = "Antidote for opioid overdose?",
                    answer = "Naloxone (Narcan)",
                    categoryId = 3,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Antidote for acetaminophen overdose?",
                    answer = "N-acetylcysteine (NAC)",
                    categoryId = 3,
                    difficulty = 2
                ),
                TermEntity(
                    question = "Drug class for beta-blockers ending?",
                    answer = "-olol (e.g., metoprolol, propranolol, atenolol)",
                    categoryId = 3,
                    difficulty = 1
                ),

                // Numbers
                TermEntity(
                    question = "Normal adult heart rate range?",
                    answer = "60 - 100 beats per minute",
                    categoryId = 4,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal respiratory rate for adults?",
                    answer = "12 - 20 breaths per minute",
                    categoryId = 4,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal blood pressure range?",
                    answer = "< 120/80 mmHg",
                    categoryId = 4,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal body temperature?",
                    answer = "98.6°F (37°C)",
                    categoryId = 4,
                    difficulty = 1
                ),
                TermEntity(
                    question = "Normal MAP (Mean Arterial Pressure)?",
                    answer = "70 - 100 mmHg",
                    categoryId = 4,
                    difficulty = 2
                ),
                TermEntity(
                    question = "Cardiac output formula?",
                    answer = "CO = HR × SV (Heart Rate × Stroke Volume)",
                    categoryId = 4,
                    difficulty = 2
                ),

                // Pathology
                TermEntity(
                    question = "Most common type of skin cancer?",
                    answer = "Basal cell carcinoma",
                    categoryId = 5,
                    difficulty = 2
                ),
                TermEntity(
                    question = "Leading cause of death worldwide?",
                    answer = "Cardiovascular disease (heart disease and stroke)",
                    categoryId = 5,
                    difficulty = 1
                ),

                // Procedures
                TermEntity(
                    question = "5 rights of medication administration?",
                    answer = "Right patient, drug, dose, route, time",
                    categoryId = 6,
                    difficulty = 1
                ),
                TermEntity(
                    question = "ABCs of emergency medicine?",
                    answer = "Airway, Breathing, Circulation",
                    categoryId = 6,
                    difficulty = 1
                )
            )
            defaultTerms.forEach { termDao.insert(it) }
        }
    }
}
