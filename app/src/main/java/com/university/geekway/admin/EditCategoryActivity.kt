package com.university.geekway.admin

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityEditCategoryBinding

class EditCategoryActivity : BaseActivity() {

    private lateinit var binding: ActivityEditCategoryBinding
    private var categoryId = ""
    private lateinit var cityTitleArrayList: ArrayList<String>
    private lateinit var cityIdArrayList: ArrayList<String>

    private companion object {
        private const val TAG = "CATEGORY_EDIT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        categoryId = intent.getStringExtra("categoryId")!!

        binding.buttonCancelEditCategory.setOnClickListener {
            finish()
        }

        binding.cityTv.setOnClickListener {
            cityDialog()
        }

        binding.buttonSaveCategory.setOnClickListener {
            saveData()
        }

        loadCities()
        loadCategoryInfo()
    }

    private fun loadCategoryInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCityId = snapshot.child("cityId").value.toString()
                    val categoryname = snapshot.child("categoryname").value.toString()

                    binding.editCategoryName.setText(categoryname)

                    val refCategoryCity = FirebaseDatabase.getInstance().getReference("Cities")
                    refCategoryCity.child(selectedCityId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val cityname = snapshot.child("cityname").value
                                binding.cityTv.text = cityname.toString()
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private var categoryname = ""
    private fun saveData() {
        categoryname = binding.editCategoryName.text.toString().trim()
        selectedCityTitle = binding.cityTv.text.toString().trim()
        if (categoryname.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_editCategory_emptyName), Toast.LENGTH_SHORT).show()
        } else if (selectedCityId.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_editCategory_emptyCity), Toast.LENGTH_SHORT).show()
        } else {
                updateCategory()
            }
        }

    private fun updateCategory() {
        Log.d(TAG, "validateData: validating")
        val hashMap = hashMapOf<String, Any>()
        hashMap["categoryname"] = "$categoryname"
        hashMap["cityId"] = "$selectedCityId"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(categoryId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, this.resources.getString(R.string.text_editCategory_complite), Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(this, this.resources.getString(R.string.text_editCategory_not_complite), Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCityId = ""
    private var selectedCityTitle = ""
    private fun cityDialog() {
        val citiesArrayList = arrayOfNulls<String>(cityTitleArrayList.size)
        for (i in cityTitleArrayList.indices) {
            citiesArrayList[i] = cityTitleArrayList[i]
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.text_add_place_chose_city)
            .setItems(citiesArrayList) { dialog, position ->
                selectedCityId = cityIdArrayList[position]
                selectedCityTitle = cityTitleArrayList[position]
                binding.cityTv.text = selectedCityTitle
            }
            .show()
    }

    private fun loadCities() {
        Log.d(TAG, "Load cities: loading cities")
        cityTitleArrayList = ArrayList()
        cityIdArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cityTitleArrayList.clear()
                cityIdArrayList.clear()
                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val cityname = "${ds.child("cityname").value}"
                    cityIdArrayList.add(id)
                    cityTitleArrayList.add(cityname)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}