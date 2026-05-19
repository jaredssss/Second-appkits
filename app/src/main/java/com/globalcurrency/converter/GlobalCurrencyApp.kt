package com.globalcurrency.converter

import android.app.Application
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GlobalCurrencyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val stripeKey = BuildConfig.STRIPE_PUBLISHABLE_KEY.trim()
        if (stripeKey.isNotEmpty()) {
            PaymentConfiguration.init(applicationContext, stripeKey)
        }
    }
}
