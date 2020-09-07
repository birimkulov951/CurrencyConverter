package com.example.currencyconverter.retrofit

import android.content.Context
import com.example.currencyconverter.model.CurrencyData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrencyPresenter(context: Context) {
    val currencyView = context as ICurrencyView

    fun getDataFromAPI(baseCurrency: String) {
        RetrofitService.create()
            .getApi(baseCurrency)
            .enqueue(object: Callback<CurrencyData> {

                override fun onFailure(call: Call<CurrencyData>, t: Throwable) {
                    currencyView.onDataErrorFromAPI(t)
                }

                override fun onResponse(call: Call<CurrencyData>, response: Response<CurrencyData>) {
                    currencyView.onDataCompleteFromAPI(response.body() as CurrencyData)
                }

            })
    }
}