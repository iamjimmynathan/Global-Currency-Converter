package com.example.ui

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferences
import com.example.data.model.ConversionHistoryEntity
import com.example.data.model.CurrencyCatalog
import com.example.data.model.CurrencyInfo
import com.example.data.model.CurrencyItemUiState
import com.example.data.model.CurrencyRateEntity
import com.example.data.repository.CurrencyRepository
import com.example.domain.CalculatorEngine
import com.example.monetization.AdManager
import com.example.monetization.SubscriptionManager
import com.example.monetization.SubscriptionPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CurrencySelectionMode {
    BASE,
    TARGET
}

sealed interface ActiveSheet {
    data object None : ActiveSheet
    data class CurrencySelect(val mode: CurrencySelectionMode) : ActiveSheet
    data object History : ActiveSheet
    data class CustomRates(val targetCode: String? = null) : ActiveSheet
    data object Settings : ActiveSheet
    data object Premium : ActiveSheet
    data class CurrencyDetail(val code: String) : ActiveSheet
    data object About : ActiveSheet
}

data class MainUiState(
    val expression: String = "100",
    val baseAmount: Double = 100.0,
    val baseCurrencyCode: String = "USD",
    val targetCurrencyCode: String = "EUR",
    val baseItem: CurrencyItemUiState? = null,
    val targetItem: CurrencyItemUiState? = null,
    val rateBaseToTarget: Double = 1.0,
    val inverseRate: Double = 1.0,
    val favorites: Set<String> = emptySet(),
    val isRefreshing: Boolean = false,
    val refreshMessage: String? = null,
    val lastUpdatedFormatted: String = "Just now",
    val isOffline: Boolean = false,
    val isPremium: Boolean = false,
    val activeSheet: ActiveSheet = ActiveSheet.None,
    val decimalPrecision: Int = 2,
    val numberFormat: String = "STANDARD",
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val themeMode: String = "DARK"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val preferences = UserPreferences(application)
    val repository = CurrencyRepository(db.currencyDao(), preferences)
    val subscriptionManager = SubscriptionManager(application, preferences, viewModelScope)

    private val _expression = MutableStateFlow("100")
    private val _baseCurrencyCode = MutableStateFlow("USD")
    private val _targetCurrencyCode = MutableStateFlow("EUR")
    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshMessage = MutableStateFlow<String?>(null)
    private val _activeSheet = MutableStateFlow<ActiveSheet>(ActiveSheet.None)

    val history = repository.historyFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val uiState: StateFlow<MainUiState> = combine(
        _expression,
        _baseCurrencyCode,
        _targetCurrencyCode,
        repository.ratesFlow,
        repository.favoritesFlow,
        preferences.isPremium,
        preferences.decimalPrecision,
        preferences.numberFormat,
        preferences.hapticEnabled,
        preferences.soundEnabled,
        preferences.themeMode,
        preferences.lastUpdateTime,
        _isRefreshing,
        _refreshMessage,
        _activeSheet
    ) { args: Array<Any?> ->
        var idx = 0
        val expr = args[idx++] as String
        val baseCode = args[idx++] as String
        val targetCode = args[idx++] as String
        @Suppress("UNCHECKED_CAST")
        val rates = args[idx++] as Map<String, CurrencyRateEntity>
        @Suppress("UNCHECKED_CAST")
        val favorites = args[idx++] as Set<String>
        val isPremium = args[idx++] as Boolean
        val precision = args[idx++] as Int
        val numFormat = args[idx++] as String
        val haptic = args[idx++] as Boolean
        val sound = args[idx++] as Boolean
        val theme = args[idx++] as String
        val lastUpdate = args[idx++] as Long
        val isRef = args[idx++] as Boolean
        val refMsg = args[idx++] as String?
        val sheet = args[idx++] as ActiveSheet

        val evaluatedAmount = CalculatorEngine.evaluate(expr) ?: 0.0

        // Base rate to USD
        val baseEntity = rates[baseCode]
        val baseRateToUsd = if (baseEntity?.isCustom == true && baseEntity.customRateToUsd != null) {
            baseEntity.customRateToUsd
        } else {
            baseEntity?.rateToUsd ?: CurrencyCatalog.DEFAULT_RATES_USD[baseCode] ?: 1.0
        }

        // Target rate to USD
        val targetEntity = rates[targetCode]
        val targetRateToUsd = if (targetEntity?.isCustom == true && targetEntity.customRateToUsd != null) {
            targetEntity.customRateToUsd
        } else {
            targetEntity?.rateToUsd ?: CurrencyCatalog.DEFAULT_RATES_USD[targetCode] ?: 1.0
        }

        // 1 Base = (targetRateToUsd / baseRateToUsd) Target
        val rateBaseToTarget = if (baseRateToUsd > 0.0) targetRateToUsd / baseRateToUsd else 1.0
        val targetConvertedAmount = evaluatedAmount * rateBaseToTarget
        val inverseRate = if (rateBaseToTarget > 0.0) 1.0 / rateBaseToTarget else 0.0

        val baseInfo = CurrencyCatalog.find(baseCode)
        val targetInfo = CurrencyCatalog.find(targetCode)

        val baseItem = CurrencyItemUiState(
            info = baseInfo,
            amount = evaluatedAmount,
            formattedAmount = CalculatorEngine.formatNumber(evaluatedAmount, precision, numFormat),
            rateToBase = 1.0,
            inverseRate = 1.0,
            isBase = true,
            isCustomRate = baseEntity?.isCustom == true,
            isFavorite = favorites.contains(baseCode)
        )

        val targetItem = CurrencyItemUiState(
            info = targetInfo,
            amount = targetConvertedAmount,
            formattedAmount = CalculatorEngine.formatNumber(targetConvertedAmount, precision, numFormat),
            rateToBase = rateBaseToTarget,
            inverseRate = inverseRate,
            isBase = false,
            isCustomRate = targetEntity?.isCustom == true,
            isFavorite = favorites.contains(targetCode)
        )

        val lastUpdatedStr = if (lastUpdate > 0) {
            SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(lastUpdate))
        } else {
            "Default cached rates"
        }

        MainUiState(
            expression = expr,
            baseAmount = evaluatedAmount,
            baseCurrencyCode = baseCode,
            targetCurrencyCode = targetCode,
            baseItem = baseItem,
            targetItem = targetItem,
            rateBaseToTarget = rateBaseToTarget,
            inverseRate = inverseRate,
            favorites = favorites,
            isRefreshing = isRef,
            refreshMessage = refMsg,
            lastUpdatedFormatted = lastUpdatedStr,
            isOffline = false,
            isPremium = isPremium,
            activeSheet = sheet,
            decimalPrecision = precision,
            numberFormat = numFormat,
            hapticEnabled = haptic,
            soundEnabled = sound,
            themeMode = theme
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainUiState()
    )

    init {
        AdManager.initialize(application)
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            val savedBase = preferences.defaultBaseCurrency.first()
            val savedTarget = preferences.targetCurrency.first()
            _baseCurrencyCode.value = savedBase
            _targetCurrencyCode.value = savedTarget
            refreshExchangeRates(silent = true)
        }
    }

    fun onKeypadClick(key: String, view: View? = null) {
        performHaptic(view)
        val current = _expression.value

        when (key) {
            "C" -> {
                _expression.value = "0"
            }
            "⌫" -> {
                if (current.length <= 1) {
                    _expression.value = "0"
                } else {
                    _expression.value = current.dropLast(1)
                }
            }
            "=" -> {
                val evaluated = CalculatorEngine.evaluate(current)
                if (evaluated != null) {
                    _expression.value = if (evaluated % 1.0 == 0.0 && evaluated < 1e12) {
                        evaluated.toLong().toString()
                    } else {
                        String.format(Locale.US, "%.4f", evaluated).trimEnd('0').trimEnd('.')
                    }
                    saveCurrentConversionToHistory(evaluated)
                }
            }
            "⇅" -> {
                swapBaseAndTarget()
            }
            "+", "-", "×", "÷" -> {
                if (current.isNotEmpty() && current.last() in listOf('+', '-', '×', '÷')) {
                    _expression.value = current.dropLast(1) + key
                } else {
                    _expression.value = "$current $key "
                }
            }
            "%" -> {
                if (current.isNotEmpty() && current.last().isDigit()) {
                    _expression.value = "$current%"
                }
            }
            "." -> {
                val tokens = current.split(" ")
                val lastToken = tokens.lastOrNull() ?: ""
                if (!lastToken.contains(".")) {
                    _expression.value = "$current."
                }
            }
            else -> {
                if (current == "0") {
                    _expression.value = key
                } else {
                    _expression.value = current + key
                }
            }
        }
    }

    fun swapBaseAndTarget() {
        val currentBase = _baseCurrencyCode.value
        val currentTarget = _targetCurrencyCode.value
        val currentTargetAmount = uiState.value.targetItem?.amount ?: 0.0

        _baseCurrencyCode.value = currentTarget
        _targetCurrencyCode.value = currentBase

        // Update the base expression to the current converted target amount
        if (currentTargetAmount > 0.0) {
            _expression.value = if (currentTargetAmount % 1.0 == 0.0 && currentTargetAmount < 1e12) {
                currentTargetAmount.toLong().toString()
            } else {
                String.format(Locale.US, "%.2f", currentTargetAmount).trimEnd('0').trimEnd('.')
            }
        }

        viewModelScope.launch {
            preferences.setDefaultBaseCurrency(currentTarget)
            preferences.setTargetCurrency(currentBase)
        }
    }

    fun selectCurrency(code: String, mode: CurrencySelectionMode) {
        viewModelScope.launch {
            when (mode) {
                CurrencySelectionMode.BASE -> {
                    if (code == _targetCurrencyCode.value) {
                        // If same as target, swap them
                        swapBaseAndTarget()
                    } else {
                        _baseCurrencyCode.value = code
                        preferences.setDefaultBaseCurrency(code)
                    }
                }
                CurrencySelectionMode.TARGET -> {
                    if (code == _baseCurrencyCode.value) {
                        // If same as base, swap them
                        swapBaseAndTarget()
                    } else {
                        _targetCurrencyCode.value = code
                        preferences.setTargetCurrency(code)
                    }
                }
            }
            closeSheet()
        }
    }

    fun openSheet(sheet: ActiveSheet) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = ActiveSheet.None
    }

    fun refreshExchangeRates(silent: Boolean = false) {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        _refreshMessage.value = if (!silent) "Fetching live rates..." else null

        viewModelScope.launch {
            val result = repository.refreshRates()
            _isRefreshing.value = false
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _refreshMessage.value = "Updated $count currency rates"
            } else {
                val error = result.exceptionOrNull()?.message ?: "Network error"
                _refreshMessage.value = if (!silent) "Using cached rates: $error" else null
            }
        }
    }

    fun toggleFavorite(code: String) {
        viewModelScope.launch {
            repository.toggleFavorite(code)
        }
    }

    fun setCustomRate(code: String, customRateToUsd: Double) {
        viewModelScope.launch {
            repository.setCustomRate(code, customRateToUsd)
            _refreshMessage.value = "Custom rate set for $code"
        }
    }

    fun resetCustomRate(code: String) {
        viewModelScope.launch {
            repository.resetCustomRate(code)
            _refreshMessage.value = "Reset $code to live market rate"
        }
    }

    fun resetAllCustomRates() {
        viewModelScope.launch {
            repository.resetAllCustomRates()
            _refreshMessage.value = "Reset all rates to live market rates"
        }
    }

    private fun saveCurrentConversionToHistory(amount: Double) {
        viewModelScope.launch {
            val state = uiState.value
            val target = state.targetItem
            val summaries = if (target != null) {
                "${target.info.code} ${target.formattedAmount}"
            } else ""

            repository.saveConversion(
                baseCode = state.baseCurrencyCode,
                baseAmount = amount,
                expression = state.expression,
                summaries = summaries
            )
        }
    }

    fun restoreHistoryItem(item: ConversionHistoryEntity) {
        _baseCurrencyCode.value = item.baseCode
        _expression.value = item.expression
        closeSheet()
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun updatePrecision(precision: Int) {
        viewModelScope.launch {
            preferences.setDecimalPrecision(precision)
        }
    }

    fun updateNumberFormat(format: String) {
        viewModelScope.launch {
            preferences.setNumberFormat(format)
        }
    }

    fun updateHaptic(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setHapticEnabled(enabled)
        }
    }

    fun updateSound(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setSoundEnabled(enabled)
        }
    }

    fun updateThemeMode(theme: String) {
        viewModelScope.launch {
            preferences.setThemeMode(theme)
        }
    }

    fun upgradeSubscription(activity: Activity, plan: SubscriptionPlan) {
        subscriptionManager.launchPurchaseFlow(activity, plan)
    }

    fun restorePurchases(onComplete: (Boolean) -> Unit) {
        subscriptionManager.restorePurchases(onComplete)
    }

    fun testTogglePremium() {
        val current = uiState.value.isPremium
        if (current) {
            subscriptionManager.setTestFreeMode()
        } else {
            subscriptionManager.simulateSubscriptionSuccess(
                subscriptionManager.yearlyPlan.value
            )
        }
    }

    private fun performHaptic(view: View?) {
        if (!uiState.value.hapticEnabled) return
        try {
            if (view != null) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } else {
                val app = getApplication<Application>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = app.getSystemService(VibratorManager::class.java)
                    vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = app.getSystemService(Vibrator::class.java)
                    vibrator?.vibrate(20)
                }
            }
        } catch (_: Exception) {}
    }
}
