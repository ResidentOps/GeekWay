package com.university.geekway.admin

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.BaseActivity
import com.university.geekway.adapters.AdapterPlace
import com.university.geekway.databinding.ActivityPlacesBinding
import com.university.geekway.models.Places
import java.lang.Exception

class PlacesActivity : BaseActivity() {

    private lateinit var binding: ActivityPlacesBinding
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var adapterPlace: AdapterPlace

    private companion object {
        const val TAG = "PLACES_LIST_TAG"
    }

    private var categoryId = ""
    private var categoryname = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Проверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.buttonCancelPlaces.setOnClickListener {
            onBackPressed()
        }

        binding.editPlaceName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPlace.filter!!.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        loadPlaces()
    }

    private fun loadPlaces() {
        val intent = intent
        categoryId= intent.getStringExtra("categoryId")!!
        categoryname= intent.getStringExtra("categoryname")!!
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref?.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(Places::class.java)
                        if (model != null) {
                            placesArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.placename} ${model.categoryname} ${model.placeImage}")
                        }
                    }
                    adapterPlace = AdapterPlace(this@PlacesActivity, placesArrayList)
                    binding.placesRv.adapter = adapterPlace
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}