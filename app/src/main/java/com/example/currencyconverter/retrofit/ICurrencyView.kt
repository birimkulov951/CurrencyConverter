package com.example.currencyconverter.retrofit

import com.example.currencyconverter.model.CurrencyData

interface ICurrencyView {
    fun onDataCompleteFromAPI(currency: CurrencyData)
    fun onDataErrorFromAPI(throwable: Throwable)
}