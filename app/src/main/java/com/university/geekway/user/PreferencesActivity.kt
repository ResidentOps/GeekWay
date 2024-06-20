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
import com.university.geekway.adapters.AdapterCategoryPreference
import com.university.geekway.databinding.ActivityPreferencesBinding
import com.university.geekway.models.Categories
import java.lang.Exception

class PreferencesActivity : BaseActivity() {

    private lateinit var binding: ActivityPreferencesBinding
    private lateinit var categoriesArrayList: ArrayList<Categories>
    private lateinit var adapterCategoryPreferences: AdapterCategoryPreference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.editCategoryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategoryPreferences.filter!!.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.buttonCancelPreferences.setOnClickListener {
            onBackPressed()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        loadPreferences()
    }

    private fun loadPreferences() {
        categoriesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Preferences")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoriesArrayList.clear()
                    for (ds in snapshot.children) {
                        val categoryId = "${ds.child("id").value}"
                        val modelCategories = Categories()
                        modelCategories.id = categoryId
                        categoriesArrayList.add(modelCategories)
                    }
                    adapterCategoryPreferences = AdapterCategoryPreference(this@PreferencesActivity, categoriesArrayList)
                    binding.preferencesRv.adapter = adapterCategoryPreferences
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}