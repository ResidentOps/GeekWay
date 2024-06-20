package com.university.geekway.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityEditPlaceBinding
import java.util.Locale

class EditPlaceActivity : BaseActivity() {

    private lateinit var binding: ActivityEditPlaceBinding
    private var placeId = ""
    private var imageUri: Uri? = null
    private lateinit var categoryTitleArrayList: ArrayList<String>
    private lateinit var categoryIdArrayList: ArrayList<String>
    private lateinit var cityTitleArrayList: ArrayList<String>
    private lateinit var cityIdArrayList: ArrayList<String>

    private companion object {
       private const val TAG = "PLACE_EDIT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        placeId = intent.getStringExtra("id")!!

        binding.buttonCancelEditPlace.setOnClickListener {
            finish()
        }

        binding.categoryTv.setOnClickListener {
            if (cityPicked != 0) {
                categoryDialog()
            }
            else
                Toast.makeText(this, this.resources.getString(R.string.error_add_place_city_not_picked), Toast.LENGTH_SHORT).show()
        }

        binding.cityTv.setOnClickListener {
            cityDialog()
        }

        binding.imageView.setOnClickListener {
            addImage()
        }

        binding.buttonSavePlace.setOnClickListener {
            saveData()
        }

        loadCities()
        loadPlaceInfo()
    }

    private fun loadPlaceInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    selectedCityId = snapshot.child("cityId").value.toString()
                    val placedescription = snapshot.child("placedescription").value.toString()
                    val placename = snapshot.child("placename").value.toString()
                    //val placepublic = snapshot.child("placePublic").value.toString()
                    val placeweb = snapshot.child("placeWeb").value.toString()
                    val placeaddress = snapshot.child("placeAddress").value.toString()
                    val placetime = snapshot.child("placeTime").value.toString()
                    val placeage = snapshot.child("placeAge").value.toString()
                    val placetelephone = snapshot.child("placeTelephone").value.toString()
                    val placeImage = "${snapshot.child("placeImage").value}"

                    binding.editPlaceName.setText(placename)
                    binding.editPlaceDescription.setText(placedescription)
                    //binding.editPlacePublic.setText(placepublic)
                    binding.editPlaceWeb.setText(placeweb)
                    binding.editPlaceAddress.setText(placeaddress)
                    binding.editPlaceTime.setText(placetime)
                    binding.editPlaceTelephone.setText(placetelephone)
                    binding.editPlaceAge.setText(placeage)

                    try {
                        Glide.with(this@EditPlaceActivity)
                            .load(placeImage)
                            .placeholder(R.drawable.ic_image)
                            .into(binding.imageView)
                    }
                    catch (e: Exception) {
                    }

                    val refPlaceCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refPlaceCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val categoryname = snapshot.child("categoryname").value
                                binding.categoryTv.text = categoryname.toString()
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    val refPlaceCity = FirebaseDatabase.getInstance().getReference("Cities")
                    refPlaceCity.child(selectedCityId)
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

    private val IMAGE_PICK_CODE = 1000
    private val PERMISSION_CODE = 1001
    private fun addImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                pickImageFromGallery()
            }
        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, AddPlaceActivity.IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, this.resources.getString(R.string.error_add_place_gallery_optin), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data!!
            binding.imageView.setImageURI(data?.data)
            Toast.makeText(this, this.resources.getString(R.string.text_add_place_photo_added), Toast.LENGTH_SHORT).show()
        }
    }

    private var placename = ""
    private var placedescription = ""
    private var placeweb = ""
    //private var placepublic = ""
    private var placeaddress = ""
    private var placetime = ""
    private var placetelephone = ""
    private var placeage = ""
    private var placeLat = ""
    private var placeLng = ""
    private fun saveData() {
        placename = binding.editPlaceName.text.toString().trim()
        placedescription = binding.editPlaceDescription.text.toString().trim()
        selectedCityTitle = binding.cityTv.text.toString().trim()
        selectedCategoryTitle = binding.categoryTv.text.toString().trim()
        //placepublic = binding.editPlacePublic.text.toString().trim()
        placeweb = binding.editPlaceWeb.text.toString().trim()
        placeaddress = binding.editPlaceAddress.text.toString().trim()
        placetime = binding.editPlaceTime.text.toString().trim()
        placetelephone = binding.editPlaceTelephone.text.toString().trim()
        placeage = binding.editPlaceAge.text.toString().trim()

        if (placeaddress != "") {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addList = geocoder.getFromLocationName(placeaddress, 1)
            placeLat = addList[0].latitude.toString()
            placeLng = addList[0].longitude.toString()
        }
        else {
            placeLat = ""
            placeLng = ""
        }

        if (placename.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_name_empty), Toast.LENGTH_SHORT).show()
        } else if (placedescription.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_discrip_empty), Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_category_empty), Toast.LENGTH_SHORT).show()
        } else if (selectedCityId.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_city_empty), Toast.LENGTH_SHORT).show()
        } else if (placeage.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_age_empty), Toast.LENGTH_SHORT).show()
        } else if ((cityPicked != 0) and (categoryPicked != 1)) {
        Toast.makeText(this, this.resources.getString(R.string.error_editPlace_new_cat), Toast.LENGTH_SHORT).show()
        }
        else {
            if (imageUri == null) {
                updatePlace("")
            } else {
                updateImagePlaceStorage()
            }
        }
    }

    private fun updatePlace(uploadedImagePlaceUrl: String) {
        Log.d(TAG, "validateData: validating")
        val hashMap = hashMapOf<String, Any>()
        hashMap["placename"] = "$placename"
        hashMap["placedescription"] = "$placedescription"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["cityId"] = "$selectedCityId"
        if (imageUri != null) {
            hashMap["placeImage"] = uploadedImagePlaceUrl
        }
        //hashMap["placePublic"] = "$placepublic"
        hashMap["placeWeb"] = "$placeweb"
        hashMap["placeAddress"] = "$placeaddress"
        hashMap["placeTime"] = "$placetime"
        hashMap["placeTelephone"] = "$placetelephone"
        hashMap["placeAge"] = "$placeage"
        hashMap["placeLat"] = "$placeLat"
        hashMap["placeLng"] = "$placeLng"

        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, this.resources.getString(R.string.text_editPlace_complite), Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(this, this.resources.getString(R.string.text_editPlace_not_complite), Toast.LENGTH_SHORT).show()
            }
    }

    //Добавление картинки места в БД Storage
    private fun updateImagePlaceStorage() {
        val filePathAndName = "Places/$placename"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImagePlaceUrl = "${uriTask.result}"
                updatePlace(uploadedImagePlaceUrl)
            }
            .addOnFailureListener {
                Log.d("TAG", "uploadImagePlace: Fail")
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private var categoryPicked = 0
    private fun categoryDialog() {
        val categoriesArrayList = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArrayList[i] = categoryTitleArrayList[i]
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.text_add_place_chose_category)
            .setItems(categoriesArrayList) { dialog, position ->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]
                categoryPicked = 1
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private var selectedCityId = ""
    private var selectedCityTitle = ""
    private var pickedCityId = ""
    private var cityPicked = 0
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
                pickedCityId = selectedCityId
                cityPicked = 1
                binding.cityTv.text = selectedCityTitle
                loadCategories()
            }
            .show()
    }

    private fun loadCategories() {
        Log.d(TAG, "Load categories: loading categories")
        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.orderByChild("cityId").equalTo(pickedCityId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()
                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val categoryname = "${ds.child("categoryname").value}"
                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(categoryname)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
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