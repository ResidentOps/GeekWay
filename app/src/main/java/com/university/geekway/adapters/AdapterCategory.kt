package com.university.geekway.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.university.geekway.admin.EditCategoryActivity
import com.university.geekway.admin.PlacesActivity
import com.university.geekway.databinding.RowCategoriesBinding
import com.university.geekway.filters.FilterCategory
import com.university.geekway.models.Comments
import com.university.geekway.models.Places
import com.university.geekway.models.RecommendPlaces

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context
    public var categoryArrayList: ArrayList<Categories>
    private lateinit var placesArrayList: ArrayList<Places>
    private var filterList: ArrayList<Categories>
    private var filter: FilterCategory? = null
    private lateinit var binding: RowCategoriesBinding

    constructor(context: Context, categoryArrayList: ArrayList<Categories>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowCategoriesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val model = categoryArrayList[position]
        val id = model.id
        val categoryname = model.categoryname

        holder.categoryTv.text = categoryname

        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, EditCategoryActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("cityname", categoryname)
            context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.text_delite_category)
                .setMessage(R.string.text_shure_delite_category)
                .setPositiveButton(R.string.text_yes_delite_category) { a, d->
                    Toast.makeText(context, (R.string.text_deliting_category), Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                    deleteCategoryPlaces(model, holder)
                }
                .setNegativeButton(R.string.text_cancel_delite_category) { a, d ->
                    a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlacesActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("categoryname", categoryname)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: Categories, holder: HolderCategory) {
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, (R.string.text_deliting_category_complite), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, (R.string.text_deliting_category_error), Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCategoryPlaces(model: Categories, holder: HolderCategory) {
        val id = model.id
        placesArrayList = ArrayList()
        val refPl = FirebaseDatabase.getInstance().getReference("Places")
        refPl.orderByChild("categoryId").equalTo(id)
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
        return categoryArrayList.size
    }

    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView) {
        var categoryTv: TextView = binding.categoryTv
        var btnDelete: ImageButton = binding.btnDelete
        var btnEdit: ImageButton = binding.btnEdit
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}