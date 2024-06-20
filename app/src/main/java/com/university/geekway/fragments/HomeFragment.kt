package com.university.geekway.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.*
import com.university.geekway.adapters.AdapterPlaceRecommend
import com.university.geekway.databinding.FragmentHomeBinding
import com.university.geekway.models.Places

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var adapterPlaceRecommend: AdapterPlaceRecommend
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(context), container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        loadRecommends()
        return binding.root
    }

    //Загрузка и отображение списка подобранных мест
    private fun loadRecommends() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!).child("Recommends")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val placeId = "${ds.child("placeId").value}"
                        val modelPlace = Places()
                        modelPlace.id = placeId
                        placesArrayList.add(modelPlace)
                    }
                    adapterPlaceRecommend = AdapterPlaceRecommend(HOME, placesArrayList)
                    binding.recommendsRv.adapter = adapterPlaceRecommend
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}