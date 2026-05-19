package com.globalcurrency.converter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globalcurrency.converter.data.model.ALL_CURRENCIES
import com.globalcurrency.converter.ui.components.CurrencyPickerButton
import com.globalcurrency.converter.ui.theme.*
import com.globalcurrency.converter.viewmodel.ConverterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fromCurrency = ALL_CURRENCIES.find { it.code == state.fromCurrencyCode }
    val toCurrency = ALL_CURRENCIES.find { it.code == state.toCurrencyCode }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Global Converter", color = GoldPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Convert any amount between global currencies.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnDarkSurface
            )

            CurrencyPickerButton(
                selectedCurrency = fromCurrency,
                isPremium = state.isPremium,
                favorites = state.favorites,
                onCurrencySelected = viewModel::onFromCurrencyChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::swapCurrencies) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap currencies", tint = GoldPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text("Swap", color = OnDarkSurface)
            }

            CurrencyPickerButton(
                selectedCurrency = toCurrency,
                isPremium = state.isPremium,
                favorites = state.favorites,
                onCurrencySelected = viewModel::onToCurrencyChanged,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.inputAmount,
                onValueChange = viewModel::onAmountChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount", color = OnDarkSurface) },
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Converted", color = OnDarkSurface, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${toCurrency?.symbol ?: ""} ${state.convertedAmount.ifBlank { "0.00" }} ${toCurrency?.code.orEmpty()}",
                        color = GoldPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1 ${state.fromCurrencyCode} = ${state.exchangeRate} ${state.toCurrencyCode}",
                        color = OnDarkSurface,
                        style = MaterialTheme.typography.bodySmall
                    )
                    state.errorMessage?.let {
                        Text(it, color = CrimsonAccent, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val favorite = state.toCurrencyCode in state.favorites
                IconButton(onClick = { viewModel.toggleFavorite(state.toCurrencyCode) }) {
                    Icon(
                        imageVector = if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (favorite) GoldPrimary else OnDarkDisabled
                    )
                }
                Text("Favorite ${state.toCurrencyCode}", color = OnDarkSurface)
            }
        }
    }
}
