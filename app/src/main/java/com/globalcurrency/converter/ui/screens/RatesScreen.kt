package com.globalcurrency.converter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.globalcurrency.converter.ui.components.CurrencyPickerButton
import com.globalcurrency.converter.ui.theme.*
import com.globalcurrency.converter.viewmodel.RatesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesScreen(
    navController: NavController,
    viewModel: RatesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Global Rates", color = GoldPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, "Refresh rates", tint = GoldPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CurrencyPickerButton(
                selectedCurrency = com.globalcurrency.converter.data.model.ALL_CURRENCIES.find { it.code == state.baseCurrencyCode },
                isPremium = state.isPremium,
                favorites = state.favorites,
                onCurrencySelected = viewModel::onBaseCurrencyChanged,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search currency", color = OnDarkDisabled) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = GoldPrimary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnDarkBackground,
                    unfocusedTextColor = OnDarkBackground,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = DarkOutline,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = GoldPrimary,
                    trackColor = DarkSurfaceVariant
                )
            }

            state.errorMessage?.let {
                Text(it, color = CrimsonAccent, style = MaterialTheme.typography.bodySmall)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.filteredCurrencies, key = { it.code }) { currency ->
                    val rate = state.rates[currency.code]
                    val isFavorite = currency.code in state.favorites
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleFavorite(currency.code) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currency.flagEmoji)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${currency.code} • ${currency.name}", color = OnDarkBackground)
                            Text(
                                text = rate?.toString() ?: "No rate",
                                color = OnDarkSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) GoldPrimary else OnDarkDisabled
                        )
                    }
                    HorizontalDivider(color = DarkOutline)
                }
            }
        }
    }
}
