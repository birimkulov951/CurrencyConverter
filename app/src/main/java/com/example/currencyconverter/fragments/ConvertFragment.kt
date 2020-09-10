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
    private var mBaseCurrency: Int = 0
    private var mBaseCurrencyValue: Double? = 1.0
    private var mConvertCurrency: Int = 0
    private var mConvertCurrencyValue: Double? = null
    private var baseSpinnerStr: String? = null
    private var convertSpinnerStr: String? = null


    // Arrays for spinner
    private var currencies = arrayOf("United States Dollar","United Arab Emirates Dirham","Armenian Dram","Australian Dollar","Azerbaijani Manat",
        "Bitcoin Cash","Bulgarian Lev","Bitcoin","Cuban Convertible Peso","Czech Republic Koruna","EOS","Ethereum","Euro","Indonesian Rupiah",
        "Israeli New Sheqel","Japanese Yen","Kyrgystani Som","Kazakhstani Tenge","Polish Zloty","Turkish Lira","Silver (troy ounce)","Gold (troy ounce)")

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

        // if i do not initialize this views i get IllegalStateException
        val baseCurrencyConverterSpinner: Spinner = view.findViewById(R.id.base_currency_converter_spinner)
        val baseCurrencyValueConverter: TextView = view.findViewById(R.id.base_currency_value_converter)
        val convertCurrencyConverterSpinner: Spinner = view.findViewById(R.id.convert_currency_converter_spinner)
        val convertCurrencyValueConverter: TextView = view.findViewById(R.id.convert_currency_value_converter)
        val switchButtonConverter: ImageButton = view.findViewById(R.id.switch_button_converter)
        val button: Button = view.findViewById(R.id.button) // chernovik

        // Spinner
        val arrayAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,currencies)
        baseCurrencyConverterSpinner.adapter = arrayAdapter
        convertCurrencyConverterSpinner.adapter = arrayAdapter
        baseCurrencyConverterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mBaseCurrency = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        convertCurrencyConverterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mConvertCurrency = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                getDataFromApi()
            }
        })

        switchButtonConverter.setOnClickListener{

            var placeHolder: Int
            placeHolder = mBaseCurrency
            mBaseCurrency = mConvertCurrency
            mConvertCurrency = placeHolder

            if (!isSwitchButtonPressed) {
                isSwitchButtonPressed = true

                baseCurrencyValueWhenSwitchButtonPressed = mBaseCurrencyValue
                convertCurrencyValueWhenSwitchButtonPressed = mConvertCurrencyValue

                mConvertCurrencyValue = mBaseCurrencyValue!!.div(mConvertCurrencyValue!!)
            } else {
                isSwitchButtonPressed = false

                mBaseCurrencyValue = baseCurrencyValueWhenSwitchButtonPressed
                mConvertCurrencyValue = convertCurrencyValueWhenSwitchButtonPressed
            }

            convert_currency_value_converter.text = roundDecimal(mConvertCurrencyValue).toString()
            baseCurrencyConverterSpinner.setSelection(mBaseCurrency,true)
            convertCurrencyConverterSpinner.setSelection(mConvertCurrency,true)

            Log.d(TAG, "onCreateView: baseCurrencyValue: " + mBaseCurrencyValue)
            Log.d(TAG, "onCreateView: convertCurrencyValue: " + mConvertCurrencyValue)
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

                //base_currency_converter_spinner.text = baseCurrency
                base_currency_value_converter.text = mBaseCurrencyValue.toString()
                //convert_currency_converter_spinner.text = convertCurrency

                // Switch
                when (true) {
                    mConvertCurrency == 0 -> mConvertCurrencyValue = it.body()?.rates?.USD as Double?
                    mConvertCurrency == 1 -> mConvertCurrencyValue = it.body()?.rates?.AED as Double?
                    mConvertCurrency == 2 -> mConvertCurrencyValue = it.body()?.rates?.AMD as Double?
                    mConvertCurrency == 3 -> mConvertCurrencyValue = it.body()?.rates?.AUD as Double?
                    mConvertCurrency == 4 -> mConvertCurrencyValue = it.body()?.rates?.AZN as Double?
                    mConvertCurrency == 5 -> mConvertCurrencyValue = it.body()?.rates?.BCH as Double?
                    mConvertCurrency == 6 -> mConvertCurrencyValue = it.body()?.rates?.BGN as Double?
                    mConvertCurrency == 7 -> mConvertCurrencyValue = it.body()?.rates?.BTC as Double?
                    mConvertCurrency == 8 -> mConvertCurrencyValue = it.body()?.rates?.CUC as Double?
                    mConvertCurrency == 9 -> mConvertCurrencyValue = it.body()?.rates?.CZK as Double?
                    mConvertCurrency == 10 -> mConvertCurrencyValue = it.body()?.rates?.EOS as Double?
                    mConvertCurrency == 11 -> mConvertCurrencyValue = it.body()?.rates?.ETH as Double?
                    mConvertCurrency == 12 -> mConvertCurrencyValue = it.body()?.rates?.TRY as Double?
                    mConvertCurrency == 13 -> mConvertCurrencyValue = it.body()?.rates?.IDR as Double?
                    mConvertCurrency == 14 -> mConvertCurrencyValue = it.body()?.rates?.ILS as Double?
                    mConvertCurrency == 15 -> mConvertCurrencyValue = it.body()?.rates?.JPY as Double?
                    mConvertCurrency == 16 -> mConvertCurrencyValue = it.body()?.rates?.KGS as Double?
                    mConvertCurrency == 17 -> mConvertCurrencyValue = it.body()?.rates?.KZT as Double?
                    mConvertCurrency == 18 -> mConvertCurrencyValue = it.body()?.rates?.PLN as Double?
                    mConvertCurrency == 19 -> mConvertCurrencyValue = it.body()?.rates?.TRY as Double?
                    mConvertCurrency == 21 -> mConvertCurrencyValue = it.body()?.rates?.XAG as Double?
                    mConvertCurrency == 22 -> mConvertCurrencyValue = it.body()?.rates?.XAU as Double?
                    else -> {
                        Toast.makeText(context, "currency not found", Toast.LENGTH_SHORT).show()
                    }
                }

                convert_currency_value_converter.text = roundDecimal(mConvertCurrencyValue).toString()
                Log.d(TAG, "getDataFromApi: coroutines")

                viewVisible()

            } else {
                Toast.makeText(context, "sorry, server problems...", Toast.LENGTH_SHORT).show()
                error("error-------> $it")
            }

        })
    }

    private fun roundDecimal(doubleValue: Double?): Double {
        return Math.round(doubleValue?.times(100000.0)!!) / 100000.0
    }

    private fun viewVisible() {
        base_currency_converter_spinner.visibility = View.VISIBLE
        base_currency_value_converter.visibility = View.VISIBLE
        convert_currency_converter_spinner.visibility = View.VISIBLE
        convert_currency_value_converter.visibility = View.VISIBLE
        switch_button_converter.visibility = View.VISIBLE
        progress_bar_converter.visibility = View.INVISIBLE
    }

    private fun viewInVisible() {
        if (base_currency_converter_spinner !=null) {
            base_currency_converter_spinner.visibility = View.INVISIBLE
        }
        if (base_currency_value_converter !=null) {
            base_currency_value_converter.visibility = View.INVISIBLE
        }
        if (convert_currency_converter_spinner !=null) {
            convert_currency_converter_spinner.visibility = View.INVISIBLE
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