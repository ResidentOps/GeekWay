package com.university.geekway.filters

import android.widget.Filter
import com.university.geekway.adapters.AdapterCityUser
import com.university.geekway.models.Cities

class FilterCityUser: Filter {

    private var filterList: ArrayList<Cities>
    private var adapterCityUser: AdapterCityUser

    constructor(filterList: ArrayList<Cities>, adapterCityUser: AdapterCityUser) : super() {
        this.filterList = filterList
        this.adapterCityUser = adapterCityUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<Cities> = ArrayList()
            for (i in filterList.indices) {
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
        adapterCityUser.citiesArrayList = results.values as ArrayList<Cities>
        adapterCityUser.notifyDataSetChanged()
    }
}