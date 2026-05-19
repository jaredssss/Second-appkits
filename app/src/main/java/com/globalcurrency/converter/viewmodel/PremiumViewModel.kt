package com.globalcurrency.converter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcurrency.converter.data.model.PremiumStatus
import com.globalcurrency.converter.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class PremiumUiState(
    val premiumStatus: PremiumStatus = PremiumStatus(false),
    val isLoading: Boolean           = false,
    val errorMessage: String?        = null,
    val successMessage: String?      = null,
    val showPaymentSheet: Boolean    = false,
    val userEmail: String            = ""
)

private val thirtyDayDurationMs = TimeUnit.DAYS.toMillis(30)

/**
 * Manages the premium subscription flow.
 *
 * Architecture for sideloaded APK payments (no Google Play Billing):
 * ─────────────────────────────────────────────────────────────────
 * 1. User taps "Subscribe $5/month"
 * 2. App calls your backend → backend creates a Stripe PaymentIntent / SetupIntent
 * 3. Stripe PaymentSheet is presented to the user
 * 4. On success → backend webhook activates subscription
 * 5. App polls backend to confirm → stores isPremium = true in DataStore
 *
 * REQUIRED SETUP:
 *  • Set STRIPE_PUBLISHABLE_KEY and BACKEND_BASE_URL in local.properties (or env vars)
 *  • Implement the backend endpoint: POST /create-payment-intent
 *    Returns: { clientSecret: "pi_xxx_secret_xxx", ephemeralKey: "...", customerId: "..." }
 *  • Implement Stripe webhook to set subscription active
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val repository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.premiumStatus.collect { status ->
                _uiState.update { it.copy(premiumStatus = status) }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(userEmail = email) }
    }

    /**
     * Initiates the subscription purchase flow.
     * In a full production build this would call your backend to get a
     * Stripe PaymentSheet configuration, then launch the sheet.
     * For now it shows a demo confirmation (replace with real backend call).
     */
    fun startSubscription() {
        val email = _uiState.value.userEmail
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address to subscribe.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            // ── Production: replace this block with a real backend call ──────
            // val response = backendApi.createPaymentIntent(email, "usd", 500) // 500 cents = $5
            // Launch Stripe PaymentSheet with response.clientSecret
            // On success, call activatePremium(email, expiryMs)
            // ─────────────────────────────────────────────────────────────────

            // Demo mode: simulate backend call (remove when backend is ready)
            kotlinx.coroutines.delay(1500)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "⚙️ Payment backend not yet configured.\n\n" +
                        "To enable real payments:\n" +
                        "1. Deploy the backend server\n" +
                        "2. Set your Stripe keys in local.properties or env vars\n" +
                        "3. Replace the demo block in PremiumViewModel.startSubscription()"
                )
            }
        }
    }

    /** Called after Stripe confirms payment successfully. */
    fun activatePremium(email: String) {
        val expiryMs = System.currentTimeMillis() + thirtyDayDurationMs
        viewModelScope.launch {
            repository.setPremiumStatus(true, expiryMs, email)
            _uiState.update {
                it.copy(
                    isLoading      = false,
                    successMessage = "🎉 Premium activated! Welcome to Global Currency Converter Pro.",
                    errorMessage   = null
                )
            }
        }
    }

    /** For testing / demo: instantly activate premium without payment */
    fun activatePremiumDemo() {
        activatePremium("demo@globalcurrency.app")
    }

    fun cancelPremium() {
        viewModelScope.launch {
            repository.setPremiumStatus(false, null, null)
            _uiState.update { it.copy(successMessage = "Premium subscription cancelled.") }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
