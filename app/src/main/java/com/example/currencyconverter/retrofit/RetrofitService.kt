package com.example.currencyconverter.retrofit

import com.example.currencyconverter.model.CurrencyData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {

    @GET("api/v1/rates?key=NnIG1M7JhhoDi8iicVWjBKkkkowfLAZSEA4A")
    fun getApi(@Query("base") baseCurrency: String) : Call<CurrencyData>

    companion object {
        fun create(): RetrofitService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://currencyapi.net/")
                .build()
            return retrofit.create(RetrofitService::class.java)
        }
    }
}