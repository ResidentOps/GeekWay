package com.university.geekway.user

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.BaseActivity
import com.university.geekway.adapters.AdapterPlaceRecommend
import com.university.geekway.databinding.ActivitySelectionBinding
import com.university.geekway.models.Comments
import com.university.geekway.models.Places
import com.university.geekway.models.RecommendPlaces

class SelectionActivity : BaseActivity() {

    private lateinit var binding: ActivitySelectionBinding
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var placesRecArrayList: ArrayList<RecommendPlaces>
    private lateinit var commentArrayList: ArrayList<Comments>
    private lateinit var adapterPlaceRecommend: AdapterPlaceRecommend
    private lateinit var firebaseAuth: FirebaseAuth
    private var isInMySelection = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Проверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            checkIsSelection()
        }

        binding.buttonCancelSelection.setOnClickListener {
            onBackPressed()
        }
    }

    private fun checkIsSelection() {
        val ref =FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Selection")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMySelection = snapshot.exists()
                    if (isInMySelection) {
                        loadSelection()
                    } else {
                        loadAllPlaces()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadAllPlaces() {
        placesArrayList = ArrayList()
        val refPl = FirebaseDatabase.getInstance().getReference("Places")
        refPl.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                placesArrayList.clear()
                for (ds in snapshot.children) {
                    val modelPlace = ds.getValue(Places::class.java)
                    if (modelPlace != null) {
                        placesArrayList.add(modelPlace)
                    }
                    val refSel = FirebaseDatabase.getInstance().getReference("Users")
                    refSel.child(firebaseAuth.uid!!).child("Places").child(modelPlace!!.id)
                        .setValue(modelPlace)
                        .addOnSuccessListener {
                            removeNegatives()
                            removeFavorites()
                            removeRating()
                            loadUserPlaces()
                        }
                        .addOnFailureListener {
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun removeNegatives() {
        placesArrayList = ArrayList()
        val refNeg = FirebaseDatabase.getInstance().getReference("Users")
        refNeg.child(firebaseAuth.uid!!).child("Negatives")
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                placesArrayList.clear()
                for (ds in snapshot.children) {
                    val modelNeg = ds.getValue(Places::class.java)
                    if (modelNeg != null) {
                        placesArrayList.add(modelNeg)
                    }
                    val refSel = FirebaseDatabase.getInstance().getReference("Users")
                    refSel.child(firebaseAuth.uid!!).child("Places").child(modelNeg!!.id)
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

    private fun removeFavorites() {
        placesArrayList = ArrayList()
        val refFav = FirebaseDatabase.getInstance().getReference("Users")
        refFav.child(firebaseAuth.uid!!).child("Favorites")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelFav = ds.getValue(Places::class.java)
                        if (modelFav != null) {
                            placesArrayList.add(modelFav)
                        }
                        val refSel = FirebaseDatabase.getInstance().getReference("Users")
                        refSel.child(firebaseAuth.uid!!).child("Places").child(modelFav!!.id)
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

    private fun removeRating() {
        commentArrayList = ArrayList()
        placesRecArrayList = ArrayList()
        val refPlace = FirebaseDatabase.getInstance().getReference("Places")
        refPlace.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesRecArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelPlace = ds.getValue(RecommendPlaces::class.java)
                        if (modelPlace != null) {
                            placesRecArrayList.add(modelPlace)
                        }
                        val refPlaceCom = FirebaseDatabase.getInstance().getReference("Places")
                        refPlaceCom.child(modelPlace!!.id).child("Comments").orderByChild("uid").equalTo(firebaseAuth.uid!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    commentArrayList.clear()
                                    for (ds in snapshot.children) {
                                        val modelRat = ds.getValue(Comments::class.java)
                                        if (modelRat != null) {
                                            commentArrayList.add(modelRat)
                                        }
                                        if ((modelRat!!.rating <= 2.5.toString()) || (modelRat!!.rating >= 3.0.toString())) {
                                            val refSel = FirebaseDatabase.getInstance().getReference("Users")
                                            refSel.child(firebaseAuth.uid!!).child("Places").child(modelPlace!!.id)
                                                .removeValue()
                                                .addOnSuccessListener {
                                                }
                                                .addOnFailureListener {
                                                }
                                        } else {
                                            Log.e("TAG","No Negative Place NegRat")
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadUserPlaces() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Places")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val placeId = "${ds.child("id").value}"
                        val placeRating = "${ds.child("placeRating").value}"
                        val modelPlace = Places()
                        modelPlace.id = placeId
                        modelPlace.placeRating = placeRating
                        placesArrayList.add(modelPlace)
                    }
                    placesArrayList.sortByDescending {
                        it.placeRating
                    }
                    adapterPlaceRecommend = AdapterPlaceRecommend(this@SelectionActivity, placesArrayList)
                    binding.placesRv.adapter = adapterPlaceRecommend
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadSelection() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Selection")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val placeId = "${ds.child("id").value}"
                        val modelPlace = Places()
                        modelPlace.id = placeId
                        placesArrayList.add(modelPlace)
                    }
                    adapterPlaceRecommend = AdapterPlaceRecommend(this@SelectionActivity, placesArrayList)
                    binding.placesRv.adapter = adapterPlaceRecommend
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}