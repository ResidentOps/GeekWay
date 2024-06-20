package com.university.geekway.filters

import android.widget.Filter
import com.university.geekway.adapters.AdapterCategory
import com.university.geekway.models.Categories

class FilterCategory: Filter {

    private var filterList: ArrayList<Categories>
    private var adapterCategory: AdapterCategory

    constructor(filterList: ArrayList<Categories>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<Categories> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].categoryname.uppercase().contains(constraint)) {
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
        adapterCategory.categoryArrayList = results.values as ArrayList<Categories>
        adapterCategory.notifyDataSetChanged()
    }
}