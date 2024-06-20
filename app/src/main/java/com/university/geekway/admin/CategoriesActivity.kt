package com.university.geekway.admin

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.BaseActivity
import com.university.geekway.adapters.AdapterCategory
import com.university.geekway.databinding.ActivityCategoriesBinding
import com.university.geekway.models.Categories
import java.lang.Exception

class CategoriesActivity : BaseActivity() {

    private lateinit var categoriesArrayList: ArrayList<Categories>
    private lateinit var adapterCategory: AdapterCategory
    private lateinit var binding: ActivityCategoriesBinding

    private var cityId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Проверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.buttonCancelCategories.setOnClickListener {
            onBackPressed()
        }

        binding.editCategoryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        loadCategories()
    }

    private fun loadCategories() {
        val intent = intent
        cityId = intent.getStringExtra("cityId")!!
        categoriesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref?.orderByChild("cityId").equalTo(cityId)
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Categories::class.java)
                    categoriesArrayList.add(model!!)
                }
                adapterCategory = AdapterCategory(this@CategoriesActivity, categoriesArrayList)
                binding.categoriesRv.adapter = adapterCategory
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}