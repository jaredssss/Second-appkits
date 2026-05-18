package com.globalcurrency.converter

import android.app.Application
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GlobalCurrencyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Stripe with your publishable key
        // Replace BuildConfig.STRIPE_PUBLISHABLE_KEY with your actual key from dashboard.stripe.com
        PaymentConfiguration.init(
            applicationContext,
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        )
    }
}
