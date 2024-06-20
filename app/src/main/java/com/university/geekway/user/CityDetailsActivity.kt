package com.university.geekway.user

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityCityDetailsBinding
import java.io.IOException

class CityDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityCityDetailsBinding
    private lateinit var cityName: TextView
    private lateinit var cityDescription: TextView
    private lateinit var cityPhoto: ImageView
    private var cityId = ""
    private var originalCityNameDB: String = ""
    private var originalCityDescriptionDB: String = ""
    private var isTranslated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Проверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        cityId = intent.getStringExtra("cityId")!!

        cityName = findViewById(R.id.textCityName)
        cityDescription = findViewById(R.id.textCityDescription)
        cityPhoto = findViewById(R.id.image_viewCityPhoto)

        binding.btnTranslate.setOnClickListener {
            if (isTranslated) {
                val ref = FirebaseDatabase.getInstance().getReference("Cities")
                ref.child(cityId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (ds in snapshot.children) {
                                    val cityname = "${snapshot.child("cityname").value}"
                                    val citydescription = "${snapshot.child("citydescription").value}"

                                    cityName.text = cityname
                                    cityDescription.text = citydescription
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                isTranslated = false
            } else {
                showProgressDialog(resources.getString(R.string.text_progress))

                Toast.makeText(this, R.string.text_waitPlease, Toast.LENGTH_SHORT).show()

                originalCityNameDB = binding.textCityName.text.toString()
                originalCityDescriptionDB = binding.textCityDescription.text.toString()
                translateCity()

                hideProgressDialog()
            }
        }

        binding.btnCancelCityDetails.setOnClickListener {
            onBackPressed()
        }

        binding.butnCityCategories.setOnClickListener {
            val intent = Intent(this, CategoriesUserActivity::class.java)
            intent.putExtra("cityId", cityId)
            this.startActivity(intent)
        }

        loadCityDetails()
        downloadRusEngTranslator()
    }

    private fun translateCity() {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.RUSSIAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val rusEngTranslator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        rusEngTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Log.e("TAG","Downloaded")
            }
            .addOnFailureListener {
                Log.e("TAG","Error download")
            }
        rusEngTranslator.translate(originalCityNameDB).addOnSuccessListener {
            binding.textCityName.text = it
        }
        rusEngTranslator.translate(originalCityDescriptionDB).addOnSuccessListener {
            binding.textCityDescription.text = it
        }
            .addOnFailureListener {
                binding.textCityDescription.text = "Error ${it.message}"
            }
        isTranslated = true
    }

    //Загрузка и отображение информации о городе
    private fun loadCityDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(cityId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            val cityname = "${snapshot.child("cityname").value}"
                            val citydescription = "${snapshot.child("citydescription").value}"
                            val cityImage = "${snapshot.child("cityImage").value}"

                            cityName.text = cityname
                            cityDescription.text = citydescription

                            try {
                                Glide.with(this@CityDetailsActivity)
                                    .load(cityImage)
                                    .placeholder(R.drawable.ic_image)
                                    .into(cityPhoto)
                            }
                            catch (e: Exception) {
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun downloadRusEngTranslator() {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.RUSSIAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val rusEngTranslator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        rusEngTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Log.e("TAG","Downloaded")
                try {
                    binding.btnTranslate.setEnabled(true)
                } catch (e: IOException) {
                    Log.e("TAG", "Failed to initialize the translator.", e)
                    Toast.makeText(this,R.string.error_translatorInitial, Toast.LENGTH_SHORT).show()
                    binding.btnTranslate.setEnabled(false)
                    onBackPressed()
                }
            }
            .addOnFailureListener {
                Log.e("TAG","Error download")
                Log.e("TAG","Failed to download the translator.",)
                Toast.makeText(this,R.string.error_translatorDownload, Toast.LENGTH_SHORT).show()
                Toast.makeText(this,R.string.text_reqWIFI, Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
    }
}