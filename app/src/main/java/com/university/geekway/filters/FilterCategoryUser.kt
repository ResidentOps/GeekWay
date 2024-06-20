package com.university.geekway.filters

import android.widget.Filter
import com.university.geekway.adapters.AdapterCategoryUser
import com.university.geekway.models.Categories

class FilterCategoryUser: Filter {

    private var filterList: ArrayList<Categories>
    private var adapterCategoryUser: AdapterCategoryUser

    constructor(filterList: ArrayList<Categories>, adapterCategoryUser: AdapterCategoryUser) : super() {
        this.filterList = filterList
        this.adapterCategoryUser = adapterCategoryUser
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
        adapterCategoryUser.categoryArrayList = results.values as ArrayList<Categories>
        adapterCategoryUser.notifyDataSetChanged()
    }
}