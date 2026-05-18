package com.globalcurrency.converter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcurrency.converter.ui.theme.*

data class CalcKey(
    val label: String,
    val type: KeyType
)

enum class KeyType { DIGIT, OPERATOR, EQUALS, CLEAR, BACKSPACE, PLUS_MINUS, PERCENT, DECIMAL }

val CALCULATOR_KEYS = listOf(
    listOf(
        CalcKey("AC",  KeyType.CLEAR),
        CalcKey("+/-", KeyType.PLUS_MINUS),
        CalcKey("%",   KeyType.PERCENT),
        CalcKey("÷",   KeyType.OPERATOR)
    ),
    listOf(
        CalcKey("7", KeyType.DIGIT),
        CalcKey("8", KeyType.DIGIT),
        CalcKey("9", KeyType.DIGIT),
        CalcKey("×", KeyType.OPERATOR)
    ),
    listOf(
        CalcKey("4", KeyType.DIGIT),
        CalcKey("5", KeyType.DIGIT),
        CalcKey("6", KeyType.DIGIT),
        CalcKey("-", KeyType.OPERATOR)
    ),
    listOf(
        CalcKey("1", KeyType.DIGIT),
        CalcKey("2", KeyType.DIGIT),
        CalcKey("3", KeyType.DIGIT),
        CalcKey("+", KeyType.OPERATOR)
    ),
    listOf(
        CalcKey("⌫",  KeyType.BACKSPACE),
        CalcKey("0",  KeyType.DIGIT),
        CalcKey(".",  KeyType.DECIMAL),
        CalcKey("=",  KeyType.EQUALS)
    )
)

@Composable
fun CalculatorPad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onPlusMinus: () -> Unit,
    onPercent: () -> Unit,
    onDecimal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CALCULATOR_KEYS.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    CalcButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key.type) {
                                KeyType.DIGIT      -> onDigit(key.label)
                                KeyType.OPERATOR   -> onOperator(key.label)
                                KeyType.EQUALS     -> onEquals()
                                KeyType.CLEAR      -> onClear()
                                KeyType.BACKSPACE  -> onBackspace()
                                KeyType.PLUS_MINUS -> onPlusMinus()
                                KeyType.PERCENT    -> onPercent()
                                KeyType.DECIMAL    -> onDecimal()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    key: CalcKey,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when (key.type) {
        KeyType.EQUALS                 -> GoldPrimary
        KeyType.OPERATOR               -> DarkSurfaceVariant
        KeyType.CLEAR, KeyType.PLUS_MINUS, KeyType.PERCENT -> Color(0xFF2A2A2A)
        KeyType.BACKSPACE              -> Color(0xFF2A1010)
        else                           -> DarkSurface
    }
    val textColor = when (key.type) {
        KeyType.EQUALS   -> OnDarkPrimary
        KeyType.OPERATOR -> GoldPrimary
        KeyType.CLEAR, KeyType.PLUS_MINUS, KeyType.PERCENT -> SkyAccent
        KeyType.BACKSPACE -> CrimsonAccent
        else             -> OnDarkBackground
    }

    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = key.label,
            fontSize   = if (key.label.length > 2) 18.sp else 24.sp,
            fontWeight = FontWeight.SemiBold,
            color      = textColor
        )
    }
}
