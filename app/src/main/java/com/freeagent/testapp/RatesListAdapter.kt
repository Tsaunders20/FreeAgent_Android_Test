package com.freeagent.testapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.freeagent.testapp.databinding.ItemViewCurrencyBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class RatesListAdapter(private val currencies: ArrayList<String>,
                       private val values: ArrayList<String>,
                       private val checked: ArrayList<Boolean>,
                       val RatesListFragment: RatesListFragment
) : RecyclerView.Adapter<RatesListAdapter.ViewHolder>() {

    // So I modified the adapter to pull the currencies, finalvalues and checked
    // currencies and values power the text that appears in the adapter
    // checked is here mostly so if the  recyclerview changes we still know what was previously ticked
    // (e.g. I don't want if someone changes the euros for the ticks to disappear)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemViewCurrencyBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {




       // val value = ((values[position].toFloat() * 100.0).roundToInt() / 100.0).toString()
        val value = values[position]

        val checkbox = holder.itemView.findViewById<CheckBox>(R.id.checkbox_meat)
        // Ok so we want our values to be at 2dp, so this will convert it to that

        holder.itemView.findViewById<TextView>(R.id.currency_name_label).text = currencies[position]
        holder.itemView.findViewById<TextView>(R.id.currency_rate_label).text = value
        checkbox.isChecked = checked[position] // This will be explained in full below but roughly it'll tick a box if its supposed to be ticked already.

        holder.itemView.setOnClickListener {


           // val lessthantwo = checkTrues()
            val noTrue = checked.count { it }

            if(noTrue < 2 || checked[position]) {
                // Ok so first up we need to check the condition - my code technically works with any number but next part specifies 2 currencies.
                    // So basically - checks if either theres not 2 currencies selected already or checks if your looking at one that already checked.
                        // so first stops you adding more, second allows you to untick.



                checkbox.isChecked = !checkbox.isChecked
                checked[position] = checkbox.isChecked
                // so basically on click we're going to toggle the checkbox status as well as logging it for usage later.
                RatesListFragment.handleChecked(checkbox.isChecked, position)
                // This parts a bit complicated but basically the adapter refreshes if on the fragment the edittext Changes
                // because of this we need to save what's ticked somehow in the fragment
                // so basically we're sending an array of booleans back so that when it remakes it, it'll know whether to tick it to the user or not.
            }


        }
        holder.itemView.findViewById<CheckBox>(R.id.checkbox_meat).setOnClickListener{
            // Doing the same for checkbox due to a bug I found - could probably put both in a function but don't think its too important
            // Only difference is that we don't programatically change the checkbox
            val noTrue = checked.count { it }

            if(noTrue < 2 || checked[position]) {
                checked[position] = checkbox.isChecked
                RatesListFragment.handleChecked(checkbox.isChecked, position)
            }
            else{
                checkbox.isChecked = !checkbox.isChecked
            }
        }



    }

    override fun getItemCount(): Int {
        return currencies.size

    }


    class ViewHolder(private val binding: ItemViewCurrencyBinding) : RecyclerView.ViewHolder(binding.root) {

    }
    interface Callbacks {
        fun handleChecked(bool: Boolean,index: Int)
    }
}