package com.globalcurrency.converter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.globalcurrency.converter.ui.components.CalculatorPad
import com.globalcurrency.converter.ui.components.CurrencyPickerButton
import com.globalcurrency.converter.ui.theme.*
import com.globalcurrency.converter.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    navController: NavController,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Currency Calculator", color = GoldPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(state.expression, color = OnDarkDisabled, style = MaterialTheme.typography.bodySmall)
            Text(
                text = state.display,
                style = MaterialTheme.typography.displaySmall,
                color = OnDarkBackground,
                fontWeight = FontWeight.Bold
            )
            if (state.convertedDisplay.isNotBlank()) {
                Text(state.convertedDisplay, color = GoldPrimary, style = MaterialTheme.typography.bodyLarge)
            }
            state.errorMessage?.let {
                Text(it, color = CrimsonAccent, style = MaterialTheme.typography.bodySmall)
            }

            CurrencyPickerButton(
                selectedCurrency = state.selectedCurrency,
                isPremium = state.isPremium,
                favorites = emptySet(),
                onCurrencySelected = viewModel::onCurrencySelected,
                modifier = Modifier.fillMaxWidth()
            )
            CurrencyPickerButton(
                selectedCurrency = state.convertToCurrency,
                isPremium = state.isPremium,
                favorites = emptySet(),
                onCurrencySelected = viewModel::onConvertCurrencySelected,
                modifier = Modifier.fillMaxWidth()
            )

            CalculatorPad(
                onDigit = viewModel::onDigit,
                onOperator = viewModel::onOperator,
                onEquals = viewModel::onEquals,
                onClear = viewModel::onClear,
                onBackspace = viewModel::onBackspace,
                onPlusMinus = viewModel::onPlusMinus,
                onPercent = viewModel::onPercent,
                onDecimal = viewModel::onDecimal
            )
        }
    }
}
