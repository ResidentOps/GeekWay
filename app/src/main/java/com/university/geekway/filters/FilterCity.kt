package com.university.geekway.filters

import android.widget.Filter
import com.university.geekway.adapters.AdapterCity
import com.university.geekway.models.Cities

class FilterCity: Filter {

    private var filterList: ArrayList<Cities>
    private var adapterCity: AdapterCity

    constructor(filterList: ArrayList<Cities>, adapterCity: AdapterCity) : super() {
        this.filterList = filterList
        this.adapterCity = adapterCity
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<Cities> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].cityname.uppercase().contains(constraint)) {
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
        adapterCity.cityArrayList = results.values as ArrayList<Cities>
        adapterCity.notifyDataSetChanged()
    }
}