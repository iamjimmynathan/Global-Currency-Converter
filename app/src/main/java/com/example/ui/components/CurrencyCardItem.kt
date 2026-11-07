package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyItemUiState
import com.example.domain.CalculatorEngine
import com.example.ui.theme.CharcoalSecondaryBg
import com.example.ui.theme.CustomRateAmber
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.TextLightGreySecondary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun CurrencyCardItem(
    item: CurrencyItemUiState?,
    isBase: Boolean,
    expressionText: String,
    oppositeCode: String,
    exchangeRate: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (item == null) return

    val borderColor by animateColorAsState(
        targetValue = if (isBase) OrangePrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        label = "cardBorder"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isBase) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .testTag(if (isBase) "base_currency_card" else "target_currency_card"),
        color = CharcoalSecondaryBg,
        tonalElevation = if (isBase) 6.dp else 2.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Header Row: Flag + Currency Code + Tag + Subtitle Rate + Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Flag Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (isBase) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.info.flagEmoji,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Currency Code + FROM / TO Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.info.code,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isBase) OrangePrimary else TextWhitePrimary
                    )

                    Badge(
                        containerColor = if (isBase) OrangePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isBase) OrangePrimary else TextLightGreySecondary
                    ) {
                        Text(
                            text = if (isBase) "FROM" else "TO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    if (item.isCustomRate) {
                        Badge(
                            containerColor = CustomRateAmber.copy(alpha = 0.25f),
                            contentColor = CustomRateAmber
                        ) {
                            Text(
                                text = "CUSTOM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Rate subtitle or indicator on top right
                if (!isBase) {
                    Text(
                        text = "1 $oppositeCode = ${CalculatorEngine.formatExchangeRate(exchangeRate)} ${item.info.code}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLightGreySecondary.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = "Base Currency",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangePrimary.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select Currency",
                    tint = TextLightGreySecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dedicated Full-Width Amount Row with Auto-Resizing Font
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                val displayAmount = if (isBase) expressionText else item.formattedAmount
                AutoResizingAmountText(
                    text = displayAmount,
                    color = if (isBase) OrangePrimary else TextWhitePrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Footer Row: Full Currency Name & Hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.info.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLightGreySecondary.copy(alpha = 0.75f),
                    maxLines = 1
                )

                Text(
                    text = "Tap to change",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGreySecondary.copy(alpha = 0.45f)
                )
            }
        }
    }
}

/**
 * Automatically adjusts the font size based on the text length and available container width
 * to guarantee that the amount fits completely on a single line without clipping or truncation.
 */
@Composable
fun AutoResizingAmountText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 32.sp,
    minFontSize: TextUnit = 14.sp
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Compute well-calibrated starting font size from text length
        val initialSize = when {
            text.length <= 7 -> maxFontSize // e.g. "100.00" -> 32.sp
            text.length <= 11 -> 26.sp       // e.g. "134,810.63" -> 26.sp
            text.length <= 15 -> 22.sp       // e.g. "1,234,567.89" -> 22.sp
            text.length <= 19 -> 18.sp       // e.g. "123,456,789.1234" -> 18.sp
            text.length <= 23 -> 16.sp
            else -> minFontSize
        }

        var fontSize by remember(text, maxWidth) { mutableStateOf(initialSize) }
        var isMeasurementDone by remember(text, maxWidth) { mutableStateOf(false) }

        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            color = color,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.End,
            onTextLayout = { result ->
                if (result.didOverflowWidth && fontSize.value > minFontSize.value) {
                    fontSize = (fontSize.value * 0.90f).sp
                } else {
                    isMeasurementDone = true
                }
            },
            modifier = Modifier.alpha(if (isMeasurementDone) 1f else 0.99f)
        )
    }
}
