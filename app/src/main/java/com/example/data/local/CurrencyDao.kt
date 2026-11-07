package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ConversionHistoryEntity
import com.example.data.model.CurrencyRateEntity
import com.example.data.model.FavoriteCurrencyEntity
import com.example.data.model.SelectedCurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currency_rates")
    fun getAllRates(): Flow<List<CurrencyRateEntity>>

    @Query("SELECT * FROM currency_rates WHERE code = :code LIMIT 1")
    suspend fun getRate(code: String): CurrencyRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRates(rates: List<CurrencyRateEntity>)

    @Query("UPDATE currency_rates SET isCustom = :isCustom, customRateToUsd = :customRate WHERE code = :code")
    suspend fun updateCustomRate(code: String, isCustom: Boolean, customRate: Double?)

    @Query("UPDATE currency_rates SET isCustom = 0, customRateToUsd = NULL WHERE code = :code")
    suspend fun resetCustomRate(code: String)

    @Query("UPDATE currency_rates SET isCustom = 0, customRateToUsd = NULL")
    suspend fun resetAllCustomRates()

    // Favorites
    @Query("SELECT * FROM favorite_currencies ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<FavoriteCurrencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteCurrencyEntity)

    @Query("DELETE FROM favorite_currencies WHERE code = :code")
    suspend fun removeFavorite(code: String)

    // Selected / Displayed Currencies on Main Screen
    @Query("SELECT * FROM selected_currencies ORDER BY sortOrder ASC")
    fun getSelectedCurrencies(): Flow<List<SelectedCurrencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSelectedCurrencies(currencies: List<SelectedCurrencyEntity>)

    @Query("DELETE FROM selected_currencies WHERE code = :code")
    suspend fun removeSelectedCurrency(code: String)

    @Query("DELETE FROM selected_currencies")
    suspend fun clearSelectedCurrencies()

    // Conversion History
    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC LIMIT 100")
    fun getHistory(): Flow<List<ConversionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ConversionHistoryEntity)

    @Query("DELETE FROM conversion_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM conversion_history")
    suspend fun clearHistory()
}
