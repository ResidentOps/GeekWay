package com.university.geekway.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.R
import com.university.geekway.user.PlaceDetailsActivity
import com.university.geekway.models.Places
import com.university.geekway.databinding.RowPlacesFavoriteBinding
import com.university.geekway.filters.FilterFavorite
import com.university.geekway.loadImage

class AdapterPlaceFavorite : RecyclerView.Adapter<AdapterPlaceFavorite.HolderPlaceFavorite>, Filterable {

    private val context: Context
    var placesArrayList: ArrayList<Places>
    private lateinit var binding: RowPlacesFavoriteBinding
    public lateinit var filterList: ArrayList<Places>
    private var filter: FilterFavorite? = null
    private var isInMyNegativePlaces = false

    constructor(context: Context, placesArrayList: ArrayList<Places>) {
        this.context = context
        this.placesArrayList = placesArrayList
        this.filterList = placesArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPlaceFavorite {
        binding = RowPlacesFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPlaceFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPlaceFavorite, position: Int) {
        val model = placesArrayList[position]

        loadPlaceDetails(model, holder)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlaceDetailsActivity::class.java)
            intent.putExtra("id", model.id)
            context.startActivity(intent)
        }

        holder.btnFavorite.setOnClickListener {
            removeFavorite(context, model.id)
        }
    }

    private fun loadPlaceDetails(model: Places, holder: HolderPlaceFavorite) {
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

                    model.isFavorite = true
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
            filter = FilterFavorite(filterList, this)
        }
        return filter as FilterFavorite
    }

    override fun getItemCount(): Int {
        return placesArrayList.size
    }

    inner class HolderPlaceFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var placenameTv = binding.placenameTv
        var categoryTv = binding.categoryTv
        var cityTv = binding.cityTv
        var ratingTv = binding.ratingTv
        var commentsTv = binding.commentsTv
        var placeImage = binding.imageplaceTv
        var btnFavorite = binding.btnFavorite
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

    private fun removeFavorite(context: Context, placeId: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(placeId)
            .removeValue().addOnSuccessListener {
                Toast.makeText(context, R.string.text_delite_placeFav, Toast.LENGTH_SHORT).show()
                checkAlreadyNegativePlace(placeId)
            }
            .addOnFailureListener {
                Toast.makeText(context, R.string.error_delite_placeFav, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAlreadyNegativePlace(placeId: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyNegativePlaces = snapshot.exists()
                    if (isInMyNegativePlaces) {
                        Log.e("TAG","Already Negative Place")
                    } else {
                        addNegativePlace(placeId)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addNegativePlace(placeId: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = placeId
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
            .setValue(hashMap)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }
}