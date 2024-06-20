package com.university.geekway.user

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.BaseActivity
import com.university.geekway.adapters.AdapterCityUser
import com.university.geekway.databinding.ActivityGuideBinding
import com.university.geekway.models.Cities
import java.lang.Exception

class GuideActivity : BaseActivity() {

    private lateinit var citiesArrayList: ArrayList<Cities>
    private lateinit var adapterCityUser: AdapterCityUser
    private lateinit var binding: ActivityGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Проверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.buttonCancelDataBase.setOnClickListener {
            onBackPressed()
        }

        binding.editCityName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCityUser.filter.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        loadCities()
    }

    private fun loadCities() {
        citiesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                citiesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Cities::class.java)
                    citiesArrayList.add(model!!)
                }
                adapterCityUser = AdapterCityUser(this@GuideActivity, citiesArrayList)
                binding.citiesRv.adapter = adapterCityUser
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}