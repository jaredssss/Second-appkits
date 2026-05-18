package com.globalcurrency.converter.data.api

import com.globalcurrency.converter.data.model.ExchangeRateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * ExchangeRate-API v6 (https://exchangerate-api.com)
 *
 * Free plan:  1,500 requests / month, 161 currencies, updates every 24 h
 * Paid plans: up to every 1 min, 170+ currencies, historical data
 *
 * Base URL:  https://v6.exchangerate-api.com/v6/{API_KEY}/
 */
interface ExchangeRateApi {

    /** Fetch all exchange rates relative to [baseCurrency] (e.g. "USD") */
    @GET("latest/{baseCurrency}")
    suspend fun getLatestRates(
        @Path("baseCurrency") baseCurrency: String
    ): Response<ExchangeRateResponse>
}
