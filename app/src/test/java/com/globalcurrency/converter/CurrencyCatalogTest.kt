package com.globalcurrency.converter

import com.globalcurrency.converter.data.model.ALL_CURRENCIES
import com.globalcurrency.converter.data.model.FREE_CURRENCIES
import org.junit.Assert.assertEquals
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

    @Test
    fun currencyCodesAreUnique() {
        val codes = ALL_CURRENCIES.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun premiumSetIsNonEmptyAndNotInFreeList() {
        val premiumCodes = ALL_CURRENCIES.filter { it.isPremiumOnly }.map { it.code }.toSet()
        val freeCodes = FREE_CURRENCIES.map { it.code }.toSet()
        assertTrue(premiumCodes.isNotEmpty())
        assertTrue(premiumCodes.intersect(freeCodes).isEmpty())
    }
}
