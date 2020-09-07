package com.example.currencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.currencyconverter.model.CurrencyData
import com.example.currencyconverter.retrofit.CurrencyPresenter
import com.example.currencyconverter.retrofit.ICurrencyView
import kotlinx.android.synthetic.main.fragment_convert.*

class MainActivity : AppCompatActivity(), ICurrencyView {

    private var baseCurrencyValue: Double = 1.0
    private var baseCurrency: String = "USD"
    private var convertCurrencyValue: Double? = null
    private var convertCurrency: String = "AED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        CurrencyPresenter(this).getDataFromAPI(baseCurrency)
    }

    override fun onDataCompleteFromAPI(currency: CurrencyData) {
        base_currency_value_converter.text = baseCurrencyValue.toString()
        base_currency_converter.text = currency.base
        convert_currency_value_converter.text = currency.rates.AED.toString()
        convert_currency_converter.text = convertCurrency
    }

    override fun onDataErrorFromAPI(throwable: Throwable) {
        Toast.makeText(this, "Error: $throwable", Toast.LENGTH_SHORT).show()
        error("error-------> $throwable")
    }


}