package com.globalcurrency.converter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcurrency.converter.data.model.ALL_CURRENCIES
import com.globalcurrency.converter.data.model.Currency
import com.globalcurrency.converter.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject

data class CalculatorUiState(
    val display: String       = "0",
    val expression: String    = "",
    val selectedCurrency: Currency? = null,
    val convertToCurrency: Currency? = null,
    val convertedDisplay: String = "",
    val exchangeRate: Double  = 0.0,
    val isPremium: Boolean    = false,
    val isLoading: Boolean    = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var operand1: BigDecimal? = null
    private var operator: String? = null
    private var shouldReset = false

    init {
        viewModelScope.launch {
            repository.premiumStatus.collect { status ->
                _uiState.update { it.copy(isPremium = status.isPremium) }
            }
        }
        // Default currencies
        _uiState.update {
            it.copy(
                selectedCurrency  = ALL_CURRENCIES.find { c -> c.code == "USD" },
                convertToCurrency = ALL_CURRENCIES.find { c -> c.code == "EUR" }
            )
        }
    }

    fun onDigit(digit: String) {
        val current = _uiState.value.display
        if (shouldReset) {
            _uiState.update { it.copy(display = digit) }
            shouldReset = false
        } else {
            val newDisplay = if (current == "0") digit else current + digit
            if (newDisplay.length <= 15) {
                _uiState.update { it.copy(display = newDisplay) }
            }
        }
        triggerConversion()
    }

    fun onDecimal() {
        val current = _uiState.value.display
        if (shouldReset) {
            _uiState.update { it.copy(display = "0.") }
            shouldReset = false
        } else if (!current.contains(".")) {
            _uiState.update { it.copy(display = "$current.") }
        }
    }

    fun onOperator(op: String) {
        val current = _uiState.value.display.toBigDecimalOrNull() ?: return
        if (operand1 != null && operator != null && !shouldReset) {
            calculateResult()
            operand1 = _uiState.value.display.toBigDecimalOrNull()
        } else {
            operand1 = current
        }
        operator = op
        shouldReset = true
        _uiState.update { it.copy(expression = "${formatBig(operand1!!)} $op") }
    }

    fun onEquals() {
        if (operand1 == null || operator == null) return
        calculateResult()
        operand1 = null
        operator = null
        _uiState.update { it.copy(expression = "") }
    }

    fun onClear() {
        operand1 = null
        operator = null
        shouldReset = false
        _uiState.update { it.copy(display = "0", expression = "", convertedDisplay = "", exchangeRate = 0.0) }
    }

    fun onBackspace() {
        val current = _uiState.value.display
        val newDisplay = when {
            current.length <= 1 || (current.startsWith("-") && current.length == 2) -> "0"
            else -> current.dropLast(1)
        }
        _uiState.update { it.copy(display = newDisplay) }
        triggerConversion()
    }

    fun onPlusMinus() {
        val current = _uiState.value.display
        val new = if (current.startsWith("-")) current.drop(1) else "-$current"
        _uiState.update { it.copy(display = new) }
        triggerConversion()
    }

    fun onPercent() {
        val value = _uiState.value.display.toBigDecimalOrNull() ?: return
        _uiState.update { it.copy(display = formatBig(value.divide(BigDecimal(100))) ) }
        triggerConversion()
    }

    fun onCurrencySelected(currency: Currency) {
        _uiState.update { it.copy(selectedCurrency = currency) }
        triggerConversion()
    }

    fun onConvertCurrencySelected(currency: Currency) {
        _uiState.update { it.copy(convertToCurrency = currency) }
        triggerConversion()
    }

    private fun calculateResult() {
        val a = operand1 ?: return
        val b = _uiState.value.display.toBigDecimalOrNull() ?: return
        val expr = "${formatBig(a)} $operator ${formatBig(b)}"
        try {
            val result = when (operator) {
                "+"  -> a.add(b)
                "-"  -> a.subtract(b)
                "×"  -> a.multiply(b)
                "÷"  -> if (b.compareTo(BigDecimal.ZERO) == 0)
                    throw ArithmeticException("Division by zero")
                    else a.divide(b, MathContext.DECIMAL128)
                "%"  -> a.multiply(b).divide(BigDecimal(100))
                else -> b
            }
            _uiState.update { it.copy(display = formatBig(result), expression = "$expr =") }
            shouldReset = true
            triggerConversion()
        } catch (e: ArithmeticException) {
            _uiState.update { it.copy(display = "Error", expression = expr, errorMessage = "Division by zero") }
            shouldReset = true
        }
    }

    private fun triggerConversion() {
        val state = _uiState.value
        val fromCurrency = state.selectedCurrency ?: return
        val toCurrency = state.convertToCurrency ?: return
        val amount = state.display.toBigDecimalOrNull()?.toDouble() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getRates(fromCurrency.code).onSuccess { rates ->
                val rate = rates[toCurrency.code] ?: 1.0
                val converted = amount * rate
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        exchangeRate = rate,
                        convertedDisplay = "≈ ${toCurrency.symbol} ${formatConverted(converted)} ${toCurrency.code}"
                    )
                }
            }.onFailure {
                _uiState.update { s -> s.copy(isLoading = false) }
            }
        }
    }

    private fun formatBig(value: BigDecimal): String {
        val plain = value.stripTrailingZeros().toPlainString()
        return if (plain.contains(".") && plain.substringAfter(".").length > 8)
            "%.8f".format(value.toDouble()).trimEnd('0').trimEnd('.')
        else plain
    }

    private fun formatConverted(value: Double): String =
        if (value >= 1000) "%.2f".format(value)
        else if (value >= 1) "%.4f".format(value)
        else "%.6f".format(value)
}
