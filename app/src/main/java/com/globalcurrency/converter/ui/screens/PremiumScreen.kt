package com.globalcurrency.converter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globalcurrency.converter.viewmodel.PremiumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? android.app.Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium ($5/month)") }
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
            Text(
                "Free users are anonymous (no account required). Paid users use email login for restore purchase.",
                style = MaterialTheme.typography.bodySmall
            )

            Text("Feature availability", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FeatureLine("Dark mode only design", "Functional now")
            FeatureLine("Global converter + calculator", "Functional now")
            FeatureLine("Major currencies free", "Functional now")
            FeatureLine("All currencies + advanced tools", "Premium gated in app")
            FeatureLine("Real $5 monthly payment", "Requires backend + Stripe keys configuration")
            FeatureLine("Sideload APK payments", "Functional with Stripe (not Play Billing) after backend setup")

            OutlinedTextField(
                value = state.userEmail,
                onValueChange = viewModel::onEmailChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Email for premium login / restore") }
            )

            Button(
                onClick = { if (activity != null) viewModel.startSubscription(activity) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Processing..." else "Subscribe - $5/month")
            }

            OutlinedButton(
                onClick = viewModel::activatePremiumDemo,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Demo activate premium") }

            if (state.premiumStatus.isPremium) {
                AssistChip(onClick = {}, label = { Text("Premium Active") })
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            state.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FeatureLine(name: String, status: String) {
    Column {
        Text("• $name", style = MaterialTheme.typography.bodyMedium)
        Text("  ↳ $status", style = MaterialTheme.typography.bodySmall)
    }
}
