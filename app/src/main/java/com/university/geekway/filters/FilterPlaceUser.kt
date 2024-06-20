package com.university.geekway.filters

import android.widget.Filter
import com.university.geekway.adapters.AdapterPlaceUser
import com.university.geekway.models.Places

class FilterPlaceUser : Filter {

    var filterList: ArrayList<Places>
    var adapterPlaceUser: AdapterPlaceUser

    constructor(filterList: ArrayList<Places>, adapterPlaceUser: AdapterPlaceUser) : super() {
        this.filterList = filterList
        this.adapterPlaceUser = adapterPlaceUser
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<Places>()
            for (i in filterList.indices) {
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

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adapterPlaceUser.placesArrayList = results.values as ArrayList<Places>
        adapterPlaceUser.notifyDataSetChanged()
    }
}