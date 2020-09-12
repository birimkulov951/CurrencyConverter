package com.example.currencyconverter.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.example.currencyconverter.Communicator
import com.example.currencyconverter.R
import com.example.currencyconverter.model.CurrencyData
import com.example.currencyconverter.model.Rates
import com.example.currencyconverter.retrofit.RetrofitInstance
import com.example.currencyconverter.retrofit.RetrofitService
import kotlinx.android.synthetic.main.fragment_convert.*
import kotlinx.android.synthetic.main.fragment_convert.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import retrofit2.Response
import java.time.Duration.ofSeconds
import java.util.concurrent.CountDownLatch
import kotlin.time.Duration
import kotlin.time.DurationUnit


class ConvertFragment : Fragment() {

    private val TAG = "ConvertFragment"

    // Arrays
    private var currencies = arrayOf(
        "United States Dollar",
        "United Arab Emirates Dirham",
        "Armenian Dram",
        "Australian Dollar",
        "Azerbaijani Manat",
        "Bitcoin Cash",
        "Bulgarian Lev",
        "Bitcoin",
        "Cuban Convertible Peso",
        "Czech Republic Koruna",
        "EOS",
        "Ethereum",
        "Euro",
        "Indonesian Rupiah",
        "Israeli New Sheqel",
        "Japanese Yen",
        "Kyrgystani Som",
        "Kazakhstani Tenge",
        "Polish Zloty",
        "Turkish Lira",
        "Silver (troy ounce)",
        "Gold (troy ounce)"
    )
    private var apiData = arrayListOf<Rates>()

    private lateinit var communicator: Communicator
    private lateinit var retService: RetrofitService
    private var baseCurrencyValueConverterEditText: TextView? = null
    private var convertCurrencyValueConverterEditText: TextView? = null

    // Main vars
    private var mBaseCurrency: Int = 0
    private var mBaseCurrencyValue: Double? = 1.0
    private var mConvertCurrency: Int = 0
    private var mConvertCurrencyValue: Double? = 1.0

    // Helpers
    private var isSwitchButtonPressed: Boolean = false
    private var baseCurrencyValueWhenSwitchButtonPressed: Double? = null
    private var convertCurrencyValueWhenSwitchButtonPressed: Double? = null
    //private var countDownLatch = CountDownLatch(1)
    //private var countDownLatch2 = CountDownLatch(1)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_convert, container, false)
        communicator = activity as Communicator

        // if i do not initialize this views i get IllegalStateException
        val baseCurrencyConverterSpinner: Spinner = view.findViewById(R.id.base_currency_converter_spinner)
        val convertCurrencyConverterSpinner: Spinner = view.findViewById(R.id.convert_currency_converter_spinner)
        val switchButtonConverter: ImageButton = view.findViewById(R.id.switch_button_converter)
        // Edit texts
        baseCurrencyValueConverterEditText = view.findViewById(R.id.base_currency_value_converter)
        convertCurrencyValueConverterEditText = view.findViewById(R.id.convert_currency_value_converter)

