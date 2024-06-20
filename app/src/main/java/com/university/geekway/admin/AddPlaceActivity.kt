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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.university.geekway.AdminActivity
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityAddPlaceBinding
import com.university.geekway.models.Categories
import com.university.geekway.models.Cities
import com.university.geekway.models.Favorites
import com.university.geekway.models.Places
import com.university.geekway.models.RecommendPlaces
import com.university.geekway.models.Users
import java.util.Locale

class AddPlaceActivity : BaseActivity() {

    private lateinit var binding: ActivityAddPlaceBinding
    private lateinit var etPlaceName: EditText
    private lateinit var etPlaceDescription: EditText
    private lateinit var imageBtn: ImageView
    private lateinit var btnAdd: Button
    private lateinit var btnCancelAddPlace: Button
    private var imageUri: Uri? = null
    private lateinit var citiesArrayList: ArrayList<Cities>
    private lateinit var categoriesArrayList: ArrayList<Categories>
    private lateinit var usersArrayList: ArrayList<Users>
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        etPlaceName = findViewById(R.id.editPlaceName)
        etPlaceDescription = findViewById(R.id.editPlaceDescription)
        imageBtn = findViewById(R.id.image_view)
        btnAdd = findViewById(R.id.buttonAdd)
        btnCancelAddPlace = findViewById(R.id.buttonCancelAddPlace)

        auth = FirebaseAuth.getInstance()

        btnCancelAddPlace.setOnClickListener {
            finish()
        }

        btnAdd.setOnClickListener {
            addPlace()
        }

        imageBtn.setOnClickListener {
            addImage()
        }

        binding.categoryTv.setOnClickListener {
            if (cityPicked != 0) {
                categoryPickDialog()
            }
            else
                Toast.makeText(this, this.resources.getString(R.string.error_add_place_city_not_picked), Toast.LENGTH_SHORT).show()
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

    private fun loadCategories() {
        categoriesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.orderByChild("cityId").equalTo(pickedCityId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Categories::class.java)
                    categoriesArrayList.add(model!!)
                    Log.d("TAG", "onDataChange: ${model.categoryname}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private var selectedCityId = ""
    private var selectedCityTitle = ""
    private var pickedCityId = ""
    private var cityPicked = 0
    private fun cityPickDialog() {
        Log.d("TAG", "cityPickDialog: Showing cities")
        val citiesArray = arrayOfNulls<String>(citiesArrayList.size)
        for (i in citiesArrayList.indices) {
            citiesArray[i] = citiesArrayList[i].cityname
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.text_add_place_chose_city)
            .setItems(citiesArray) {dialog, which ->
                selectedCityTitle = citiesArrayList[which].cityname
                selectedCityId = citiesArrayList[which].id
                pickedCityId = selectedCityId
                cityPicked = 1
                binding.cityTv.text = selectedCityTitle
                loadCategories()
                Log.d("TAG", "cityPickDialog: Selected City ID: $selectedCityId")
                Log.d("TAG", "cityPickDialog: Selected City Title: $selectedCityTitle")
            }
            .show()
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private fun categoryPickDialog() {
        Log.d("TAG", "categoryPickDialog: Showing categories")
        val categoriesArray = arrayOfNulls<String>(categoriesArrayList.size)
        for (i in categoriesArrayList.indices) {
            categoriesArray[i] = categoriesArrayList[i].categoryname
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.text_add_place_chose_category)
            .setItems(categoriesArray) {dialog, which ->
                selectedCategoryTitle = categoriesArrayList[which].categoryname
                selectedCategoryId = categoriesArrayList[which].id
                binding.categoryTv.text = selectedCategoryTitle
                Log.d("TAG", "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d("TAG", "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }

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
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        val IMAGE_PICK_CODE = 1000
        private val PERMISSION_CODE = 1001
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
    private var categoryname = ""
    private var cityname = ""
    //private var placepublic = ""
    private var placeweb = ""
    private var placeaddress = ""
    private var placetime = ""
    private var placetelephone = ""
    private var placeage = ""

    private var placeRating = ""
    private var placeComments = ""

    private var placeImage = ""
    private var placeLat = ""
    private var placeLng = ""
    private fun addPlace() {
        Log.d("TAG", "validateData: validating")
        placename = binding.editPlaceName.text.toString().trim()
        placedescription = binding.editPlaceDescription.text.toString().trim()
        categoryname = binding.categoryTv.text.toString().trim()
        cityname = binding.cityTv.text.toString().trim()
        placeage = binding.editPlaceAge.text.toString().trim()
        //placepublic = binding.editPlacePublic.text.toString().trim()
        placeweb = binding.editPlaceWeb.text.toString().trim()
        placeaddress = binding.editPlaceAddress.text.toString().trim()
        placetime = binding.editPlaceTime.text.toString().trim()
        placetelephone = binding.editPlaceTelephone.text.toString().trim()

        if (placeaddress != "") {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addList = geocoder.getFromLocationName(placeaddress, 1)
            placeLat = addList[0].latitude.toString()
            placeLng = addList[0].longitude.toString()
        }

        if (placename.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_name_empty), Toast.LENGTH_SHORT).show()
        } else if (placedescription.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_discrip_empty), Toast.LENGTH_SHORT).show()
        } else if (categoryname.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_category_empty), Toast.LENGTH_SHORT).show()
        } else if (cityname.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_city_empty), Toast.LENGTH_SHORT).show()
        } else if (placeage.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_age_empty), Toast.LENGTH_SHORT).show()
        } else if ( imageUri == null) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_place_photo_empty), Toast.LENGTH_SHORT).show()
        } else {
            addImagePlaceStorage()
        }
    }

    private fun addPlaceFirebase(uploadedImagePlaceUrl: String, timestamp: Long) {
        Log.d("TAG", "validateData: validating")
        val timestamp = System.currentTimeMillis()
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["placename"] = "$placename"
        hashMap["placedescription"] = "$placedescription"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["cityId"] = "$selectedCityId"
        hashMap["placeAge"] = "$placeage"
        hashMap["placeImage"] = "$uploadedImagePlaceUrl"
        //hashMap["placePublic"] = "$placepublic"
        hashMap["placeWeb"] = "$placeweb"
        hashMap["placeTime"] = "$placetime"
        hashMap["placeTelephone"] = "$placetelephone"
        hashMap["placeAddress"] = "$placeaddress"

        hashMap["placeRating"] = ""
        hashMap["placeComments"] = ""

        hashMap["placeLat"] = "$placeLat"
        hashMap["placeLng"] = "$placeLng"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                imageUri = null
                Toast.makeText(this, this.resources.getString(R.string.error_add_place_complite), Toast.LENGTH_SHORT).show()
                val adminActivity = Intent(this, AdminActivity::class.java)
                startActivity(adminActivity)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, this.resources.getString(R.string.error_add_place_not_complite), Toast.LENGTH_SHORT).show()
            }
    }

    private fun addImagePlaceStorage() {
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Places/$placename"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImagePlaceUrl = "${uriTask.result}"
                addPlaceFirebase(uploadedImagePlaceUrl, timestamp)
            }
            .addOnFailureListener {
                Log.d("TAG", "uploadImagePlace: Fail")
            }
    }
}