package com.university.geekway.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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
import com.university.geekway.adapters.AdapterComment
import com.university.geekway.databinding.ActivityPlaceDetailsBinding
import com.university.geekway.models.Comments
import com.university.geekway.models.Places
import com.university.geekway.models.RecommendPlaces
import java.io.IOException
import kotlin.math.roundToInt

class PlaceDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityPlaceDetailsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var commentArrayList: ArrayList<Comments>
    private lateinit var placesRecArrayList: ArrayList<RecommendPlaces>
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var adapterComment: AdapterComment
    private var placeId = ""
    private var isInMyFavorite = false
    private var isInMyNegativePlaces = false
    private var addedComment = false
    private var isTranslated = false
    private var originalPlaceNameDB: String = ""
    private var originalPlaceDescriptionDB: String = ""
    private var originalPlaceCategoryDB: String = ""
    private var originalPlaceCityDB: String = ""
    private var originalPlaceTimeDB:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        placeId = intent.getStringExtra("id")!!

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }

        binding.btnTranslate.setOnClickListener {
            if (isTranslated) {
                val ref = FirebaseDatabase.getInstance().getReference("Places")
                ref.child(placeId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val categoryId = "${snapshot.child("categoryId").value}"
                            val cityId = "${snapshot.child("cityId").value}"
                            val placename = "${snapshot.child("placename").value}"
                            val placedescription = "${snapshot.child("placedescription").value}"
                            val placetime = "${snapshot.child("placeTime").value}"
                            loadCategory(categoryId, binding.textPlaceCategoryDB)
                            loadCity(cityId, binding.textPlaceCityDB)
                            binding.textPlaceName.text = placename
                            binding.textPlaceDescription.text = placedescription

                            if (placetime != "") {
                                binding.textPlaceTimeBD.text = placetime
                            } else {
                                binding.textPlaceTimeBD.text = getText(R.string.text_noData)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                isTranslated = false
            } else {
                showProgressDialog(resources.getString(R.string.text_progress))

                Toast.makeText(this, R.string.text_waitPlease, Toast.LENGTH_SHORT).show()

                originalPlaceNameDB = binding.textPlaceName.text.toString()
                originalPlaceDescriptionDB = binding.textPlaceDescription.text.toString()
                originalPlaceCategoryDB = binding.textPlaceCategoryDB.text.toString()
                originalPlaceCityDB = binding.textPlaceCityDB.text.toString()
                originalPlaceTimeDB = binding.textPlaceTimeBD.text.toString()
                translatePlace()

                hideProgressDialog()
            }
        }

        binding.btnCancelPlaceDetails.setOnClickListener {
            onBackPressed()
        }

        binding.btnAddComment.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, R.string.text_placeDetails_authNot, Toast.LENGTH_SHORT).show()
            } else {
                checkAddedComment()
            }
        }

        binding.butnFavorite.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, R.string.text_placeDetails_authNot, Toast.LENGTH_SHORT).show()
            } else {
                if (isInMyFavorite) {
                    removeFavorite(this, placeId)
                } else {
                    checkDeleteNegativePlace()
                }
            }
        }

        loadPlaceDetails()
        showComments()
        downloadRusEngTranslator()
    }

    private fun translatePlace() {
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
        rusEngTranslator.translate(originalPlaceNameDB).addOnSuccessListener {
            binding.textPlaceName.text = it
        }
        rusEngTranslator.translate(originalPlaceDescriptionDB).addOnSuccessListener {
            binding.textPlaceDescription.text = it
        }
            .addOnFailureListener {
                binding.textPlaceDescription.text = "Error ${it.message}"
            }
        rusEngTranslator.translate(originalPlaceCategoryDB).addOnSuccessListener {
            binding.textPlaceCategoryDB.text = it
        }
            .addOnFailureListener {
                binding.textPlaceCategoryDB.text = "Error ${it.message}"
            }
        rusEngTranslator.translate(originalPlaceCityDB).addOnSuccessListener {
            binding.textPlaceCityDB.text = it
        }
            .addOnFailureListener {
                binding.textPlaceCityDB.text = "Error ${it.message}"
            }
        rusEngTranslator.translate(originalPlaceTimeDB).addOnSuccessListener {
            binding.textPlaceTimeBD.text = it
        }
            .addOnFailureListener {
                binding.textPlaceTimeBD.text = "Error ${it.message}"
            }
        isTranslated = true
    }

    private fun checkAddedComment() {
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId).child("Comments").orderByChild("uid").equalTo(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    addedComment = snapshot.exists()
                    if (addedComment) {
                        alertCommentDialog()
                    } else {
                        Toast.makeText(this@PlaceDetailsActivity, R.string.text_openingCommentDialog, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@PlaceDetailsActivity, AddCommentActivity::class.java)
                        intent.putExtra("id", placeId)
                        this@PlaceDetailsActivity.startActivity(intent)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun alertCommentDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.text_alertCommentDialoge)
            .setMessage(R.string.text_alertCommentDialog_shure)
            .setPositiveButton(R.string.text_alertCommentDialog_yes) {d,e ->
                newComment()
            }
            .setNegativeButton(R.string.text_alertCommentDialog_no) {d,e ->
                d.dismiss()
            }
            .show()
    }

    private fun newComment() {
        commentArrayList = ArrayList()
        val refCom = FirebaseDatabase.getInstance().getReference("Places")
        refCom.child(placeId).child("Comments").orderByChild("uid").equalTo(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelCom = ds.getValue(Comments::class.java)
                        if (modelCom != null) {
                            commentArrayList.add(modelCom)
                        }
                        val ref = FirebaseDatabase.getInstance().getReference("Places")
                        ref.child(placeId).child("Comments").child(modelCom!!.id)
                            .removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this@PlaceDetailsActivity, (R.string.text_oldComment_deliteComplite), Toast.LENGTH_SHORT).show()
                                Toast.makeText(this@PlaceDetailsActivity, R.string.text_openingCommentDialog, Toast.LENGTH_SHORT).show()
                                checkRating()
                                val intent = Intent(this@PlaceDetailsActivity, AddCommentActivity::class.java)
                                intent.putExtra("id", placeId)
                                this@PlaceDetailsActivity.startActivity(intent)
                            }
                            .addOnFailureListener {
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun checkRating() {
        var rating = ""
        val refCom = FirebaseDatabase.getInstance().getReference("Places")
        refCom.child(placeId).child("Comments").orderByChild("uid").equalTo(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    rating = "${snapshot.child("rating").value}"
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        if (rating >= 3.0.toString()) {
            Log.e("TAG","Not Negative Place")
        }

        if (rating <= 2.5.toString()) {
            checkDeleteNegativePlaceComment()
        }

        submitRating()
    }

    private fun checkDeleteNegativePlaceComment() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyNegativePlaces = snapshot.exists()
                    if (isInMyNegativePlaces) {
                        val refNeg = FirebaseDatabase.getInstance().getReference("Users")
                        refNeg.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
                            .removeValue()
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                            }
                    } else {
                        Log.e("TAG","Not Negative Place")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun submitRating() {
        val db = FirebaseDatabase.getInstance().getReference("Places")
        val dbRef = db.child(placeId).child("Comments")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var total = 0.0
                var count = 0
                var placeRating = ""
                var placeComments = ""
                for (ds in dataSnapshot.children) {
                    val rating = ds.child("rating").value.toString().toDouble()
                    total = total + rating
                    count = count + 1
                    placeRating = (((total / count) * 100).roundToInt().toDouble() / 100).toString()
                    placeComments = ((placeComments.toIntOrNull()?:0) + 1).toString()
                    val refRat = FirebaseDatabase.getInstance().getReference("Places")
                    refRat.child(placeId).child("placeRating").setValue(placeRating)
                    val refCom = FirebaseDatabase.getInstance().getReference("Places")
                    refCom.child(placeId).child("placeComments").setValue(placeComments)
                }
                val refRat = FirebaseDatabase.getInstance().getReference("Places")
                refRat.child(placeId).child("placeRating").setValue(placeRating)
                val refCom = FirebaseDatabase.getInstance().getReference("Places")
                refCom.child(placeId).child("placeComments").setValue(placeComments)
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun loadPlaceDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val cityId = "${snapshot.child("cityId").value}"
                    val placename = "${snapshot.child("placename").value}"
                    val placedescription = "${snapshot.child("placedescription").value}"
                    val placeage = "${snapshot.child("placeAge").value}"
                    val placetime = "${snapshot.child("placeTime").value}"
                    val placeaddress = "${snapshot.child("placeAddress").value}"
                    //val placepublic = "${snapshot.child("placePublic").value}"
                    val placeweb = "${snapshot.child("placeWeb").value}"
                    val placetelephone = "${snapshot.child("placeTelephone").value}"
                    val placerating = "${snapshot.child("placeRating").value}"
                    val placeImage = "${snapshot.child("placeImage").value}"

                    loadCategory(categoryId, binding.textPlaceCategoryDB)
                    loadCity(cityId, binding.textPlaceCityDB)
                    //loadRating(placeId, binding.textPlaceTotalRatingDB)

                    binding.textPlaceName.text = placename
                    binding.textPlaceDescription.text = placedescription
//                    if (placepublic != "") {
//                        binding.textPlacePublicDB.text = placepublic
//                    } else {
//                        binding.textPlacePublicDB.text = "не указано"
//                    }
                    if (placeweb != "") {
                        binding.textPlaceWebBD.text = placeweb
                    } else {
                        binding.textPlaceWebBD.text = getText(R.string.text_noData)
                    }
                    if (placeaddress != "") {
                        binding.textPlaceAddressBD.text = placeaddress
                        val mTextView = findViewById<TextView>(R.id.textPlaceAddressBD)
                        val mSpannableString = SpannableString(placeaddress)
                        mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
                        mTextView.text = mSpannableString
                        //Кнопка-текст "Адрес"
                        binding.textPlaceAddressBD.setOnClickListener {
                            Toast.makeText(this@PlaceDetailsActivity, R.string.text_openingMap, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@PlaceDetailsActivity, MapActivity::class.java)
                            intent.putExtra("placeaddress", placeaddress)
                            this@PlaceDetailsActivity.startActivity(intent)
                        }
                    } else {
                        binding.textPlaceAddressBD.text = getText(R.string.text_noData)
                    }
                    if (placetime != "") {
                        binding.textPlaceTimeBD.text = placetime
                    } else {
                        binding.textPlaceTimeBD.text = getText(R.string.text_noData)
                    }
                    if (placetelephone != "") {
                        binding.textPlaceTelephoneBD.text = placetelephone
                    } else {
                        binding.textPlaceTelephoneBD.text = getText(R.string.text_noData)
                    }

                    if (placerating != "") {
                        binding.textPlaceTotalRatingDB.text = placerating
                    } else {
                        binding.textPlaceTotalRatingDB.text = getText(R.string.text_ratind_0)
                    }

                    binding.textPlaceAgeDB.text = placeage

                    try {
                        Glide.with(this@PlaceDetailsActivity)
                            .load(placeImage)
                            .placeholder(R.drawable.ic_image)
                            .into(binding.imageViewPlacePhoto)
                    }
                    catch (e: Exception) {
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun showComments() {
        commentArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(Comments::class.java)
                        commentArrayList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@PlaceDetailsActivity, commentArrayList)
                    binding.commentsRv.adapter = adapterComment
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadCategory(categoryId: String, categoryTv: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryname = "${snapshot.child("categoryname").value}"
                    categoryTv.text = categoryname
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadCity(cityId: String, cityTv: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(cityId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cityname = "${snapshot.child("cityname").value}"
                    cityTv.text = cityname
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun checkDeleteNegativePlace() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyNegativePlaces = snapshot.exists()
                    if (isInMyNegativePlaces) {
                        val refNeg = FirebaseDatabase.getInstance().getReference("Users")
                        refNeg.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
                            .removeValue()
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                            }
                        addFavorite()
                    } else {
                        addFavorite()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addFavorite() {
        var categoryId = ""
        val refPlace = FirebaseDatabase.getInstance().getReference("Places")
        refPlace.child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryId = "${snapshot.child("categoryId").value}"
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = placeId
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(placeId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.text_place_addFav, Toast.LENGTH_SHORT).show()
                addSelection(categoryId)
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_place_addFav, Toast.LENGTH_SHORT).show()
            }
    }

    private fun addSelection(categoryId: String) {
        placesRecArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesRecArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(RecommendPlaces::class.java)
                        if (model != null) {
                            placesRecArrayList.add(model)
                        }
                        val refSel = FirebaseDatabase.getInstance().getReference("Users")
                        refSel.child(firebaseAuth.uid!!).child("Selection").child(model!!.id)
                            .setValue(model)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        Toast.makeText(this, R.string.text_selectionUpdate, Toast.LENGTH_SHORT).show()
        removeFavSelection()
        removeRatSelection(categoryId)
        removeNegSelection()
    }

    private fun removeRatSelection(categoryId: String) {
        commentArrayList = ArrayList()
        placesRecArrayList = ArrayList()
        val refPlace = FirebaseDatabase.getInstance().getReference("Places")
        refPlace.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesRecArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelPlace = ds.getValue(RecommendPlaces::class.java)
                        if (modelPlace != null) {
                            placesRecArrayList.add(modelPlace)
                        }
                        val refPlaceCom = FirebaseDatabase.getInstance().getReference("Places")
                        refPlaceCom.child(modelPlace!!.id).child("Comments").orderByChild("uid").equalTo(firebaseAuth.uid!!)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    commentArrayList.clear()
                                    for (ds in snapshot.children) {
                                        val modelRat = ds.getValue(Comments::class.java)
                                        if (modelRat != null) {
                                            commentArrayList.add(modelRat)
                                        }
                                        if ((modelRat!!.rating <= 2.5.toString()) || (modelRat!!.rating >= 3.0.toString())) {
                                            val refSel = FirebaseDatabase.getInstance().getReference("Users")
                                            refSel.child(firebaseAuth.uid!!).child("Selection").child(modelPlace!!.id)
                                                .removeValue()
                                                .addOnSuccessListener {
                                                }
                                                .addOnFailureListener {
                                                }
                                        } else {
                                            Log.e("TAG","No Negative Place NegRat")
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun removeNegSelection() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelPl = ds.getValue(Places::class.java)
                        if (modelPl != null) {
                            placesArrayList.add(modelPl)
                        }
                        val refNeg = FirebaseDatabase.getInstance().getReference("Users")
                        refNeg.child(firebaseAuth.uid!!).child("Negatives").orderByChild("id").equalTo(modelPl!!.id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    placesArrayList.clear()
                                    for (ds in snapshot.children) {
                                        val model = ds.getValue(Places::class.java)
                                        if (model != null) {
                                            placesArrayList.add(model)
                                        }
                                        val refSel = FirebaseDatabase.getInstance().getReference("Users")
                                        refSel.child(firebaseAuth.uid!!).child("Selection").child(model!!.id)
                                            .removeValue()
                                            .addOnSuccessListener {
                                            }
                                            .addOnFailureListener {
                                            }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun removeFavSelection() {
        var id = ""
        val refPlace = FirebaseDatabase.getInstance().getReference("Places")
        refPlace.child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    id = "${snapshot.child("id").value}"
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        placesRecArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").orderByChild("id").equalTo(id)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                placesRecArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(RecommendPlaces::class.java)
                    if (model != null) {
                        placesRecArrayList.add(model)
                    }
                    val refSel = FirebaseDatabase.getInstance().getReference("Users")
                    refSel.child(firebaseAuth.uid!!).child("Selection").child(id)
                        .removeValue()
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener {
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun removeFavorite(context: Context, placeId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(placeId)
            .removeValue().addOnSuccessListener {
                Toast.makeText(this, R.string.text_place_remFav, Toast.LENGTH_SHORT).show()
                checkAlreadyNegativePlace()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_place_remFav, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAlreadyNegativePlace() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyNegativePlaces = snapshot.exists()
                    if (isInMyNegativePlaces) {
                        Log.e("TAG","Already Negative Place")
                    } else {
                        addNegativePlace()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addNegativePlace() {
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = placeId
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
            .setValue(hashMap)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }

    private fun checkIsFavorite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(placeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {
                        binding.butnFavorite.setImageResource(
                            R.drawable.ic_favorite_fullwhite_bl)
                            //binding.buttonFavorite.text = "Убрать из избранного"
                            //binding.buttonFavorite.text = getText(R.string.button_place_remFav)
                    } else {
                        binding.butnFavorite.setImageResource(
                            R.drawable.ic_favorite_white_bl)
                            //binding.buttonFavorite.text = "Добавить в избранное"
                            //binding.buttonFavorite.text = getText(R.string.button_place_addFav)
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
                    binding.btnAddComment.setEnabled(true)
                } catch (e: IOException) {
                    Log.e("TAG", "Failed to initialize the translator.", e)
                    Toast.makeText(this,R.string.error_translatorInitial, Toast.LENGTH_SHORT).show()
                    binding.btnTranslate.setEnabled(false)
                    binding.btnAddComment.setEnabled(false)
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