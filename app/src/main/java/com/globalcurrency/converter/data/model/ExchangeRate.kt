package com.globalcurrency.converter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** API response model from ExchangeRate-API v6 */
data class ExchangeRateResponse(
    val result: String,           // "success" or "error"
    val documentation: String?,
    val terms_of_use: String?,
    val time_last_update_unix: Long,
    val time_last_update_utc: String?,
    val time_next_update_unix: Long,
    val time_next_update_utc: String?,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)

/** Cached exchange rate stored in Room database */
@Entity(tableName = "exchange_rates")
data class CachedExchangeRate(
    @PrimaryKey
    val baseCurrency: String,
    val ratesJson: String,          // JSON string of Map<String, Double>
    val lastUpdatedAt: Long         // epoch millis
)

/** A single conversion result shown in the UI */
data class ConversionResult(
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val fromAmount: Double,
    val toAmount: Double,
    val rate: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/** Subscription / premium status */
data class PremiumStatus(
    val isPremium: Boolean,
    val expiryTimestamp: Long? = null,
    val email: String? = null
)

/** Alert thresholds set by user (premium feature) */
@Entity(tableName = "rate_alerts")
data class RateAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fromCode: String,
    val toCode: String,
    val targetRate: Double,
    val alertAbove: Boolean,        // true = alert when rate goes ABOVE target
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/** A historical conversion kept in the user's history (last 30 free, unlimited premium) */
@Entity(tableName = "conversion_history")
data class ConversionHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fromCode: String,
    val toCode: String,
    val fromAmount: Double,
    val toAmount: Double,
    val rate: Double,
    val timestamp: Long = System.currentTimeMillis()
)
