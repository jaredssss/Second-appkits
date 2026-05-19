package com.globalcurrency.converter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcurrency.converter.data.model.*
import com.globalcurrency.converter.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RatesUiState(
    val baseCurrencyCode: String = "USD",
    val searchQuery: String      = "",
    val rates: Map<String, Double> = emptyMap(),
    val filteredCurrencies: List<Currency> = emptyList(),
    val isLoading: Boolean       = false,
    val errorMessage: String?    = null,
    val isPremium: Boolean       = false,
    val favorites: Set<String>   = emptySet(),
    val lastUpdated: String      = ""
)

@HiltViewModel
class RatesViewModel @Inject constructor(
    private val repository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RatesUiState())
    val uiState: StateFlow<RatesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.premiumStatus.collect { status ->
                _uiState.update { it.copy(isPremium = status.isPremium) }
                applyFilter()
            }
        }
        viewModelScope.launch {
            repository.favoriteCurrencies.collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
        loadRates()
    }

    fun onBaseCurrencyChanged(code: String) {
        _uiState.update { it.copy(baseCurrencyCode = code) }
        loadRates()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    fun refresh() { loadRates() }

    fun toggleFavorite(code: String) {
        viewModelScope.launch { repository.toggleFavorite(code) }
    }

    private fun loadRates() {
        val base = _uiState.value.baseCurrencyCode
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getRates(base)
                .onSuccess { rates ->
                    _uiState.update { it.copy(rates = rates, isLoading = false, lastUpdated = "Updated") }
                    applyFilter()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun applyFilter() {
        val state = _uiState.value
        val availableCurrencies = if (state.isPremium) ALL_CURRENCIES else FREE_CURRENCIES
        val query = state.searchQuery.trim().lowercase()
        val filtered = if (query.isEmpty()) {
            availableCurrencies
        } else {
            availableCurrencies.filter { c ->
                c.code.lowercase().contains(query) || c.name.lowercase().contains(query)
            }
        }
        // Put favorites first, then sort by code
        val sorted = filtered.sortedWith(
            compareByDescending<Currency> { it.code in state.favorites }
                .thenBy { it.code }
        )
        _uiState.update { it.copy(filteredCurrencies = sorted) }
    }
}
