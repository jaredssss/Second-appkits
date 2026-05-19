package com.globalcurrency.converter.data.repository

import androidx.room.*
import com.globalcurrency.converter.data.model.CachedExchangeRate
import com.globalcurrency.converter.data.model.ConversionHistoryEntry
import com.globalcurrency.converter.data.model.RateAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :base")
    suspend fun getCachedRate(base: String): CachedExchangeRate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rate: CachedExchangeRate)

    @Query("DELETE FROM exchange_rates WHERE lastUpdatedAt < :olderThanMs")
    suspend fun deleteOlderThan(olderThanMs: Long)
}

@Dao
interface ConversionHistoryDao {
    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC LIMIT :limit")
    fun getHistory(limit: Int = 30): Flow<List<ConversionHistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ConversionHistoryEntry)

    @Query("DELETE FROM conversion_history WHERE id NOT IN (SELECT id FROM conversion_history ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun pruneOldEntries(keepCount: Int = 200)

    @Query("DELETE FROM conversion_history")
    suspend fun clearAll()
}

@Dao
interface RateAlertDao {
    @Query("SELECT * FROM rate_alerts WHERE isActive = 1")
    fun getActiveAlerts(): Flow<List<RateAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: RateAlert): Long

    @Update
    suspend fun update(alert: RateAlert)

    @Delete
    suspend fun delete(alert: RateAlert)
}

@Database(
    entities = [CachedExchangeRate::class, ConversionHistoryEntry::class, RateAlert::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun conversionHistoryDao(): ConversionHistoryDao
    abstract fun rateAlertDao(): RateAlertDao
}
