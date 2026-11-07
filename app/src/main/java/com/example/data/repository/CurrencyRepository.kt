package com.example.data.repository

import com.example.data.local.CurrencyDao
import com.example.data.local.UserPreferences
import com.example.data.model.ConversionHistoryEntity
import com.example.data.model.CurrencyCatalog
import com.example.data.model.CurrencyInfo
import com.example.data.model.CurrencyRateEntity
import com.example.data.model.FavoriteCurrencyEntity
import com.example.data.model.SelectedCurrencyEntity
import com.example.data.remote.ExchangeRateProvider
import com.example.data.remote.OpenErApiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrencyRepository(
    private val dao: CurrencyDao,
    private val preferences: UserPreferences,
    private val rateProvider: ExchangeRateProvider = OpenErApiProvider()
) {
    val ratesFlow: Flow<Map<String, CurrencyRateEntity>> = dao.getAllRates().map { list ->
        list.associateBy { it.code }
    }

    val favoritesFlow: Flow<Set<String>> = dao.getFavorites().map { list ->
        list.map { it.code }.toSet()
    }

    val selectedCurrenciesFlow: Flow<List<String>> = dao.getSelectedCurrencies().map { list ->
        list.map { it.code }
    }

    val historyFlow: Flow<List<ConversionHistoryEntity>> = dao.getHistory()

    suspend fun initializeDefaultsIfNeeded() {
        withContext(Dispatchers.IO) {
            val selected = dao.getSelectedCurrencies().first()
            if (selected.isEmpty()) {
                val initialSelected = CurrencyCatalog.DEFAULT_SELECTED_CODES.mapIndexed { index, code ->
                    SelectedCurrencyEntity(code = code, sortOrder = index)
                }
                dao.setSelectedCurrencies(initialSelected)
            }

            val existingRates = dao.getAllRates().first()
            if (existingRates.isEmpty()) {
                val currentTime = System.currentTimeMillis()
                val initialRates = CurrencyCatalog.DEFAULT_RATES_USD.map { (code, rate) ->
                    CurrencyRateEntity(
                        code = code,
                        rateToUsd = rate,
                        lastUpdated = currentTime,
                        isCustom = false
                    )
                }
                dao.insertOrUpdateRates(initialRates)
                preferences.setLastUpdateTime(currentTime)
            }
        }
    }

    suspend fun refreshRates(): Result<Int> = withContext(Dispatchers.IO) {
        val result = rateProvider.getRates("USD")
        result.fold(
            onSuccess = { ratesMap ->
                val now = System.currentTimeMillis()
                val existingMap = dao.getAllRates().first().associateBy { it.code }

                val updatedEntities = ratesMap.map { (code, rate) ->
                    val existing = existingMap[code]
                    CurrencyRateEntity(
                        code = code,
                        rateToUsd = rate,
                        lastUpdated = now,
                        isCustom = existing?.isCustom ?: false,
                        customRateToUsd = existing?.customRateToUsd
                    )
                }
                dao.insertOrUpdateRates(updatedEntities)
                preferences.setLastUpdateTime(now)
                Result.success(updatedEntities.size)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun addSelectedCurrency(code: String) = withContext(Dispatchers.IO) {
        val current = dao.getSelectedCurrencies().first()
        if (current.none { it.code == code }) {
            val nextOrder = current.size
            dao.setSelectedCurrencies(current + SelectedCurrencyEntity(code, nextOrder))
        }
    }

    suspend fun removeSelectedCurrency(code: String) = withContext(Dispatchers.IO) {
        val current = dao.getSelectedCurrencies().first()
        // Ensure at least 2 currencies remain
        if (current.size > 2) {
            dao.removeSelectedCurrency(code)
            // Re-index orders
            val updated = current.filter { it.code != code }.mapIndexed { index, item ->
                SelectedCurrencyEntity(item.code, index)
            }
            dao.setSelectedCurrencies(updated)
        }
    }

    suspend fun reorderSelectedCurrencies(newOrder: List<String>) = withContext(Dispatchers.IO) {
        val entities = newOrder.mapIndexed { index, code ->
            SelectedCurrencyEntity(code, index)
        }
        dao.setSelectedCurrencies(entities)
    }

    suspend fun swapCurrencies(codeA: String, codeB: String) = withContext(Dispatchers.IO) {
        val current = dao.getSelectedCurrencies().first().map { it.code }.toMutableList()
        val indexA = current.indexOf(codeA)
        val indexB = current.indexOf(codeB)
        if (indexA != -1 && indexB != -1) {
            current[indexA] = codeB
            current[indexB] = codeA
            reorderSelectedCurrencies(current)
        }
    }

    suspend fun moveCurrency(code: String, moveUp: Boolean) = withContext(Dispatchers.IO) {
        val current = dao.getSelectedCurrencies().first().map { it.code }.toMutableList()
        val index = current.indexOf(code)
        if (index != -1) {
            val targetIndex = if (moveUp) index - 1 else index + 1
            if (targetIndex in current.indices) {
                val item = current.removeAt(index)
                current.add(targetIndex, item)
                reorderSelectedCurrencies(current)
            }
        }
    }

    suspend fun toggleFavorite(code: String) = withContext(Dispatchers.IO) {
        val favorites = dao.getFavorites().first()
        if (favorites.any { it.code == code }) {
            dao.removeFavorite(code)
        } else {
            dao.addFavorite(FavoriteCurrencyEntity(code))
        }
    }

    suspend fun setCustomRate(code: String, customRateToUsd: Double) = withContext(Dispatchers.IO) {
        dao.updateCustomRate(code, isCustom = true, customRate = customRateToUsd)
    }

    suspend fun resetCustomRate(code: String) = withContext(Dispatchers.IO) {
        dao.resetCustomRate(code)
    }

    suspend fun resetAllCustomRates() = withContext(Dispatchers.IO) {
        dao.resetAllCustomRates()
    }

    suspend fun saveConversion(
        baseCode: String,
        baseAmount: Double,
        expression: String,
        summaries: String
    ) = withContext(Dispatchers.IO) {
        dao.insertHistory(
            ConversionHistoryEntity(
                baseCode = baseCode,
                baseAmount = baseAmount,
                expression = expression,
                convertedSummaries = summaries
            )
        )
    }

    suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteHistory(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearHistory()
    }
}
