package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata for a world currency.
 */
data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val countryCode: String,
    val flagEmoji: String,
    val isPopular: Boolean = false
)

/**
 * Room entity to cache exchange rates (relative to USD base = 1.0)
 * Also stores user-defined custom rates when overridden.
 */
@Entity(tableName = "currency_rates")
data class CurrencyRateEntity(
    @PrimaryKey val code: String,
    val rateToUsd: Double,
    val lastUpdated: Long,
    val isCustom: Boolean = false,
    val customRateToUsd: Double? = null
)

/**
 * Room entity for user favorited currencies.
 */
@Entity(tableName = "favorite_currencies")
data class FavoriteCurrencyEntity(
    @PrimaryKey val code: String,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for currencies currently shown on the converter screen in order.
 */
@Entity(tableName = "selected_currencies")
data class SelectedCurrencyEntity(
    @PrimaryKey val code: String,
    val sortOrder: Int
)

/**
 * Room entity for recording conversion history entries.
 */
@Entity(tableName = "conversion_history")
data class ConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val baseCode: String,
    val baseAmount: Double,
    val expression: String,
    val convertedSummaries: String // JSON or comma-separated top conversions
)

/**
 * UI State model for each currency item in the converter list.
 */
data class CurrencyItemUiState(
    val info: CurrencyInfo,
    val amount: Double,
    val formattedAmount: String,
    val rateToBase: Double,
    val inverseRate: Double,
    val isBase: Boolean,
    val isCustomRate: Boolean,
    val isFavorite: Boolean = false
)
