package com.example.currencyconverter.model

data class CurrencyData(
    val base: String,
    val rates: Rates,
    val updated: Int,
    val valid: Boolean
)