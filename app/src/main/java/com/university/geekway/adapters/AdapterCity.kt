package com.university.geekway.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.R
import com.university.geekway.admin.CategoriesActivity
import com.university.geekway.admin.EditCityActivity
import com.university.geekway.databinding.RowCitiesBinding
import com.university.geekway.filters.FilterCity
import com.university.geekway.models.Categories
import com.university.geekway.models.Cities
import com.university.geekway.models.Places

class AdapterCity : RecyclerView.Adapter<AdapterCity.HolderCity>, Filterable {

    private val context: Context
    public var cityArrayList: ArrayList<Cities>
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var categoryArrayList: ArrayList<Categories>
    private var filterList: ArrayList<Cities>
    private var filter: FilterCity? = null
    private lateinit var binding: RowCitiesBinding

    constructor(context: Context, cityArrayList: ArrayList<Cities>) {
        this.context = context
        this.cityArrayList = cityArrayList
        this.filterList = cityArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCity {
        binding = RowCitiesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCity(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCity, position: Int) {
        val model = cityArrayList[position]
        val cityId = model.id
        val cityname = model.cityname

        holder.cityTv.text = cityname

        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, EditCityActivity::class.java)
            intent.putExtra("cityId", cityId)
            intent.putExtra("cityname", cityname)
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CategoriesActivity::class.java)
            intent.putExtra("cityId", cityId)
            context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.text_delite_city)
                .setMessage(R.string.text_shure_delite_city)
                .setPositiveButton(R.string.text_yes) { a, d->
                    Toast.makeText(context, (R.string.text_deliting), Toast.LENGTH_SHORT).show()
                    deleteCity(model, holder)
                    deleteCityCategories(model, holder)
                    deleteCityPlaces(model, holder)
                }
                .setNegativeButton(R.string.text_cancel_delite_city) { a, d ->
                    a.dismiss()
                }
                .show()
        }
    }

    private fun deleteCity(model: Cities, holder: HolderCity) {
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, (R.string.text_deliting_complite), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, (R.string.text_deliting_error), Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCityCategories(model: Cities, holder: HolderCity) {
        val id = model.id
        categoryArrayList = ArrayList()
        val refCa = FirebaseDatabase.getInstance().getReference("Categories")
        refCa.orderByChild("cityId").equalTo(id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelC = ds.getValue(Categories::class.java)
                        if (modelC != null) {
                            categoryArrayList.add(modelC)
                        }
                        val ref = FirebaseDatabase.getInstance().getReference("Categories")
                        ref.child(modelC!!.id)
                            .removeValue()
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun deleteCityPlaces(model: Cities, holder: HolderCity) {
        val id = model.id
        placesArrayList = ArrayList()
        val refPl = FirebaseDatabase.getInstance().getReference("Places")
        refPl.orderByChild("cityId").equalTo(id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelP = ds.getValue(Places::class.java)
                        if (modelP != null) {
                            placesArrayList.add(modelP)
                        }
                        val ref = FirebaseDatabase.getInstance().getReference("Places")
                        ref.child(modelP!!.id)
                            .removeValue()
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun getItemCount(): Int {
        return cityArrayList.size
    }

    inner class HolderCity(itemView: View): RecyclerView.ViewHolder(itemView) {
        var cityTv: TextView = binding.cityTv
        var btnDelete: ImageButton = binding.btnDelete
        var btnEdit: ImageButton = binding.btnEdit
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCity(filterList, this)
        }
        return filter as FilterCity
    }
}