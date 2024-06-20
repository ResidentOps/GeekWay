package com.university.geekway.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.R
import com.university.geekway.user.PlaceDetailsActivity
import com.university.geekway.models.Places
import com.university.geekway.databinding.RowPlacesRecommendsBinding
import com.university.geekway.filters.FilterRecommend
import com.university.geekway.loadImage

class AdapterPlaceRecommend : RecyclerView.Adapter<AdapterPlaceRecommend.HolderPlaceRecommend>, Filterable {

    private val context: Context
    var placesArrayList: ArrayList<Places>
    private lateinit var binding: RowPlacesRecommendsBinding
    public lateinit var filterList: ArrayList<Places>
    private var filter: FilterRecommend? = null

    constructor(context: Context, placesArrayList: ArrayList<Places>) {
        this.context = context
        this.placesArrayList = placesArrayList
        this.filterList = placesArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPlaceRecommend {
        binding = RowPlacesRecommendsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPlaceRecommend(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPlaceRecommend, position: Int) {
        val model = placesArrayList[position]

        loadPlaceDetails(model, holder)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlaceDetailsActivity::class.java)
            intent.putExtra("id", model.id)
            context.startActivity(intent)
        }

    }

    private fun loadPlaceDetails(model: Places, holder: HolderPlaceRecommend) {
        val placeId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val cityId = "${snapshot.child("cityId").value}"
                    val placename = "${snapshot.child("placename").value}"
                    val placeImage = "${snapshot.child("placeImage").value}"

                    val placeRating = "${snapshot.child("placeRating").value}"
                    val placeComments = "${snapshot.child("placeComments").value}"

                    model.isRecommend = true
                    model.placename = placename
                    model.placeImage = placeImage
                    model.categoryId = categoryId
                    model.cityId = cityId

                    model.placeRating = placeRating
                    model.placeComments = placeComments

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
                        holder.commentsTv.text = context.getText(R.string.text_comments_0)
                    }

                    holder.placeImage.loadImage(model.placeImage)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterRecommend(filterList, this)
        }
        return filter as FilterRecommend
    }

    override fun getItemCount(): Int {
        return placesArrayList.size
    }

    inner class HolderPlaceRecommend(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var placenameTv = binding.placenameTv
        var categoryTv = binding.categoryTv
        var cityTv = binding.cityTv

        var ratingTv = binding.ratingTv
        var commentsTv = binding.commentsTv

        var placeImage = binding.imageplaceTv
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
}