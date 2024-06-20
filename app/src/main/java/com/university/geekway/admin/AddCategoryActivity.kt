package com.university.geekway.admin

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.AdminActivity
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityAddCategoryBinding
import com.university.geekway.models.Cities

class AddCategoryActivity : BaseActivity() {

    private lateinit var textAddCategory: TextView
    private lateinit var etCategoryName: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnCancelAddCategory: Button
    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var citiesArrayList: ArrayList<Cities>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        textAddCategory = findViewById(R.id.textAddCategory)
        etCategoryName = findViewById(R.id.editCategoryName)
        btnAdd = findViewById(R.id.buttonAdd)
        btnCancelAddCategory = findViewById(R.id.buttonCancelAddCategory)

        auth = FirebaseAuth.getInstance()

        btnCancelAddCategory.setOnClickListener {
            finish()
        }

        btnAdd.setOnClickListener {
            addCategory()
        }

        binding.cityTv.setOnClickListener {
            cityPickDialog()
        }

        loadCities()
    }

    private fun loadCities() {
        Log.d("TAG", "Load cities: loading cities")
        citiesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                citiesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Cities::class.java)
                    citiesArrayList.add(model!!)
                    Log.d("TAG", "onDataChange: ${model.cityname}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private var selectedCityId = ""
    private var selectedCityTitle = ""
    private fun cityPickDialog() {
        Log.d("TAG", "cityPickDialog: Showing cities")
        val citiesArray = arrayOfNulls<String>(citiesArrayList.size)
        for (i in citiesArrayList.indices) {
            citiesArray[i] = citiesArrayList[i].cityname
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.text_chose_city)
            .setItems(citiesArray) {dialog, which ->
                selectedCityTitle = citiesArrayList[which].cityname
                selectedCityId = citiesArrayList[which].id
                binding.cityTv.text = selectedCityTitle
                Log.d("TAG", "cityPickDialog: Selected City ID: $selectedCityId")
                Log.d("TAG", "cityPickDialog: Selected City Title: $selectedCityTitle")
            }
            .show()
    }

    private var categoryname = ""
    private var cityname = ""
    private fun addCategory() {
        Log.d("TAG", "validateData: validating")
        categoryname = this.etCategoryName.text.toString().trim()
        cityname = binding.cityTv.text.toString().trim()
        if (categoryname.isEmpty() || cityname.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_category_fields_empty), Toast.LENGTH_SHORT).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        Log.d("TAG", "validateData: validating")
        val timestamp = System.currentTimeMillis()
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["categoryname"] = "$categoryname"
        hashMap["cityId"] = "$selectedCityId"
        hashMap["timestamp"] = timestamp

        if (!categoryname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_category_right), Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, this.resources.getString(R.string.text_add_category_complite), Toast.LENGTH_SHORT).show()
                val adminActivity = Intent(this, AdminActivity::class.java)
                startActivity(adminActivity)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, this.resources.getString(R.string.error_add_category_not_complite), Toast.LENGTH_SHORT).show()
            }
    }
}