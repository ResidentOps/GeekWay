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
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.university.geekway.AdminActivity
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityAddCityBinding

class AddCityActivity : BaseActivity() {

    private lateinit var binding: ActivityAddCityBinding
    private lateinit var textAddCity: TextView
    private lateinit var etCityName: EditText
    private lateinit var etCityDescription: EditText
    private lateinit var btnAdd: Button
    private lateinit var imageBtn: ImageView
    private lateinit var btnCancelAddCity: Button
    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        textAddCity = findViewById(R.id.textAddCity)
        etCityName = findViewById(R.id.editCityName)
        etCityDescription = findViewById(R.id.editCityDescription)
        btnAdd = findViewById(R.id.buttonAdd)
        imageBtn = findViewById(R.id.image_viewCity)
        btnCancelAddCity = findViewById(R.id.buttonCancelAddCity)

        auth = FirebaseAuth.getInstance()

        imageBtn.setOnClickListener {
            addImage()
        }

        btnCancelAddCity.setOnClickListener {
            finish()
        }

        btnAdd.setOnClickListener {
            addCity()
        }
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
                    Toast.makeText(this, this.resources.getString(R.string.error_gallery_optin), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data!!
            binding.imageViewCity.setImageURI(data?.data)
            Toast.makeText(this, this.resources.getString(R.string.text_photo_added), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImageCityStorage() {
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Cities/$cityname"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImagePlaceUrl = "${uriTask.result}"
                addCityFirebase(uploadedImagePlaceUrl, timestamp)
            }
            .addOnFailureListener {
                Log.d("TAG", "uploadImagePlace: Fail")
            }
    }

    private var cityname = ""
    private var citydescription = ""
    private fun addCity() {
        cityname = this.etCityName.text.toString().trim()
        citydescription = this.etCityDescription.text.toString().trim()
        if (cityname.isEmpty() || citydescription.isEmpty()) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_city_fields_empty), Toast.LENGTH_SHORT).show()
        } else if ( imageUri == null) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_city_photo_empty), Toast.LENGTH_SHORT).show()
        } else {
            addImageCityStorage()
        }
    }

    private fun addCityFirebase(uploadedImageCityUrl: String, timestamp: Long) {
        val timestamp = System.currentTimeMillis()
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["cityname"] = cityname
        hashMap["citydescription"] = citydescription
        hashMap["cityImage"] = "$uploadedImageCityUrl"
        hashMap["timestamp"] = timestamp

        if (!cityname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_add_city_name_right), Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, this.resources.getString(R.string.text_add_city_complite), Toast.LENGTH_SHORT).show()
                val adminActivity = Intent(this, AdminActivity::class.java)
                startActivity(adminActivity)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, this.resources.getString(R.string.error_add_city_not_complite), Toast.LENGTH_SHORT).show()
            }
    }
}