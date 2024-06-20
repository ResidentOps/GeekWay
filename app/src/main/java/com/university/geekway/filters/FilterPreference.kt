package com.university.geekway.filters

import android.widget.Filter
import com.university.geekway.adapters.AdapterCategoryPreference
import com.university.geekway.models.Categories

class FilterPreference: Filter {

    var filterList: ArrayList<Categories>
    lateinit var adapterCategoryPreferences: AdapterCategoryPreference

    constructor(filterList: ArrayList<Categories>, adapterCategoryPreferences: AdapterCategoryPreference) : super() {
        this.filterList = filterList
        this.adapterCategoryPreferences = adapterCategoryPreferences
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<Categories>()
            for (i in filterList.indices) {
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

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adapterCategoryPreferences.categoriesArrayList = results.values as ArrayList<Categories>
        adapterCategoryPreferences.notifyDataSetChanged()
    }
}