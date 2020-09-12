package com.example.currencyconverter.fragments

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.example.currencyconverter.CoroutineLatch
import com.example.currencyconverter.R
import com.example.currencyconverter.model.CurrencyData
import com.example.currencyconverter.model.Rates
import com.example.currencyconverter.retrofit.RetrofitInstance
import com.example.currencyconverter.retrofit.RetrofitService
import kotlinx.android.synthetic.main.custom_notify.*
import kotlinx.android.synthetic.main.fragment_notify.*
import kotlinx.android.synthetic.main.fragment_notify.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.internal.notify
import retrofit2.Response
import java.util.*
import java.util.concurrent.CountDownLatch

class NotifyFragment : Fragment() {

    private val TAG = "NotifyFragment"

    private lateinit var retService: RetrofitService
    private var latch =  CoroutineLatch(1)

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
    private lateinit var alarmManager: AlarmManager

    private var CHANNEL_ID = "CHANNEL_ID"

    private var notificationID = 0

    private var apiLastUpdatedTime: Long? = null
    private var mFirstCurrencyName: String? = null
    private var mSecondCurrencyName: String? = null
    private var mFirstCurrencyValue: Double? = 1.0
    private var mSecondCurrencyValue: Double? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view :View = inflater.inflate(R.layout.fragment_notify, container, false)

        var first: TextView = view.findViewById(R.id.first_currency_notify)
        var second: TextView = view.findViewById(R.id.second_currency_notify)

        getDataFromApi()


        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        var notifyButton: Button = view.findViewById(R.id.button_notify)

        mCreateNotificationChannel()

        notifyButton.setOnClickListener{

            Log.d(TAG, "onCreateView: ")
            notificationID += 1

            CoroutineScope(IO).launch {

                withContext(IO) {

                    val job1 = launch{

                        while (isActive) {

                            var compareFirst = getCurrencyDataByID(mFirstCurrencyName?.toInt()).toString()
                            var compareSecond = getCurrencyDataByID(mSecondCurrencyName?.toInt()).toString()

                            // if user's condition is true send notification
                            if (compareSecond.toDouble() >= second_currency_value_notify?.text.toString().toDouble()) {

                                val notifyText =  "Now 1 $mFirstCurrencyName is greater than ${second_currency_value_notify.toString().toInt()} $mSecondCurrencyName" + "s"

                                sendNotification(notifyText)
                            }

                            Thread.sleep(1800000)
                            // Updates data
                            getDataFromApi()

                            Thread.sleep(5000)
                        }
                    }

                }

            }
        }


        return view
    }

    private fun getCurrencyDataByID(id: Int?): Double? {
        var result: Double? = -1.0

        try{
            when (true) {
                id == 0 -> result =
                    apiData[0].USD as Double?
                id == 1 -> result =
                    apiData[0].AED as Double?
                id == 2 -> result =
                    apiData[0].AMD as Double?
                id == 3 -> result =
                    apiData[0].AUD as Double?
                id == 4 -> result =
                    apiData[0].AZN as Double?
                id == 5 -> result =
                    apiData[0].BCH as Double?
                id == 6 -> result =
                    apiData[0].BGN as Double?
                id == 7 -> result =
                    apiData[0].BTC as Double?
                id == 8 -> result =
                    apiData[0].CUC as Double?
                id == 9 -> result =
                    apiData[0].CZK as Double?
                id == 10 -> result =
                    apiData[0].EOS as Double?
                id == 11 -> result =
                    apiData[0].ETH as Double?
                id == 12 -> result =
                    apiData[0].TRY as Double?
                id == 13 -> result =
                    apiData[0].IDR as Double?
                id == 14 -> result =
                    apiData[0].ILS as Double?
                id == 15 -> result =
                    apiData[0].JPY as Double?
                id == 16 -> result =
                    apiData[0].KGS as Double?
                id == 17 -> result =
                    apiData[0].KZT as Double?
                id == 18 -> result =
                    apiData[0].PLN as Double?
                id == 19 -> result =
                    apiData[0].TRY as Double?
                id == 21 -> result =
                    apiData[0].XAG as Double?
                id == 22 -> result =
                    apiData[0].XAU as Double?
                else -> Toast.makeText(context, "currency not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IndexOutOfBoundsException) {
            error("getCurrencyValue: $e")
        } finally {
            return result!!
        }
    }


    private fun getDataFromApi() {

        retService = RetrofitInstance.getRetrofitInstance().create(RetrofitService::class.java)

        val responseLiveData: LiveData<Response<CurrencyData>> = liveData {
            val response = retService.getRates()
            emit(response)
        }

        responseLiveData.observe(requireActivity(), Observer {
            if (it.isSuccessful) {

                Log.d(TAG, "onDataCompleteFromAPI: Response222: " + it.body()?.rates)
                // adds api response to custom global array
                apiData.clear()
                apiData.add(it.body()?.rates!!)
                apiLastUpdatedTime = it.body()?.updated.toString().toLong()


                if (arguments != null) {
                    mFirstCurrencyName = arguments?.getString("BASE_CURRENCY")
                    mSecondCurrencyName = arguments?.getString("CONVERT_CURRENCY")

                    Log.d(TAG, "passData: BASE_CURRENCY: $mFirstCurrencyName")
                    Log.d(TAG, "passData: BASE_CURRENCY: $mSecondCurrencyName")

                    first_currency_notify?.text = currencies[mFirstCurrencyName.toString().toInt()]
                    second_currency_notify?.text = currencies[mSecondCurrencyName.toString().toInt()]

                }


            } else {

                Toast.makeText(context, "sorry, server problems...", Toast.LENGTH_SHORT).show()
                error("error-------> $it")

            }
        })
    }

    private fun mCreateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App notification"
            val descriptionText = "This is notification "
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }
            val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        } else {
            Log.d(TAG, "mCreateNotificationChannel: error")
        }
    }

    private fun sendNotification(text: String) {

        var bitmapLargeIcon = BitmapFactory.decodeResource(requireContext().resources,R.drawable.image_currency_converter)

        var builder = NotificationCompat.Builder(requireContext(),CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Bingo!")
            .setContentText(text)
            .setLargeIcon(bitmapLargeIcon)
            //.setStyle(NotificationCompat.BigPictureStyle().bigLargeIcon(bitmapLargeIcon))
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        with(NotificationManagerCompat.from(requireContext())){
            notify(notificationID,builder.build())
            notificationID += 1
        }
    }

}