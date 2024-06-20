package com.university.geekway.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.university.geekway.models.Categories
import com.university.geekway.user.PlacesUserActivity
import com.university.geekway.databinding.RowCategoriesUserBinding
import com.university.geekway.filters.FilterCategoryUser

class AdapterCategoryUser : RecyclerView.Adapter<AdapterCategoryUser.HolderCategoryUser>, Filterable {

    private val context: Context
    public var categoryArrayList: ArrayList<Categories>
    private var filterList: ArrayList<Categories>
    private var filter: FilterCategoryUser? = null
    private lateinit var binding: RowCategoriesUserBinding

    constructor(context: Context, categoryArrayList: ArrayList<Categories>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoryUser {
        binding = RowCategoriesUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategoryUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategoryUser, position: Int) {
        val model = categoryArrayList[position]
        val id = model.id
        val categoryname = model.categoryname

        holder.categoryTv.text = categoryname

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlacesUserActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("categoryname", categoryname)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    inner class HolderCategoryUser(itemView: View): RecyclerView.ViewHolder(itemView) {
        var categoryTv: TextView = binding.categoryTv
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCategoryUser(filterList, this)
        }
        return filter as FilterCategoryUser
    }
}