package com.globalcurrency.converter.util

fun formatCurrencyAmount(value: Double): String =
    if (value >= 1000) "%.2f".format(value)
    else if (value >= 1) "%.4f".format(value)
    else "%.6f".format(value)
