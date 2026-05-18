package com.globalcurrency.converter.data.api

import com.globalcurrency.converter.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Base URL already includes the API key path segment as part of the path,
    // but ExchangeRate-API v6 puts the key BEFORE /latest/... so we embed it here.
    private val baseUrl: String
        get() = "${BuildConfig.EXCHANGE_RATE_BASE_URL}${BuildConfig.EXCHANGE_RATE_API_KEY}/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    val exchangeRateApi: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}
