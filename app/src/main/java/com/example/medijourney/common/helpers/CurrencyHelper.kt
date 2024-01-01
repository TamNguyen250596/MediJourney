package com.example.medijourney.common.helpers

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyHelper {

    fun getPrice(price: Double, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val currency = Currency.getInstance(currencyCode)
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = currency
        return formatter.format(price)
    }
}