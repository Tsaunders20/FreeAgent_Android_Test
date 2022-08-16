package com.freeagent.testapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.freeagent.testapp.databinding.FragmentRatesListBinding
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class RatesListFragment : Fragment() {

    private val binding by lazy { FragmentRatesListBinding.inflate(layoutInflater) }
    lateinit var checked: ArrayList<Boolean>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.amountInputField.setText("100")
        // Ok so we're just going to start it at 100


        val rates = requireActivity().intent.getStringExtra("rates")!!
        // pulling the JSON String from LoadHandler using the MainActivity



        val ratesJSON = JSONObject(rates)

        val keys = ratesJSON.keys()

        val currencies = ArrayList<String>()
        val values  = ArrayList<String>()

        // We're then putting it back into an object so we can loop through it;
        // We'll be putting the Currency Symbols (USD,EUR,GBP) and the values into their own array

        println(keys.withIndex())
        while (keys.hasNext()) {
            // so basically we'll loop through the rates

            val key = keys.next()
            // key is basically the pointer - so in our case the currency code
            val curValue = ratesJSON[key].toString()
            // then using that key we can get the appropriate currency value - we'' convert this to string here since no more need for calculations

            currencies.add(key)
            values.add(curValue)


        }
        checked = ArrayList(Collections.nCopies(currencies.size, false))
        // We're also going to make an array of the checked rows so we can track them.
        // Originally I didn't see that it was only 2 we were interested in - later I stop them using more than 2 but this code allows for any number to be ticked.



        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())





        binding.amountInputField.doAfterTextChanged{
            executor.execute {
                //Background work here
                handler.post {

                    handleExchange(currencies,values) // originally I didn't have this async, but what I found was that the EditText was taking a while to show the
                    // change to the editext itself - I didn't want it to be overtly slow
                }
            }

        }

        binding.history.setOnClickListener{
            val noTrue = checked.count { it }
            if(noTrue == 2) {

                // first up - we need to make sure for step 2 that they've only selected 2
                // the next part of code works from 1-infinity technically but just sticking to the assignment



                // ok lets make an array of currencies we actually want
                val output = ArrayList<String>()
                val noEU = binding.amountInputField.text.toString()
                // first up we're setting up an array of only the currencies we care about
                // We're also pulling the number of Euros the user has put in.

                currencies.forEachIndexed { index, element ->
                    if (checked[index]) {
                        output.add(element)
                    }
                }
                // looping through the currencies then checkin the index and comparing the ticked rows against that, adding it if has been ticked.
                val currencyString = output.joinToString(separator = ",")
                // the API in the next step uses this as a string, same as last time - so we'll sort it out here.


                val intent = Intent(activity, History::class.java)

                intent.putExtra("currencyString", currencyString)
                intent.putExtra("noEU", noEU)

                startActivity(intent)
            }
            else{
                // If they haven't selected 2 we'll just go ahead and give them a little hint.
                val text = "Please select 2 currencies."
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(activity, text, duration)
                toast.show()

            }
        }

        handleExchange(currencies,values) // default call - using 100



    }
    fun handleExchange(currencies: ArrayList<String>,values: ArrayList<String>){

        // this'll be our function to handle changes in the EU and editng the rows accordingly.

        val finalValues = ArrayList<String>()
        var noEU:Float = 0.0F
        if(binding.amountInputField.text.toString() != "") {
            noEU = binding.amountInputField.text.toString().toFloat()
        }
        // so firstly we need to be sure on the whole nothing in the box possibility
        // the above will set the number of Euros to 0 if theres nothing there.



        // noEU is the number we have in the editext field

        // so we're going to convert it to currency format here - this'll also give us the currency code
        // sadly in some cases this gives us a bit too much more than I'd like
        // e.g. GBP becomes £ but USD becomes US$


        values.forEachIndexed{ index, it ->
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 2
            format.currency = Currency.getInstance(currencies?.get(index) ?: "EUR")
            // so we're going to convert it to currency format here - this'll also give us the currency code
            // sadly in some cases this gives us a bit too much more than I'd like
            // e.g. GBP becomes £ but USD becomes US$

         //   inputArray.add(format.format(baseValue).toString())





            finalValues.add(format.format((it.toFloat() * noEU)).toString())
        }
        // we're making a finalValues array which multiplies our euros with each given exchange rate.

        val ratesListAdapter = RatesListAdapter(currencies,finalValues,checked,this)
        // brought it down here to give us more time to load in the variables from mainactivity
        // also allows us to directly bring in the new arrays

        binding.ratesListRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ratesListAdapter
        }
    }
     fun handleChecked(bool: Boolean,index: Int) {
        checked[index] = bool

         // this handles the boolean arraylist so that this side knows what's been ticked.
         // It's  needed because the code that handles the clicks is in the RatesListAdapter
    }
}