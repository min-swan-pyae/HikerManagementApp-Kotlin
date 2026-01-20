package com.example.hikermanagementapp.ui.common

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class NoFilterArrayAdapter<T>(context: Context, layout: Int, private val items: List<T>) :
    ArrayAdapter<T>(context, layout, items) {

    private val noFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return FilterResults().apply {
                values = items
                count = items.size
            }
        }
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
        override fun convertResultToString(resultValue: Any?): CharSequence {
            return resultValue?.toString() ?: ""
        }
    }

    override fun getFilter(): Filter = noFilter
}

