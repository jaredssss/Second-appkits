package com.globalcurrency.converter

import com.globalcurrency.converter.data.model.ALL_CURRENCIES
import com.globalcurrency.converter.data.model.FREE_CURRENCIES
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyCatalogTest {

    @Test
    fun freeCurrenciesAreSubsetOfAllCurrencies() {
        assertTrue(FREE_CURRENCIES.isNotEmpty())
        assertTrue(FREE_CURRENCIES.all { free -> ALL_CURRENCIES.any { it.code == free.code } })
    }

    @Test
    fun allCurrenciesContainMajorCodes() {
        val codes = ALL_CURRENCIES.map { it.code }.toSet()
        assertTrue("USD" in codes)
        assertTrue("EUR" in codes)
        assertTrue("JPY" in codes)
        assertTrue("GBP" in codes)
    }
}