        // Spinners
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            currencies
        )
        baseCurrencyConverterSpinner.adapter = arrayAdapter
        convertCurrencyConverterSpinner.adapter = arrayAdapter

        baseCurrencyConverterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mBaseCurrency = p2
                //countDownLatch.countDown()
                mBaseCurrencyValue = getCurrencyValue(p2)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        convertCurrencyConverterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mConvertCurrency = p2
                //countDownLatch2.countDown()
                mConvertCurrencyValue = getCurrencyValue(p2)
                convertCurrencyValueConverterEditText?.text = roundDecimal(mConvertCurrencyValue).toString()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Edit values
        baseCurrencyValueConverterEditText?.addTextChangedListener(object : TextWatcher {
            var placeholderValue: Double? = null
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                placeholderValue = mBaseCurrencyValue
                mBaseCurrencyValue = baseCurrencyValueConverterEditText?.text.toString().toDouble()
                Log.i(
                    TAG,
                    "INFO: mBaseCurrency: " + mBaseCurrency + " . mConvertCurrency: " + mConvertCurrency
                            + " . mBaseCurrencyValue: " + mBaseCurrencyValue + " . mConvertCurrencyValue: " + mConvertCurrencyValue + " . placeholder: " + placeholderValue
                )
                calculateConvert(placeholderValue)
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        convertCurrencyValueConverterEditText?.addTextChangedListener(object : TextWatcher {
            var placeholderValue: Double? = null
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                placeholderValue = mConvertCurrencyValue
                mConvertCurrencyValue = convertCurrencyValueConverterEditText?.text.toString().toDouble()
                Log.i(
                    TAG,
                    "INFO: mBaseCurrency: " + mBaseCurrency + " . mConvertCurrency: " + mConvertCurrency
                            + " . mBaseCurrencyValue: " + mBaseCurrencyValue + " . mConvertCurrencyValue: " + mConvertCurrencyValue + ""
                )
                calculateBase(placeholderValue)
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        // Switcher button
        switch_button_converter?.setOnClickListener{

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

            convertCurrencyValueConverterEditText?.text = roundDecimal(mConvertCurrencyValue).toString()
            baseCurrencyConverterSpinner.setSelection(mBaseCurrency, true)
            convertCurrencyConverterSpinner.setSelection(mConvertCurrency, true)

            Log.d(TAG, "onCreateView: baseCurrencyValue: $mBaseCurrencyValue")
            Log.d(TAG, "onCreateView: convertCurrencyValue: $mConvertCurrencyValue")
        }

        view.button_generate_notification_convert.setOnClickListener{
            communicator.passData(mBaseCurrency.toString(),mConvertCurrency.toString())
        }

        GlobalScope.launch {
            while (isActive) {
                delay(1800000)
                apiData.clear()
                getDataFromApi()
                println("One more second")
            }
        }

        return view
    }



    private fun calculateBase(baseOldValue: Double?) {
        var valueWeNeed: Double? = null
        when {
            baseOldValue!! < mConvertCurrencyValue!! ->  {
                // increased %
                valueWeNeed = mConvertCurrencyValue!!/baseOldValue
                mBaseCurrencyValue = mBaseCurrencyValue!!*valueWeNeed
                baseCurrencyValueConverterEditText!!.text = mBaseCurrencyValue.toString()
            }
            baseOldValue!! > mConvertCurrencyValue!! ->  {
                // decreased %
                valueWeNeed = baseOldValue/mConvertCurrencyValue!!
                mBaseCurrencyValue = mBaseCurrencyValue!!/valueWeNeed
                baseCurrencyValueConverterEditText!!.text = mBaseCurrencyValue.toString()
            }
        }
    }
    private fun calculateConvert(convertOldValue: Double?) {
        var valueWeNeed: Double? = null
        when {
            convertOldValue!! < mBaseCurrencyValue!! ->  {
                // increased %
                valueWeNeed = mBaseCurrencyValue!!/convertOldValue
                mConvertCurrencyValue = mConvertCurrencyValue!!*valueWeNeed
                baseCurrencyValueConverterEditText!!.text = mConvertCurrencyValue.toString()
            }
            convertOldValue!! > mBaseCurrencyValue!! ->  {
                // decreased %
                valueWeNeed = convertOldValue/mBaseCurrencyValue!!
                mConvertCurrencyValue = mConvertCurrencyValue!!/valueWeNeed
                baseCurrencyValueConverterEditText!!.text = mConvertCurrencyValue.toString()
            }
        }
    }

    private fun getCurrencyValue(currencyID: Int): Double {

        Log.d(TAG, "getCurrencyValue: ID - $currencyID")
        var value: Double? = 0.0

        try {
            when (true) {
                currencyID == 0 -> value =
                    apiData[0].USD as Double?
                currencyID == 1 -> value =
                    apiData[0].AED as Double?
                currencyID == 2 -> value =
                    apiData[0].AMD as Double?
                currencyID == 3 -> value =
                    apiData[0].AUD as Double?
                currencyID == 4 -> value =
                    apiData[0].AZN as Double?
                currencyID == 5 -> value =
                    apiData[0].BCH as Double?
                currencyID == 6 -> value =
                    apiData[0].BGN as Double?
                currencyID == 7 -> value =
                    apiData[0].BTC as Double?
                currencyID == 8 -> value =
                    apiData[0].CUC as Double?
                currencyID == 9 -> value =
                    apiData[0].CZK as Double?
                currencyID == 10 -> value =
                    apiData[0].EOS as Double?
                currencyID == 11 -> value =
                    apiData[0].ETH as Double?
                currencyID == 12 -> value =
                    apiData[0].TRY as Double?
                currencyID == 13 -> value =
                    apiData[0].IDR as Double?
                currencyID == 14 -> value =
                    apiData[0].ILS as Double?
                currencyID == 15 -> value =
                    apiData[0].JPY as Double?
                currencyID == 16 -> value =
                    apiData[0].KGS as Double?
                currencyID == 17 -> value =
                    apiData[0].KZT as Double?
                currencyID == 18 -> value =
                    apiData[0].PLN as Double?
                currencyID == 19 -> value =
                    apiData[0].TRY as Double?
                currencyID == 21 -> value =
                    apiData[0].XAG as Double?
                currencyID == 22 -> value =
                    apiData[0].XAU as Double?
                else ->  Toast.makeText(context, "currency not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IndexOutOfBoundsException) {
            error("getCurrencyValue: $e")
        } finally {
            return value!!
        }

    }

    override fun onStart() {
        super.onStart()
        getDataFromApi()
        Log.d(TAG, "onResume: 2")
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

                // adds api response to custom global array
                apiData.add(it.body()?.rates!!)

                /*countDownLatch.await()
                countDownLatch2.await()*/
                Log.d(TAG, "onDataCompleteFromAPI: Response: " + it.body()?.rates)

                Thread.sleep(1000)

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
        button_generate_notification_convert.visibility = View.VISIBLE
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
        if (button_generate_notification_convert!=null) {
            button_generate_notification_convert.visibility = View.INVISIBLE
        }
        if (progress_bar_converter!=null) {
            progress_bar_converter.visibility = View.VISIBLE
        }
    }

}