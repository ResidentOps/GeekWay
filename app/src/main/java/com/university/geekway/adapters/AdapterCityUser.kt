package com.university.geekway.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.university.geekway.models.Cities
import com.university.geekway.user.CityDetailsActivity
import com.university.geekway.databinding.RowCitiesUserBinding
import com.university.geekway.filters.FilterCityUser
import com.university.geekway.loadImage

class AdapterCityUser : RecyclerView.Adapter<AdapterCityUser.HolderCityUser>, Filterable {

    private var context: Context
    public var citiesArrayList: ArrayList<Cities>
    private lateinit var binding: RowCitiesUserBinding
    public var filterList: ArrayList<Cities>
    private var filter: FilterCityUser? = null

    constructor(context: Context, citiesArrayList: ArrayList<Cities>) : super() {
        this.context = context
        this.citiesArrayList = citiesArrayList
        this.filterList = citiesArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCityUser {
        binding = RowCitiesUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCityUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCityUser, position: Int) {
        val model = citiesArrayList[position]
        val cityId = model.id
        val cityname = model.cityname

        holder.citynameTv.text = cityname
        holder.cityImage.loadImage(model.cityImage)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CityDetailsActivity::class.java)
            intent.putExtra("cityId", cityId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return citiesArrayList.size
    }

    inner class HolderCityUser(itemView: View): RecyclerView.ViewHolder(itemView) {
        var citynameTv = binding.citynameTv
        var cityImage = binding.imagecityTv
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCityUser(filterList, this)
        }
        return filter as FilterCityUser
    }
}