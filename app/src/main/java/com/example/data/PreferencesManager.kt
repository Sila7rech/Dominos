package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dominos_user_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("preferred_language")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_KEEP_SIGNED_IN = booleanPreferencesKey("keep_signed_in")
        private val KEY_NOTIF_ORDERS = booleanPreferencesKey("notif_order_updates")
        private val KEY_NOTIF_PROMOS = booleanPreferencesKey("notif_promos")
        private val KEY_FAVORITE_IDS = stringSetPreferencesKey("favorite_product_ids")
    }

    // French is the mandatory default language on first launch
    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[KEY_LANGUAGE] ?: AppLanguage.FR.code)
    }

    val onboardingDoneFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    val keepSignedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_KEEP_SIGNED_IN] ?: true
    }

    val notifOrderUpdatesFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIF_ORDERS] ?: true
    }

    val notifPromosFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIF_PROMOS] ?: true
    }

    val favoriteIdsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITE_IDS] ?: setOf("prod-pepperoni-feast", "prod-cheese-burger")
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.code
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = done
        }
    }

    suspend fun setKeepSignedIn(keep: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_KEEP_SIGNED_IN] = keep
        }
    }

    suspend fun setNotifOrderUpdates(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIF_ORDERS] = enabled
        }
    }

    suspend fun setNotifPromos(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIF_PROMOS] = enabled
        }
    }

    suspend fun toggleFavorite(productId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITE_IDS]?.toMutableSet()
                ?: mutableSetOf("prod-pepperoni-feast", "prod-cheese-burger")
            if (current.contains(productId)) {
                current.remove(productId)
            } else {
                current.add(productId)
            }
            prefs[KEY_FAVORITE_IDS] = current
        }
    }
}
