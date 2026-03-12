package com.medpopquiz.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Popup on unlock
    fun isPopupOnUnlockEnabled(): Boolean {
        return prefs.getBoolean(KEY_POPUP_ON_UNLOCK, true)
    }

    fun setPopupOnUnlockEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_POPUP_ON_UNLOCK, enabled) }
    }

    // Random order
    fun isRandomOrder(): Boolean {
        return prefs.getBoolean(KEY_RANDOM_ORDER, true)
    }

    fun setRandomOrder(random: Boolean) {
        prefs.edit { putBoolean(KEY_RANDOM_ORDER, random) }
    }

    // Daily review
    fun isDailyReviewEnabled(): Boolean {
        return prefs.getBoolean(KEY_DAILY_REVIEW, false)
    }

    fun setDailyReviewEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DAILY_REVIEW, enabled) }
    }

    // Daily review count
    fun getDailyReviewCount(): Int {
        return prefs.getInt(KEY_DAILY_REVIEW_COUNT, 3)
    }

    fun setDailyReviewCount(count: Int) {
        prefs.edit { putInt(KEY_DAILY_REVIEW_COUNT, count) }
    }

    // Daily review time
    fun getDailyReviewHour(): Int {
        return prefs.getInt(KEY_DAILY_REVIEW_HOUR, 9)
    }

    fun setDailyReviewHour(hour: Int) {
        prefs.edit { putInt(KEY_DAILY_REVIEW_HOUR, hour) }
    }

    fun getDailyReviewMinute(): Int {
        return prefs.getInt(KEY_DAILY_REVIEW_MINUTE, 0)
    }

    fun setDailyReviewMinute(minute: Int) {
        prefs.edit { putInt(KEY_DAILY_REVIEW_MINUTE, minute) }
    }

    // Minimum interval between popups (in seconds)
    fun getMinIntervalSeconds(): Int {
        return prefs.getInt(KEY_MIN_INTERVAL, 3)
    }

    fun setMinIntervalSeconds(seconds: Int) {
        prefs.edit { putInt(KEY_MIN_INTERVAL, seconds) }
    }

    // Show answer first or question first
    fun isShowAnswerFirst(): Boolean {
        return prefs.getBoolean(KEY_SHOW_ANSWER_FIRST, false)
    }

    fun setShowAnswerFirst(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_ANSWER_FIRST, show) }
    }

    // Selected categories (comma-separated IDs)
    fun getSelectedCategoryIds(): Set<Long> {
        val idsString = prefs.getString(KEY_SELECTED_CATEGORIES, "") ?: ""
        return if (idsString.isEmpty()) {
            emptySet()
        } else {
            idsString.split(",").mapNotNull { it.toLongOrNull() }.toSet()
        }
    }

    fun setSelectedCategoryIds(ids: Set<Long>) {
        prefs.edit { putString(KEY_SELECTED_CATEGORIES, ids.joinToString(",")) }
    }

    // First launch
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchComplete() {
        prefs.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
    }

    // Stats
    fun getTotalSessions(): Int {
        return prefs.getInt(KEY_TOTAL_SESSIONS, 0)
    }

    fun incrementTotalSessions() {
        prefs.edit { putInt(KEY_TOTAL_SESSIONS, getTotalSessions() + 1) }
    }

    fun getCurrentStreak(): Int {
        return prefs.getInt(KEY_CURRENT_STREAK, 0)
    }

    fun setCurrentStreak(streak: Int) {
        prefs.edit { putInt(KEY_CURRENT_STREAK, streak) }
    }

    fun getLongestStreak(): Int {
        return prefs.getInt(KEY_LONGEST_STREAK, 0)
    }

    fun setLongestStreak(streak: Int) {
        prefs.edit { putInt(KEY_LONGEST_STREAK, streak) }
    }

    // Last study date
    fun getLastStudyDate(): Long {
        return prefs.getLong(KEY_LAST_STUDY_DATE, 0)
    }

    fun setLastStudyDate(date: Long) {
        prefs.edit { putLong(KEY_LAST_STUDY_DATE, date) }
    }

    // Reset all settings
    fun resetAll() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "medpop_quiz_prefs"
        
        private const val KEY_POPUP_ON_UNLOCK = "popup_on_unlock"
        private const val KEY_RANDOM_ORDER = "random_order"
        private const val KEY_DAILY_REVIEW = "daily_review"
        private const val KEY_DAILY_REVIEW_COUNT = "daily_review_count"
        private const val KEY_DAILY_REVIEW_HOUR = "daily_review_hour"
        private const val KEY_DAILY_REVIEW_MINUTE = "daily_review_minute"
        private const val KEY_MIN_INTERVAL = "min_interval"
        private const val KEY_SHOW_ANSWER_FIRST = "show_answer_first"
        private const val KEY_SELECTED_CATEGORIES = "selected_categories"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_TOTAL_SESSIONS = "total_sessions"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_LAST_STUDY_DATE = "last_study_date"
    }
}
