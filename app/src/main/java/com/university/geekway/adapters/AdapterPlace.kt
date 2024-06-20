package com.university.geekway.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.university.geekway.R
import com.university.geekway.admin.EditPlaceActivity
import com.university.geekway.models.Places
import com.university.geekway.databinding.RowPlacesBinding
import com.university.geekway.filters.FilterPlace
import com.university.geekway.loadImage

class AdapterPlace : RecyclerView.Adapter<AdapterPlace.HolderPlace>, Filterable {

    private var context: Context
    public var placesArrayList: ArrayList<Places>
    private var filterList: ArrayList<Places>
    private var filter: FilterPlace? = null
    private lateinit var binding: RowPlacesBinding

    constructor(context: Context, placesArrayList: ArrayList<Places>) : super() {
        this.context = context
        this.placesArrayList = placesArrayList
        this.filterList = placesArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPlace {
        binding = RowPlacesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPlace(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPlace, position: Int) {
        val model = placesArrayList[position]
        val categoryId = model.categoryId
        val cityId = model.cityId
        val placename = model.placename
        val placeRating = model.placeRating
        val placeComments = model.placeComments

        holder.placenameTv.text = placename
        loadCategory(categoryId, holder.categoryTv)
        loadCity(cityId, holder.cityTv)

        if (placeRating != "") {
            holder.ratingTv.text = placeRating
        } else {
            holder.ratingTv.text = context.getText(R.string.text_ratind_0)
        }

        if (placeComments != "") {
            holder.commentsTv.text = placeComments
        } else {
            holder.ratingTv.text = context.getText(R.string.text_comments_0)
        }

        holder.placeImage.loadImage(model.placeImage)

        holder.btnMore.setOnClickListener {
            moreOptionsDialog(model, holder)
        }
    }

    private fun moreOptionsDialog(model: Places, holder: HolderPlace) {
        val placeId = model.id
        val placeImage = model.placeImage
        val placename = model.placename
        val options = arrayOf(context.getText(R.string.text_edit_place), context.getText(R.string.text_delite_place))
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.text_chose_option)
            .setItems(options) {dialog, position ->
                if (position == 0) {
                    val intent = Intent(context, EditPlaceActivity::class.java)
                    intent.putExtra("id", placeId)
                    context.startActivity(intent)
                } else if (position == 1) {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle(R.string.text_delite_place_opt)
                    builder.setTitle(R.string.text_shure_delite_place)
                        .setPositiveButton(R.string.text_yes_delite_place) { a, d->
                            Toast.makeText(context, (R.string.text_deliting_place), Toast.LENGTH_SHORT).show()
                            deletePlace(context, placeImage, placeId, placename)
                        }
                        .setNegativeButton(R.string.text_cancel_delite_place) { a, d ->
                            a.dismiss()
                        }
                        .show()
                }
            }
            .show()
    }

    private fun deletePlace(context: Context, placeImage: String, placeId: String, placename: String) {
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(placeImage)
        storageReference.delete()
            .addOnSuccessListener {
                val ref = FirebaseDatabase.getInstance().getReference("Places")
                ref.child(placeId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, (R.string.text_deliting_place_complite), Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener {
                        Toast.makeText(context, (R.string.text_deliting_place_error), Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, (R.string.text_deliting_place_error), Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return placesArrayList.size
    }

    inner class HolderPlace(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var placenameTv = binding.placenameTv
        var categoryTv = binding.categoryTv
        var cityTv = binding.cityTv
        var ratingTv = binding.ratingTv
        var commentsTv = binding.commentsTv
        var placeImage = binding.imageplaceTv
        var btnMore = binding.btnMore
    }

    private fun loadCategory(categoryId: String, categoryTv: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryname = "${snapshot.child("categoryname").value}"
                    categoryTv.text = categoryname
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
            filter = FilterPlace(filterList, this)
        }
        return filter as FilterPlace
    }
}