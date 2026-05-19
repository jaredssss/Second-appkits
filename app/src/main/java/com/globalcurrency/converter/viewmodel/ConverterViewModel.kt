package com.globalcurrency.converter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcurrency.converter.data.model.*
import com.globalcurrency.converter.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConverterUiState(
    val fromCurrencyCode: String = "USD",
    val toCurrencyCode: String   = "EUR",
    val inputAmount: String      = "1",
    val convertedAmount: String  = "",
    val exchangeRate: Double     = 0.0,
    val isLoading: Boolean       = false,
    val errorMessage: String?    = null,
    val lastUpdated: String      = "",
    val isPremium: Boolean       = false,
    val favorites: Set<String>   = emptySet(),
    val history: List<ConversionHistoryEntry> = emptyList(),
    val allRates: Map<String, Double> = emptyMap()  // from-base rates for rates screen
)

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val repository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null

    init {
        // Observe premium status
        viewModelScope.launch {
            repository.premiumStatus.collect { status ->
                _uiState.update { it.copy(isPremium = status.isPremium) }
            }
        }
        // Observe favorites
        viewModelScope.launch {
            repository.favoriteCurrencies.collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
        // Observe defaults
        viewModelScope.launch {
            combine(
                repository.defaultFromCurrency,
                repository.defaultToCurrency
            ) { from, to -> Pair(from, to) }.collect { (from, to) ->
                _uiState.update { it.copy(fromCurrencyCode = from, toCurrencyCode = to) }
                performConversion()
            }
        }
        // Observe history
        viewModelScope.launch {
            repository.getConversionHistory().collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    fun onAmountChanged(value: String) {
        _uiState.update { it.copy(inputAmount = value, errorMessage = null) }
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            performConversion()
        }
    }

    fun onFromCurrencyChanged(code: String) {
        _uiState.update { it.copy(fromCurrencyCode = code) }
        persistDefaultCurrencies()
        performConversion()
    }

    fun onToCurrencyChanged(code: String) {
        _uiState.update { it.copy(toCurrencyCode = code) }
        persistDefaultCurrencies()
        performConversion()
    }

    fun swapCurrencies() {
        _uiState.update {
            it.copy(
                fromCurrencyCode = it.toCurrencyCode,
                toCurrencyCode   = it.fromCurrencyCode,
                inputAmount      = if (it.convertedAmount.isNotBlank()) it.convertedAmount else it.inputAmount
            )
        }
        persistDefaultCurrencies()
        performConversion()
    }

    fun performConversion() {
        val state = _uiState.value
        val amount = state.inputAmount.toDoubleOrNull()
        if (amount == null) {
            _uiState.update {
                it.copy(
                    convertedAmount = "",
                    exchangeRate = 0.0,
                    errorMessage = if (state.inputAmount.isBlank()) null else "Enter a valid amount."
                )
            }
            return
        }
        if (amount <= 0) {
            _uiState.update {
                it.copy(
                    convertedAmount = "0.00",
                    exchangeRate = 0.0,
                    errorMessage = "Amount must be greater than 0."
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.convert(state.fromCurrencyCode, state.toCurrencyCode, amount)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading       = false,
                            convertedAmount = formatAmount(result.toAmount),
                            exchangeRate    = result.rate,
                            lastUpdated     = "Rate updated"
                        )
                    }
                    // Also load all rates for the rates screen
                    loadAllRates(state.fromCurrencyCode)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }

    private fun loadAllRates(base: String) {
        viewModelScope.launch {
            repository.getRates(base).onSuccess { rates ->
                _uiState.update { it.copy(allRates = rates) }
            }
        }
    }

    fun toggleFavorite(code: String) {
        viewModelScope.launch { repository.toggleFavorite(code) }
    }

    fun refresh() {
        performConversion()
    }

    private fun persistDefaultCurrencies() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.setDefaultCurrencies(state.fromCurrencyCode, state.toCurrencyCode)
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount >= 1000) {
            "%.2f".format(amount)
        } else if (amount >= 1) {
            "%.4f".format(amount)
        } else {
            "%.6f".format(amount)
        }
    }
}
