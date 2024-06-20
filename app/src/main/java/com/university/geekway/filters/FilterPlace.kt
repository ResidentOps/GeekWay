package com.university.geekway.filters

import android.widget.Filter
import com.university.geekway.adapters.AdapterPlace
import com.university.geekway.models.Places

class FilterPlace : Filter {

    private var filterList: ArrayList<Places>
    private var adapterPlace: AdapterPlace

    constructor(filterList: ArrayList<Places>, adapterPlace: AdapterPlace) : super() {
        this.filterList = filterList
        this.adapterPlace = adapterPlace
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<Places> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].placename.uppercase().contains(constraint)) {
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterPlace.placesArrayList = results.values as ArrayList<Places>
        adapterPlace.notifyDataSetChanged()
    }
}