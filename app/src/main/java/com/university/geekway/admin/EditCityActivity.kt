package com.university.geekway.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityEditCityBinding

class EditCityActivity : BaseActivity() {

    private lateinit var binding: ActivityEditCityBinding
    private var cityId = ""
    private var imageUri: Uri? = null

    private companion object {
        private const val TAG = "CITY_EDIT_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        cityId = intent.getStringExtra("cityId")!!

        binding.buttonCancelEditCity.setOnClickListener {
            finish()
        }

        binding.imageView.setOnClickListener {
            addImage()
        }

        binding.buttonSaveCity.setOnClickListener {
            saveData()
        }

        loadCityInfo()
    }

    private fun loadCityInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(cityId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val citydescription = snapshot.child("citydescription").value.toString()
                    val cityname = snapshot.child("cityname").value.toString()
                    val cityImage = "${snapshot.child("cityImage").value}"

                    binding.editCityName.setText(cityname)
                    binding.editCityDescription.setText(citydescription)

                    try {
                        Glide.with(this@EditCityActivity)
                            .load(cityImage)
                            .placeholder(R.drawable.ic_image)
                            .into(binding.imageView)
                    }
                    catch (e: Exception) {
                    }
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
                    Toast.makeText(this, this.resources.getString(R.string.error_gallery_optin), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data!!
            binding.imageView.setImageURI(data?.data)
            Toast.makeText(this, this.resources.getString(R.string.text_photo_added), Toast.LENGTH_SHORT).show()
        }
    }

    private var cityname = ""
    private var citydescription = ""
    private fun saveData() {
        cityname = binding.editCityName.text.toString().trim()
        citydescription = binding.editCityDescription.text.toString().trim()

        if (cityname.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_editCity_emptyName), Toast.LENGTH_SHORT).show()
        } else if (citydescription.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_editCity_emptyDesc), Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateCity("")
            } else {
                updateImageCityStorage()
            }
        }
    }

    private fun updateCity(uploadedImageCityUrl: String) {
        if (!cityname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_city_name_right), Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "validateData: validating")
        val hashMap = hashMapOf<String, Any>()
        hashMap["cityname"] = "$cityname"
        hashMap["citydescription"] = "$citydescription"
        if (imageUri != null) {
            hashMap["cityImage"] = uploadedImageCityUrl
        }

        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(cityId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, this.resources.getString(R.string.text_editCity_complite), Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(this, this.resources.getString(R.string.text_editCity_not_complite), Toast.LENGTH_SHORT).show()
            }
    }

    //Добавление картинки места в БД Storage
    private fun updateImageCityStorage() {
        if (!cityname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_city_name_right), Toast.LENGTH_SHORT).show()
            return
        }
        val filePathAndName = "Cities/$cityname"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImagePlaceUrl = "${uriTask.result}"
                updateCity(uploadedImagePlaceUrl)
            }
            .addOnFailureListener {
                Log.d("TAG", "uploadImagePlace: Fail")
            }
    }
}