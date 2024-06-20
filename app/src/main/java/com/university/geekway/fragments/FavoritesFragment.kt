package com.university.geekway.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.*
import com.university.geekway.adapters.AdapterPlaceFavorite
import com.university.geekway.adapters.AdapterPlaceUser
import com.university.geekway.databinding.FragmentFavoritesBinding
import com.university.geekway.models.Places
import java.lang.Exception

class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var adapterPlaceFavorite: AdapterPlaceFavorite
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapterPlaceUser: AdapterPlaceUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoritesBinding.inflate(LayoutInflater.from(context), container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadFavoritePlaces()

        binding.buttonCancelFavorites.setOnClickListener {
            HOME.navController.navigate(R.id.action_favoritesFragment_to_profileFragment)
        }

        //Поле "Поиск места"
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

        return binding.root
    }

    private fun loadFavoritePlaces() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val placeId = "${ds.child("placeId").value}"
                        val modelPlace = Places()
                        modelPlace.id = placeId
                        placesArrayList.add(modelPlace)
                    }
                    adapterPlaceFavorite = AdapterPlaceFavorite(HOME, placesArrayList)
                    binding.favoritesRv.adapter = adapterPlaceFavorite
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}