package com.globalcurrency.converter.data.repository

import android.util.Log
import com.globalcurrency.converter.data.api.ExchangeRateApi
import com.globalcurrency.converter.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CurrencyRepository"
private val exchangeRateCacheTtlMs = TimeUnit.HOURS.toMillis(6) // 6 hours cache duration

@Singleton
class CurrencyRepository @Inject constructor(
    private val api: ExchangeRateApi,
    private val db: AppDatabase,
    private val prefs: UserPreferencesRepository
) {
    private val gson = Gson()

    /**
     * Returns exchange rates for [baseCurrency].
     * Reads from cache first; fetches from network if stale (> 6 h old).
     */
    suspend fun getRates(baseCurrency: String): Result<Map<String, Double>> =
        withContext(Dispatchers.IO) {
            val cached = db.exchangeRateDao().getCachedRate(baseCurrency)
            val now = System.currentTimeMillis()
            if (cached != null && (now - cached.lastUpdatedAt) < exchangeRateCacheTtlMs) {
                val type = object : TypeToken<Map<String, Double>>() {}.type
                val rates: Map<String, Double> = gson.fromJson(cached.ratesJson, type)
                return@withContext Result.success(rates)
            }
            // Fetch from network
            return@withContext try {
                val response = api.getLatestRates(baseCurrency)
                if (response.isSuccessful && response.body()?.result == "success") {
                    val rates = response.body()!!.conversion_rates
                    // Cache it
                    db.exchangeRateDao().insertOrUpdate(
                        CachedExchangeRate(
                            baseCurrency = baseCurrency,
                            ratesJson = gson.toJson(rates),
                            lastUpdatedAt = now
                        )
                    )
                    Result.success(rates)
                } else {
                    // Try stale cache as fallback
                    if (cached != null) {
                        val type = object : TypeToken<Map<String, Double>>() {}.type
                        val rates: Map<String, Double> = gson.fromJson(cached.ratesJson, type)
                        Result.success(rates)
                    } else {
                        Result.failure(Exception("No data available. Please check your internet connection."))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}")
                if (cached != null) {
                    val type = object : TypeToken<Map<String, Double>>() {}.type
                    val rates: Map<String, Double> = gson.fromJson(cached.ratesJson, type)
                    Result.success(rates) // serve stale cache offline
                } else {
                    Result.failure(Exception("No internet and no cached data available."))
                }
            }
        }

    /**
     * Converts [amount] from [fromCode] to [toCode].
     * Returns null if rates are unavailable.
     */
    suspend fun convert(fromCode: String, toCode: String, amount: Double): Result<ConversionResult> {
        val fromCurrency = ALL_CURRENCIES.find { it.code == fromCode }
            ?: return Result.failure(Exception("Unknown currency: $fromCode"))
        val toCurrency = ALL_CURRENCIES.find { it.code == toCode }
            ?: return Result.failure(Exception("Unknown currency: $toCode"))

        return getRates(fromCode).fold(
            onSuccess = { rates ->
                val rate = rates[toCode] ?: return@fold Result.failure(
                    Exception("No rate available for $toCode")
                )
                val result = ConversionResult(
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    fromAmount = amount,
                    toAmount = amount * rate,
                    rate = rate
                )
                withContext(Dispatchers.IO) {
                    db.conversionHistoryDao().insert(
                        ConversionHistoryEntry(
                            fromCode = fromCode,
                            toCode = toCode,
                            fromAmount = amount,
                            toAmount = result.toAmount,
                            rate = rate
                        )
                    )
                    db.conversionHistoryDao().pruneOldEntries()
                }
                Result.success(result)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    fun getConversionHistory(limit: Int = 30): Flow<List<ConversionHistoryEntry>> =
        db.conversionHistoryDao().getHistory(limit)

    fun getRateAlerts(): Flow<List<RateAlert>> =
        db.rateAlertDao().getActiveAlerts()

    suspend fun addRateAlert(alert: RateAlert) =
        withContext(Dispatchers.IO) { db.rateAlertDao().insert(alert) }

    suspend fun deleteRateAlert(alert: RateAlert) =
        withContext(Dispatchers.IO) { db.rateAlertDao().delete(alert) }

    val premiumStatus = prefs.premiumStatus
    val defaultFromCurrency = prefs.defaultFromCurrency
    val defaultToCurrency = prefs.defaultToCurrency
    val favoriteCurrencies = prefs.favoriteCurrencies

    suspend fun setPremiumStatus(isPremium: Boolean, expiryMs: Long?, email: String?) =
        prefs.setPremiumStatus(isPremium, expiryMs, email)

    suspend fun setDefaultCurrencies(from: String, to: String) =
        prefs.setDefaultCurrencies(from, to)

    suspend fun toggleFavorite(code: String) = prefs.toggleFavorite(code)
}
