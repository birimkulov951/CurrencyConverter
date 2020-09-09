package com.example.currencyconverter.model

data class CurrencyData(
    val valid: Boolean,
    val updated: Int,
    val base: String,
    val rates: Rates
)