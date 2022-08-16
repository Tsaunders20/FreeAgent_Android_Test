package com.freeagent.testapp

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import org.json.JSONObject
import org.json.JSONTokener
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class History : AppCompatActivity(), View.OnClickListener {

    var noEU = "100"
    val dataArray = ArrayList<ArrayList<String>>()
    private lateinit var currencies : List<String>
    var lastID = 0
    private var direction = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        noEU = intent.getStringExtra("noEU").toString()
        val currencyString = intent.getStringExtra("currencyString").toString()
        currencies = currencyString?.split(",") // it's important again later for our table

        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE // We've got another progress bar while the table loads - generally doesn't take long but doesn't hurt to have.

        // Ok so I actually misunderstood the exercise at first here and didn't realise it should only be 2 currencies coming through here
        // I decided to just stick with what I had though and roll with it
        // I went back and restricted it - but this'll work with as many as needed (though I didn't add a scrollview)


       // findViewById<TextView>(R.id.EUind).text = "You Entered: €$noEU"
        findViewById<TextView>(R.id.EUind).text = getString(R.string.enteredEU, noEU)
        // Basically just a string resource for "You Entered €404.50"









        //val APIKEY = "Twv0UtNIhLkb0sfEuSHmbTuDFh76qxcP" // so basically the API key
        val APIKEY = "SM2MeJ7cDlv3Es0hREZI6WDjtOPoT1zL"

        val current = LocalDateTime.now()
        val fivedaysago = LocalDateTime.now().minusDays(4)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start_date = fivedaysago.format(formatter)
        val end_date = current.format(formatter)

        // In hindsight I probably could have gotten this right at the beginning and used the data for both parts

val URL = "https://api.apilayer.com/fixer/timeseries?start_date=$start_date&end_date=$end_date&symbols=$currencyString&base=EUR"

        Fuel.get(URL)
            .header("Content-Type", "application/json")
            .header("apikey", APIKEY)
            .response { request, response, result ->
                when (result) {
                    is Result.Failure -> {

                    }
                    is Result.Success -> {
                        val output = String(response.data)

                        val jsonObject = JSONTokener(output).nextValue() as JSONObject

                        val rates = jsonObject.getString("rates")

                        val ratesJSON = JSONObject(rates)
                        val keys = ratesJSON.keys()
                        // So similar to before we need to loop through our new data.

                        var indexr = 0
                        while (keys.hasNext()) {
                            // OK so this time keys refers to a given DATE - so for example key will be 2022-07-29
                            // so basically we gotta use that and parse it.
                            val key = keys.next()

                            val inputArray = ArrayList<String>() // we're going to make a 2d array by making an array for each of these so that we can save it for later

                                inputArray.add(key)
                            

                            val actualRates = JSONObject(ratesJSON[key].toString())
                            // last time this produced our value - this time it produces the rates as they were last time ironically
                            val keys2 = actualRates.keys()
                            var currencyIndex = 0

                                while (keys2.hasNext()) {
                                    val key2 = keys2.next()


                                    println(actualRates[key2].toString())

                                    val baseValue = ((actualRates[key2].toString().toFloat())*(noEU!!.toFloat()))
                                    // ok so I've pulled the base string for the currency conversion * the number of euroes
                                    // this is becasue I found late in testing that we would sometimes be given values like
                                    // GBP: 84.2 after my function to go to 2dp

                                    val format: NumberFormat = NumberFormat.getCurrencyInstance()
                                    format.maximumFractionDigits = 2
                                    format.currency = Currency.getInstance(currencies?.get(currencyIndex) ?: "EUR")
                                    // so we're going to convert it to currency format here - this'll also give us the currency code
                                    // sadly in some cases this gives us a bit too much more than I'd like
                                    // e.g. GBP becomes £ but USD becomes US$

                                    inputArray.add(format.format(baseValue).toString())
                                    currencyIndex++
                                }

                          //  val inputArray:Array<String> = arrayOf(key,key2,actualRates[key2].toString())


                            dataArray.add(inputArray)

                            indexr++
                        }
                        // so basically we're coming out of this with a 2D array in the format
                        //[[date1,cur1,cur2,cur3],[date2,cur1,cur2,cur3]] and so on

                        dataArray.sortWith { o1, o2 -> o1[0].compareTo(o2[0])}
                        // Ok so this is going to sort our array by the index ASCENDING
                        // since this is our first go around we ideally want to start with date DESCENDING
                        // So we'll flip in array.


                        // Ok so basically by default the API returns the array in order of date ASCENDING
                        // so we'll reverse it because everyone wants DESCENDING
                        dataArray.reverse()

                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.INVISIBLE // we can now hide our progress bar since tables about to be sorted
                        handleTable(dataArray,currencies,0,direction) // so starting it off with pushing it to the table sorted by date
                    }
                }
            }


    }
    fun handleTable(dataArray: ArrayList<ArrayList<String>>,currencies: List<String>?,index:Int,direction:Boolean){
        var table = findViewById<TableLayout>(R.id.main_table)
        // Ok so we're going to programatically make our table.
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            //Background work here
            handler.post {
                table.removeAllViews() // clears the table to allow us to rebuild it
            }
        }

        val tr = TableRow(this)
        // so defining a tableRow to go into the table - this'll be for our header



        val parentView = LinearLayout(this)
        // this parentView will include a Text for our header and potentially an arrow to represent the sorting.

        val c1 = TextView(this)
        c1.setPadding(50, 10, 10, 10)
        c1.setTextColor(Color.parseColor("#FFFF00"))
        c1.text = "Date:"
        c1.id = 0;
        // identification
        c1.setOnClickListener(this); // set TableRow onClickListner

        // So basically we've given it a color, a bit of padding, a title then given it an onclick - that onclick will be used to handle the sorting later on click.


        parentView.addView(c1)
        // we'll then add it to the linearlayout

        if(index == 0) {

            val imageView1 = ImageView(this)
            if(direction) {
                imageView1.setImageResource(R.drawable.downarrow);
            }
            else{
                imageView1.setImageResource(R.drawable.uparrow);
            }


            parentView.addView(imageView1)
        }
        // So what we did here was we looked to see if they were looking to sort the date, if they were we checked if they'd prevously clicked date and whichway it was.
        // The first time you read this it'll be going down because that's what I've got default sort as
        // if you click date again it'll sort ascendingly, if you then click another column it'll sort that descending.


        tr.addView(parentView)
        // we then add the linearlayout including the textview and imageview.



        currencies?.forEachIndexed { index2, element ->

            // Ok so now we're just doing the same thing but for all the currencies we have
            // in this exercise we're forced into 2 - but it'll work with more.


            val parentView2 = LinearLayout(this)






            val c2 = TextView(this)
            c2.setPadding(50, 10, 10, 10)
            c2.setTextColor(Color.parseColor("#FFFF00"))
            c2.text = element
            c2.id = index2 + 1
            c2.setOnClickListener(this)
            parentView2.addView(c2)

            if(index == index2 + 1) {
                val imageView = ImageView(this)
                if(direction) {
                    imageView.setImageResource(R.drawable.downarrow);
                }
                else{
                    imageView.setImageResource(R.drawable.uparrow);
                }
                parentView2.addView(imageView)
            }



            tr.addView(parentView2)
        }

        tr.setPadding(0, 30, 0, 30)
        // finally we'll add a bit more padding since the headers are more important and should be bigger to allow room to click on them.


        executor.execute {
            //Background work here
            handler.post {
                table.addView(tr)
            }
        }
        val type1 = GradientDrawable()

        type1.cornerRadius = 5f
        type1.setColor(Color.parseColor("#c4dfe0"))
        type1.setStroke(1, Color.GRAY)

        val type2 = GradientDrawable()
        type2.cornerRadius = 5f
        type2.setColor(Color.WHITE)
        type2.setStroke(1, Color.GRAY)

        // Ok so these gradientdrawables will allow us to pretty easily change the colors of each row.
        // we're going for alternating colors so type1 will be for the evens and type2 for the odds.





        dataArray.forEachIndexed { count, it ->

            // so going through each array in our 2d array now.
            val tr2 = TableRow(this)
            it.forEach {
                val c2 = TextView(this)
                c2.setPadding(50, 10, 10, 10)
                c2.text = it

                tr2.addView(c2)
            }

            if(count % 2 == 0){
                tr2.background = type1
            }
            else{
                tr2.background = type2
            }
            // As we said before this is where depending on odd and even rows we give the row a different color.



            executor.execute {
                //Background work here
                handler.post {
                    table.addView(tr2)
                }
            }
        }




    }
    override fun onClick(v: View) {
        // Ok so this is where we handle the clicks made by our headers - so v has an ID for each we gave it earlier.
        // I gave them numbers for simplicity but they could be made pretty much anything.



        val clickedid = v.id // so this could equal 0 for date, or 1 for the next col, 2 for one after for example.

        dataArray.sortWith { o1, o2 -> o1[clickedid].compareTo(o2[clickedid])}
        // Ok so this is going to sort our array by the index ASCENDING
        // since this is our first go around we ideally want to start with date DESCENDING
        // So we'll flip in array.


        // Ok so toucched on this before but now we want to check whether or not we should be sorting DESCENDING or ASCENDING
        // descending is our default but if the users already got it descending AND they're clicking the same row again it'll go direction false which means ascending.
        // if it is descending though we reverse the direction of our array sincce the sortwith above will sort ASCENDING
        if(clickedid == lastID && direction){
            direction = false
        }
        else {
            dataArray.reverse()
            direction = true
        }
        lastID = clickedid
        // we then set the lastID to be whatever we clicked for the next time they come through here.

        handleTable(dataArray,currencies,clickedid,direction)
        // And finally refresh the table.
    }

}

