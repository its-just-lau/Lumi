package com.lumi.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(
    name = "onboarding_preferences"
)

data class OnboardingUserPreferences(
    val selectedGoals: Set<String> = emptySet(),
    val dailyGoalMinutes: Int = 15,
    val currentStep: String = "WELCOME",
    val tourIndex: Int = 0,
    val tourCompleted: Boolean = false
)

class OnboardingPreferencesRepository(
    private val context: Context
) {

    private object PreferencesKeys {
        val SELECTED_GOALS =
            stringSetPreferencesKey("selected_goals")

        val DAILY_GOAL =
            intPreferencesKey("daily_goal_minutes")

        val CURRENT_STEP =
            stringPreferencesKey("current_step")

        val TOUR_INDEX =
            intPreferencesKey("tour_index")

        val TOUR_COMPLETED =
            booleanPreferencesKey("tour_completed")

        val ONBOARDING_COMPLETED =
            booleanPreferencesKey("onboarding_completed")
    }

    val preferences: Flow<OnboardingUserPreferences> =
        context.onboardingDataStore.data.map { preferences ->

            OnboardingUserPreferences(
                selectedGoals = preferences[
                    PreferencesKeys.SELECTED_GOALS
                ] ?: emptySet(),

                dailyGoalMinutes = preferences[
                    PreferencesKeys.DAILY_GOAL
                ] ?: 15,

                currentStep = preferences[
                    PreferencesKeys.CURRENT_STEP
                ] ?: "WELCOME",

                tourIndex = preferences[
                    PreferencesKeys.TOUR_INDEX
                ] ?: 0,

                tourCompleted = preferences[
                    PreferencesKeys.TOUR_COMPLETED
                ] ?: false
            )
        }

    suspend fun saveGoals(goals: Set<String>) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_GOALS] = goals
        }
    }

    suspend fun saveDailyGoal(minutes: Int) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_GOAL] = minutes
        }
    }

    suspend fun saveCurrentStep(step: String) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_STEP] = step
        }
    }

    suspend fun saveTourIndex(index: Int) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.TOUR_INDEX] = index
        }
    }

    suspend fun saveTourCompleted(completed: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.TOUR_COMPLETED] = completed
        }
    }

    suspend fun clearPreferences() {
        context.onboardingDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
