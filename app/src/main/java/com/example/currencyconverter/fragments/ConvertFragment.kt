package com.example.currencyconverter.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.example.currencyconverter.R
import com.example.currencyconverter.model.CurrencyData
import com.example.currencyconverter.retrofit.RetrofitInstance
import com.example.currencyconverter.retrofit.RetrofitService
import kotlinx.android.synthetic.main.fragment_convert.*
import retrofit2.Response

class ConvertFragment : Fragment() {

    private val TAG = "ConvertFragment"

    private lateinit var retService: RetrofitService

    // Main vars
    private var baseCurrency: String = "USD"
    private var baseCurrencyValue: Double? = 1.0
    private var convertCurrency: String = "KGS"
    private var convertCurrencyValue: Double? = 1.0

    // Helpers
    private var isSwitchButtonPressed: Boolean = false
    private var baseCurrencyValueWhenSwitchButtonPressed: Double? = null
    private var convertCurrencyValueWhenSwitchButtonPressed: Double? = null
    private var placeholderCurrency: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_convert, container, false)

        var base_currency_converter: TextView = view.findViewById(R.id.base_currency_converter)
        var base_currency_value_converter: TextView = view.findViewById(R.id.base_currency_value_converter)
        var convert_currency_converter: TextView = view.findViewById(R.id.convert_currency_converter)
        var convert_currency_value_converter: TextView = view.findViewById(R.id.convert_currency_value_converter)
        val switch_button_converter: ImageButton = view.findViewById(R.id.switch_button_converter)
        //val progress_bar_converter: ProgressBar = view.findViewById(R.id.progress_bar_converter)

        base_currency_converter.visibility = View.INVISIBLE
        base_currency_value_converter.visibility = View.INVISIBLE
        convert_currency_converter.visibility = View.INVISIBLE
        convert_currency_value_converter.visibility = View.INVISIBLE
        switch_button_converter.visibility = View.INVISIBLE

        getDataFromApi()

        switch_button_converter.setOnClickListener{
            isSwitchButtonPressed = true
            getDataFromApi()

            // Switch currencies
            placeholderCurrency = baseCurrency
            baseCurrency = convertCurrency
            convertCurrency = placeholderCurrency!!

            if (isSwitchButtonPressed) {

                baseCurrencyValueWhenSwitchButtonPressed = baseCurrencyValue
                convertCurrencyValueWhenSwitchButtonPressed = convertCurrencyValue

                convertCurrencyValue = baseCurrencyValue!!.div(convertCurrencyValue!!)
                convert_currency_value_converter.text = convertCurrencyValue.toString()

            } else {

                isSwitchButtonPressed = false

                baseCurrencyValue= baseCurrencyValueWhenSwitchButtonPressed
                convertCurrencyValue = convertCurrencyValueWhenSwitchButtonPressed
                convert_currency_value_converter.text = convertCurrencyValue.toString()


            }

            Log.d(TAG, "onCreateView: baseCurrencyValue: " + baseCurrencyValue)
            Log.d(TAG, "onCreateView: convertCurrencyValue: " + convertCurrencyValue)

        }

        return view
    }

    // Gets row data from API.
    private fun getDataFromApi() {

        viewInVisible()

        retService = RetrofitInstance.getRetrofitInstance().create(RetrofitService::class.java)

        val responseLiveData: LiveData<Response<CurrencyData>> = liveData {
            val response = retService.getRates()
            emit(response)
        }

        responseLiveData.observe(requireActivity(), Observer {

            if (it.isSuccessful) {

                Log.d(TAG, "onDataCompleteFromAPI: Response: " + it.body()?.rates)

                base_currency_converter.text = baseCurrency
                base_currency_value_converter.text = baseCurrencyValue.toString()
                convert_currency_converter.text = convertCurrency

                // Switch
                when (true) {
                    convertCurrency == "TRY" -> convertCurrencyValue = it.body()?.rates?.TRY as Double?
                    convertCurrency == "KGS" -> convertCurrencyValue = it.body()?.rates?.KGS as Double?
                    else -> {
                        Toast.makeText(context, "currency not found", Toast.LENGTH_SHORT).show()
                    }
                }
                convert_currency_value_converter.text = String.format("%.5f",convertCurrencyValue)

                viewVisible()

            } else {
                Toast.makeText(context, "sorry, server problems...", Toast.LENGTH_SHORT).show()
                error("error-------> $it")
            }

        })
    }

    private fun viewVisible() {
        base_currency_converter.visibility = View.VISIBLE
        base_currency_value_converter.visibility = View.VISIBLE
        convert_currency_converter.visibility = View.VISIBLE
        convert_currency_value_converter.visibility = View.VISIBLE
        switch_button_converter.visibility = View.VISIBLE
        progress_bar_converter.visibility = View.INVISIBLE
    }

    private fun viewInVisible() {
        if (base_currency_converter!=null) {
            base_currency_converter.visibility = View.INVISIBLE
        }
        if (base_currency_value_converter!=null) {
            base_currency_value_converter.visibility = View.INVISIBLE
        }
        if (convert_currency_converter !=null) {
            convert_currency_converter.visibility = View.INVISIBLE
        }
        if (convert_currency_value_converter !=null) {
            convert_currency_value_converter.visibility = View.INVISIBLE
        }
        if (switch_button_converter !=null) {
            switch_button_converter.visibility = View.INVISIBLE
        }
        if (progress_bar_converter!=null) {
            progress_bar_converter.visibility = View.VISIBLE
        }
    }

}