package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalDeepBlack
import com.example.ui.theme.KeypadMediumGrey
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun CalculatorKeypad(
    onKeyClick: (String, android.view.View) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CharcoalDeepBlack,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: C, ⌫, %, ÷
            KeypadRow(
                keys = listOf("C", "⌫", "%", "÷"),
                onKeyClick = { onKeyClick(it, view) }
            )

            // Row 2: 7, 8, 9, ×
            KeypadRow(
                keys = listOf("7", "8", "9", "×"),
                onKeyClick = { onKeyClick(it, view) }
            )

            // Row 3: 4, 5, 6, -
            KeypadRow(
                keys = listOf("4", "5", "6", "-"),
                onKeyClick = { onKeyClick(it, view) }
            )

            // Row 4: 1, 2, 3, +
            KeypadRow(
                keys = listOf("1", "2", "3", "+"),
                onKeyClick = { onKeyClick(it, view) }
            )

            // Row 5: ⇅, 0, ., =
            KeypadRow(
                keys = listOf("⇅", "0", ".", "="),
                onKeyClick = { onKeyClick(it, view) }
            )
        }
    }
}

@Composable
private fun KeypadRow(
    keys: List<String>,
    onKeyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (key in keys) {
            KeypadButton(
                key = key,
                onClick = { onKeyClick(key) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOperation = key in listOf("÷", "×", "-", "+", "=")
    val isSpecial = key in listOf("C", "⌫", "%", "⇅")

    val bgColor = when {
        isOperation -> OrangePrimary
        isSpecial -> Color(0xFF3E4251)
        else -> KeypadMediumGrey
    }

    val textColor = TextWhitePrimary

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() }
            .testTag("keypad_btn_$key"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            fontSize = if (key.length > 2) 18.sp else 22.sp,
            fontWeight = if (isOperation || key == "=") FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
