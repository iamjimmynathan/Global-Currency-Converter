package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AdBannerView
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.CurrencyCardItem
import com.example.ui.sheets.CurrencyDetailSheet
import com.example.ui.sheets.CurrencySelectionSheet
import com.example.ui.sheets.CustomRatesSheet
import com.example.ui.sheets.HistorySheet
import com.example.ui.sheets.PremiumSheet
import com.example.ui.sheets.SettingsSheet
import com.example.ui.theme.CharcoalDeepBlack
import com.example.ui.theme.CharcoalSecondaryBg
import com.example.ui.theme.LiveRateGreen
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.TextLightGreySecondary
import com.example.ui.theme.TextWhitePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.history.collectAsState()
    val monthlyPlan by viewModel.subscriptionManager.monthlyPlan.collectAsState()
    val yearlyPlan by viewModel.subscriptionManager.yearlyPlan.collectAsState()
    val billingMessage by viewModel.subscriptionManager.billingStatusMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isMenuOpen by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.refreshMessage) {
        uiState.refreshMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(billingMessage) {
        billingMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalDeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Official App Logo Icon Thumbnail
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Currency Converter",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.3.sp
                                    ),
                                    color = TextWhitePrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Live rate status indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(LiveRateGreen)
                                )
                            }

                            Text(
                                text = "Updated: ${uiState.lastUpdatedFormatted}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightGreySecondary
                            )
                        }
                    }
                },
                actions = {
                    // Quick Refresh Rates button
                    IconButton(
                        onClick = { viewModel.refreshExchangeRates() },
                        enabled = !uiState.isRefreshing,
                        modifier = Modifier.testTag("refresh_rates_btn")
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OrangePrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Rates",
                                tint = OrangePrimary
                            )
                        }
                    }

                    // Conversion History
                    IconButton(
                        onClick = { viewModel.openSheet(ActiveSheet.History) },
                        modifier = Modifier.testTag("history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = TextWhitePrimary
                        )
                    }

                    // VIP Premium Crown Badge
                    IconButton(
                        onClick = { viewModel.openSheet(ActiveSheet.Premium) },
                        modifier = Modifier.testTag("premium_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = "VIP Premium",
                            tint = if (uiState.isPremium) PremiumGold else OrangePrimary
                        )
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = { isMenuOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextWhitePrimary
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuOpen,
                            onDismissRequest = { isMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("About Application") },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = OrangePrimary)
                                },
                                onClick = {
                                    isMenuOpen = false
                                    showAboutDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Own Rates (Custom)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = OrangePrimary)
                                },
                                onClick = {
                                    isMenuOpen = false
                                    viewModel.openSheet(ActiveSheet.CustomRates())
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    isMenuOpen = false
                                    viewModel.openSheet(ActiveSheet.Settings)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (uiState.isPremium) "VIP Membership Active" else "Upgrade to Premium (₦700/mo)",
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Diamond, contentDescription = null, tint = PremiumGold)
                                },
                                onClick = {
                                    isMenuOpen = false
                                    viewModel.openSheet(ActiveSheet.Premium)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalDeepBlack
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Conversion Display: ONLY TWO CURRENCIES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. BASE CURRENCY CARD
                CurrencyCardItem(
                    item = uiState.baseItem,
                    isBase = true,
                    expressionText = uiState.expression,
                    oppositeCode = uiState.targetCurrencyCode,
                    exchangeRate = uiState.rateBaseToTarget,
                    onClick = {
                        viewModel.openSheet(ActiveSheet.CurrencySelect(CurrencySelectionMode.BASE))
                    }
                )

                // SWAP BUTTON AFFORDANCE BETWEEN THE TWO CARDS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(2.dp, OrangePrimary, CircleShape)
                            .clickable { viewModel.swapBaseAndTarget() }
                            .testTag("swap_currencies_btn"),
                        color = CharcoalSecondaryBg,
                        shape = CircleShape,
                        tonalElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap Currencies",
                                tint = OrangePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // 2. TARGET CURRENCY CARD
                CurrencyCardItem(
                    item = uiState.targetItem,
                    isBase = false,
                    expressionText = uiState.targetItem?.formattedAmount ?: "0.00",
                    oppositeCode = uiState.baseCurrencyCode,
                    exchangeRate = uiState.rateBaseToTarget,
                    onClick = {
                        viewModel.openSheet(ActiveSheet.CurrencySelect(CurrencySelectionMode.TARGET))
                    }
                )
            }

            // CALCULATOR KEYPAD
            CalculatorKeypad(
                onKeyClick = { key, view ->
                    viewModel.onKeypadClick(key, view)
                }
            )

            // ADMOB BANNER (Suppressed for VIP)
            AdBannerView(
                isPremium = uiState.isPremium,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    }

    // Currency Selection Sheet
    when (val sheet = uiState.activeSheet) {
        is ActiveSheet.None -> Unit

        is ActiveSheet.CurrencySelect -> {
            val currentCode = if (sheet.mode == CurrencySelectionMode.BASE) {
                uiState.baseCurrencyCode
            } else {
                uiState.targetCurrencyCode
            }

            CurrencySelectionSheet(
                sheetState = sheetState,
                selectionMode = sheet.mode,
                currentSelectedCode = currentCode,
                favorites = uiState.favorites,
                onSelectCurrency = { selectedCode ->
                    viewModel.selectCurrency(selectedCode, sheet.mode)
                },
                onToggleFavorite = { code ->
                    viewModel.toggleFavorite(code)
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        is ActiveSheet.History -> {
            HistorySheet(
                sheetState = sheetState,
                historyList = historyList,
                onRestoreItem = { item ->
                    viewModel.restoreHistoryItem(item)
                },
                onDeleteItem = { id ->
                    viewModel.deleteHistoryItem(id)
                },
                onClearAll = {
                    viewModel.clearAllHistory()
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        is ActiveSheet.CustomRates -> {
            val currencyList = listOfNotNull(uiState.baseItem, uiState.targetItem)
            CustomRatesSheet(
                sheetState = sheetState,
                initialTargetCode = sheet.targetCode ?: uiState.targetCurrencyCode,
                currencies = currencyList,
                onSaveCustomRate = { code, rate ->
                    viewModel.setCustomRate(code, rate)
                },
                onResetCustomRate = { code ->
                    viewModel.resetCustomRate(code)
                },
                onResetAll = {
                    viewModel.resetAllCustomRates()
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        is ActiveSheet.Premium -> {
            PremiumSheet(
                sheetState = sheetState,
                isPremium = uiState.isPremium,
                monthlyPlan = monthlyPlan,
                yearlyPlan = yearlyPlan,
                onSubscribe = { activity, plan ->
                    viewModel.upgradeSubscription(activity, plan)
                },
                onRestorePurchases = { onComplete ->
                    viewModel.restorePurchases(onComplete)
                },
                onToggleTestTier = {
                    viewModel.testTogglePremium()
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        is ActiveSheet.Settings -> {
            SettingsSheet(
                sheetState = sheetState,
                themeMode = uiState.themeMode,
                decimalPrecision = uiState.decimalPrecision,
                numberFormat = uiState.numberFormat,
                hapticEnabled = uiState.hapticEnabled,
                soundEnabled = uiState.soundEnabled,
                onUpdateTheme = { viewModel.updateThemeMode(it) },
                onUpdatePrecision = { viewModel.updatePrecision(it) },
                onUpdateFormat = { viewModel.updateNumberFormat(it) },
                onUpdateHaptic = { viewModel.updateHaptic(it) },
                onUpdateSound = { viewModel.updateSound(it) },
                onResetAllCustomRates = { viewModel.resetAllCustomRates() },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        is ActiveSheet.CurrencyDetail -> {
            val currItem = if (sheet.code == uiState.baseCurrencyCode) uiState.baseItem else uiState.targetItem
            CurrencyDetailSheet(
                sheetState = sheetState,
                currencyCode = sheet.code,
                baseCurrencyCode = uiState.baseCurrencyCode,
                currencyItem = currItem,
                onSetAsBase = { code ->
                    viewModel.selectCurrency(code, CurrencySelectionMode.BASE)
                },
                onOpenCustomRate = { code ->
                    viewModel.openSheet(ActiveSheet.CustomRates(code))
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        is ActiveSheet.About -> {
            viewModel.closeSheet()
        }
    }

    // About Dialog with Official Logo
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text("Global Currency Converter", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Version 1.1", style = MaterialTheme.typography.bodySmall, color = OrangePrimary)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A clean, modern calculator-style universal currency converter with real-time live rates, offline caching, and instant two-way conversions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLightGreySecondary
                    )
                    Text(
                        text = "Supports 160+ world currencies, custom exchange rates, conversion history, and ad-free VIP memberships.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightGreySecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CharcoalSecondaryBg
        )
    }
}
