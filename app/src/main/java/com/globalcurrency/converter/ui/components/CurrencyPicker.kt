package com.globalcurrency.converter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.globalcurrency.converter.data.model.ALL_CURRENCIES
import com.globalcurrency.converter.data.model.Currency
import com.globalcurrency.converter.data.model.FREE_CURRENCIES
import com.globalcurrency.converter.ui.theme.*

private val PREMIUM_ONLY_COUNT = ALL_CURRENCIES.count { it.isPremiumOnly }

/**
 * A row that displays the currently selected currency and opens the picker dialog on tap.
 */
@Composable
fun CurrencyPickerButton(
    selectedCurrency: Currency?,
    isPremium: Boolean,
    favorites: Set<String>,
    onCurrencySelected: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { showDialog = true },
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedCurrency?.flagEmoji ?: "🌍",
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = selectedCurrency?.code ?: "---",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = selectedCurrency?.name ?: "Select currency",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnDarkDisabled,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Choose currency",
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showDialog) {
        CurrencyPickerDialog(
            isPremium  = isPremium,
            favorites  = favorites,
            onSelected = { currency ->
                onCurrencySelected(currency)
                showDialog = false
            },
            onDismiss  = { showDialog = false }
        )
    }
}

@Composable
fun CurrencyPickerDialog(
    isPremium: Boolean,
    favorites: Set<String>,
    onSelected: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allCurrencies = if (isPremium) ALL_CURRENCIES else FREE_CURRENCIES

    val filtered = remember(query, allCurrencies) {
        val q = query.trim().lowercase()
        val list = if (q.isEmpty()) allCurrencies
        else allCurrencies.filter {
            it.code.lowercase().contains(q) || it.name.lowercase().contains(q)
        }
        list.sortedWith(
            compareByDescending<Currency> { it.code in favorites }.thenBy { it.code }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ── Header ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Currency",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = OnDarkSurface)
                    }
                }
                if (!isPremium) {
                    Text(
                        text = "🔒 $PREMIUM_ONLY_COUNT more currencies with Premium",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // ── Search field ──────────────────────────────────────────
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search by name or code…", color = OnDarkDisabled) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Search", tint = GoldPrimary)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = OnDarkSurface)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = GoldPrimary,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor     = OnDarkBackground,
                        unfocusedTextColor   = OnDarkBackground,
                        cursorColor          = GoldPrimary,
                        focusedContainerColor   = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // ── Currency list ─────────────────────────────────────────
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(filtered, key = { it.code }) { currency ->
                        CurrencyListItem(
                            currency   = currency,
                            isFavorite = currency.code in favorites,
                            onSelected = { onSelected(currency) }
                        )
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text = "No currencies found for \"$query\"",
                                color = OnDarkDisabled,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyListItem(
    currency: Currency,
    isFavorite: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelected)
            .background(DarkSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = currency.flagEmoji, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.code,
                style = MaterialTheme.typography.labelLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currency.name,
                style = MaterialTheme.typography.bodySmall,
                color = OnDarkSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = currency.symbol,
            style = MaterialTheme.typography.titleSmall,
            color = OnDarkDisabled,
            modifier = Modifier.padding(end = 8.dp)
        )
        if (isFavorite) {
            Icon(Icons.Filled.Star, "Favorite", tint = GoldPrimary, modifier = Modifier.size(18.dp))
        }
    }
}
