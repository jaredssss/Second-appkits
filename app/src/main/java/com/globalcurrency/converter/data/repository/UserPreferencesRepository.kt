package com.globalcurrency.converter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.globalcurrency.converter.data.model.PremiumStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val IS_PREMIUM          = booleanPreferencesKey("is_premium")
        val PREMIUM_EXPIRY      = longPreferencesKey("premium_expiry")
        val PREMIUM_EMAIL       = stringPreferencesKey("premium_email")
        val STRIPE_CUSTOMER_ID  = stringPreferencesKey("stripe_customer_id")
        val DEFAULT_FROM        = stringPreferencesKey("default_from")
        val DEFAULT_TO          = stringPreferencesKey("default_to")
        val FAVORITES           = stringSetPreferencesKey("favorites")
    }

    val premiumStatus: Flow<PremiumStatus> = context.dataStore.data.map { prefs ->
        val isPremium  = prefs[Keys.IS_PREMIUM] ?: false
        val expiry     = prefs[Keys.PREMIUM_EXPIRY]
        val email      = prefs[Keys.PREMIUM_EMAIL]
        // If expiry has passed, treat as non-premium
        val nowValid   = isPremium && (expiry == null || expiry > System.currentTimeMillis())
        PremiumStatus(isPremium = nowValid, expiryTimestamp = expiry, email = email)
    }

    val defaultFromCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_FROM] ?: "USD"
    }

    val defaultToCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_TO] ?: "EUR"
    }

    val favoriteCurrencies: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITES] ?: setOf("USD", "EUR", "GBP", "JPY", "AUD")
    }

    suspend fun setPremiumStatus(isPremium: Boolean, expiryMs: Long?, email: String?) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM]     = isPremium
            if (expiryMs != null) prefs[Keys.PREMIUM_EXPIRY] = expiryMs
            if (email != null)    prefs[Keys.PREMIUM_EMAIL]  = email
        }
    }

    suspend fun setDefaultCurrencies(from: String, to: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_FROM] = from
            prefs[Keys.DEFAULT_TO]   = to
        }
    }

    suspend fun toggleFavorite(code: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            if (code in current) current.remove(code) else current.add(code)
            prefs[Keys.FAVORITES] = current
        }
    }
}
