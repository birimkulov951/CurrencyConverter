package com.example.currencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.currencyconverter.fragments.ConvertFragment
import com.example.currencyconverter.fragments.NotifyFragment

class MainActivity : AppCompatActivity() , Communicator {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //supportActionBar?.hide()
        //val fragment1 = ConvertFragment()
        //supportFragmentManager.beginTransaction().replace(R.id.fra, fragment1).commit()

    }

    override fun passData(firstCurrency: String, secondCurrency: String) {
        val bundle = Bundle()
        bundle.putString("BASE_CURRENCY",firstCurrency)
        bundle.putString("CONVERT_CURRENCY",secondCurrency)

        val transaction = this.supportFragmentManager.beginTransaction()
        val frag2 = NotifyFragment()
        frag2.arguments = bundle

        transaction.replace(R.id.content_id, frag2)
        transaction.addToBackStack(null)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.commit()
    }
}