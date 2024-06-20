package com.university.geekway.user

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.*
import com.university.geekway.adapters.AdapterPlaceFavorite
import com.university.geekway.databinding.ActivityFavoritesBinding
import com.university.geekway.models.Cities
import com.university.geekway.models.Places

class FavoritesActivity : BaseActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var adapterPlaceFavorite: AdapterPlaceFavorite
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.editPlaceName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPlaceFavorite.filter!!.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.buttonCancelFavorites.setOnClickListener {
            onBackPressed()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        loadFavorites()
    }

    private fun loadFavorites() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val placeId = "${ds.child("id").value}"
                        val modelPlace = Places()
                        modelPlace.id = placeId
                        placesArrayList.add(modelPlace)
                    }
                    adapterPlaceFavorite = AdapterPlaceFavorite(this@FavoritesActivity, placesArrayList)
                    binding.favoritesRv.adapter = adapterPlaceFavorite
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}