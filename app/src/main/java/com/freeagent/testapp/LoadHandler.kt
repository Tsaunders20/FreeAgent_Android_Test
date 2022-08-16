package com.freeagent.testapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import org.json.JSONObject
import org.json.JSONTokener

class LoadHandler: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.load_handler)

        // Making this activity so I can both make a loading splash screen and also have the currency amounts ready for the user instead of needing to make a post.
       // val APIKEY = "Twv0UtNIhLkb0sfEuSHmbTuDFh76qxcP" // so basically the API key
        val APIKEY = "SM2MeJ7cDlv3Es0hREZI6WDjtOPoT1zL" // new API key becuase i hit 100 requests
        val currencies =
            arrayOf("USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "SEK", "NZD")
        // Then I've made an array of the currencies
        val currencyString = currencies.joinToString(separator = ",")
        // Technically I could have made a string like "USD,EUR,JPY" and so on but it wouldn't have looked as neat

        // fuels the POST/GET API I've used the most often when coding in kotlin
        Fuel.get("https://api.apilayer.com/fixer/latest?symbols=$currencyString&base=EUR")
            .header("Content-Type", "application/json")
            .header("apikey", APIKEY)
         //   .body(jsoncode.toString())
            .response { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                        kotlin.io.println("Fail")
                        val (payload, error) = result
                        kotlin.io.println(payload)




                    }
                    is Result.Success -> {
                        val data = result.get()
                        val output = String(response.data)
                       println(output)
                        println(response)


                        val jsonObject = JSONTokener(output).nextValue() as JSONObject

                        val rates = jsonObject.getString("rates")

                        val intent = Intent(applicationContext, MainActivity::class.java)
                     //   intent.putExtra("timestamp",timestamp) Don't think we'll need timestamp
                        intent.putExtra("rates",rates)

                        startActivity(intent)

                    }
                }
            }

    }
}