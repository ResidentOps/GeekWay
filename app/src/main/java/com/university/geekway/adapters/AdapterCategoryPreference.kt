package com.university.geekway.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.R
import com.university.geekway.models.Categories
import com.university.geekway.databinding.RowCategoriesPreferencesBinding
import com.university.geekway.filters.FilterPreference
import com.university.geekway.models.RecommendPlaces

class AdapterCategoryPreference : RecyclerView.Adapter<AdapterCategoryPreference.HolderCategoryPreferences>, Filterable {

    private val context: Context
    var categoriesArrayList: ArrayList<Categories>
    private lateinit var binding: RowCategoriesPreferencesBinding
    public lateinit var filterList: ArrayList<Categories>
    private lateinit var placesRecArrayList: ArrayList<RecommendPlaces>
    private var filter: FilterPreference? = null

    constructor(context: Context, categoriesArrayList: ArrayList<Categories>) {
        this.context = context
        this.categoriesArrayList = categoriesArrayList
        this.filterList = categoriesArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoryPreferences {
        binding = RowCategoriesPreferencesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategoryPreferences(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategoryPreferences, position: Int) {
        val model = categoriesArrayList[position]
        loadCategoryDetails(model, holder)
        holder.btnPreference.setOnClickListener {
            removePreferences(context, model.id)
        }
    }

    private fun loadCategoryDetails(model: Categories, holder: HolderCategoryPreferences) {
        val placeId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryname = "${snapshot.child("categoryname").value}"
                    val cityId = "${snapshot.child("cityId").value}"
                    model.isPreference = true
                    model.categoryname = categoryname
                    model.cityId = cityId
                    holder.categoryTv.text = categoryname
                    loadCity(cityId, holder.cityTv)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadCity(cityId: String, cityTv: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(cityId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cityname = "${snapshot.child("cityname").value}"
                    cityTv.text = cityname
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPreference(filterList, this)
        }
        return filter as FilterPreference
    }

    override fun getItemCount(): Int {
        return categoriesArrayList.size
    }

    inner class HolderCategoryPreferences(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryTv = binding.categoryTv
        var cityTv = binding.cityTv
        var btnPreference= binding.btnPreference
    }

    private fun removePreferences(context: Context, categoryId: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Preferences").child(categoryId)
            .removeValue().addOnSuccessListener {
                Toast.makeText(context, R.string.text_delite_categoryPref, Toast.LENGTH_SHORT).show()
                removeSelection(categoryId)
            }
            .addOnFailureListener {
                Toast.makeText(context, R.string.error_delite_categoryPref, Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeSelection(categoryId: String) {
        placesRecArrayList = ArrayList()
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesRecArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(RecommendPlaces::class.java)
                        if (model != null) {
                            placesRecArrayList.add(model)
                        }
                        val refSel = FirebaseDatabase.getInstance().getReference("Users")
                        refSel.child(firebaseAuth.uid!!).child("Selection").child(model!!.id)
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
        Toast.makeText(context, R.string.text_selectionUpdate, Toast.LENGTH_SHORT).show()
    }
}