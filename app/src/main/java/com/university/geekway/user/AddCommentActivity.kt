package com.university.geekway.user

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityAddCommentBinding
import com.university.geekway.models.Comments
import com.university.geekway.models.Places
import com.university.geekway.models.RecommendPlaces
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

class AddCommentActivity : BaseActivity() {

    private lateinit var textAddComment: TextView
    private lateinit var btnAddComment: Button
    private lateinit var btnCancelAddComment: Button
    private lateinit var binding: ActivityAddCommentBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var placesRecArrayList: ArrayList<RecommendPlaces>
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var commentArrayList: ArrayList<Comments>
    private var isInMyNegativePlaces = false
    private var placeId = ""
    private var comment = ""
    private var rating = ""
    private var commentENG = ""
    private var pointNegative = ""
    private var pointPositive = ""
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        placeId = intent.getStringExtra("id")!!

        firebaseAuth = FirebaseAuth.getInstance()

        textAddComment = findViewById(R.id.textAddComment)
        btnAddComment = findViewById(R.id.buttonAddComment)
        btnCancelAddComment = findViewById(R.id.buttonCancelAddComment)

        auth = FirebaseAuth.getInstance()

        btnCancelAddComment.setOnClickListener {
            finish()
        }

        btnAddComment.setOnClickListener {
            comment = binding.editComment.text.toString().trim()
            rating = binding.rBar.rating.toString()
            if (comment.isEmpty()) {
                Toast.makeText(this, R.string.error_addCom_emptyCom, Toast.LENGTH_SHORT).show()
            } else if (rating.toDouble() == 0.0) {
                Toast.makeText(this, R.string.error_addCom_emptyRat, Toast.LENGTH_SHORT).show()
            } else {
                showProgressDialog(resources.getString(R.string.text_progress))

                Toast.makeText(this, R.string.text_waitPlease, Toast.LENGTH_SHORT).show()

                commentENG(binding.editComment.getText().toString())

                hideProgressDialog()
            }
        }

        downloadModelSentiment()
    }

    private fun commentENG(text:String) {
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
        rusEngTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                Log.e("TAG", comment)
                Log.e("TAG", "Translated")
                Log.e("TAG", translatedText)
                commentENG = translatedText
                classifyComment()
            }
            .addOnFailureListener {
                Log.e("TAG", "Error translate")
            }
    }

    private var textClassifier: NLClassifier? = null
    private fun classifyComment() {
        val results: List<Category> = textClassifier!!.classify(commentENG)
        var textToShow = "Input: $commentENG\nOutput:\n"
        for (i in results.indices) {
            val result = results[i]
            textToShow += String.format("    %s: %s\n", result.label, result.score)
        }
        textToShow += "---------\n"
        Log.e("TAG", textToShow)
        Log.e("TAG", results[0].score.toString())
        Log.e("TAG", results[1].score.toString())

        pointNegative = results[0].score.toString()
        pointPositive = results[1].score.toString()

        if (rating < 3.0.toString() && pointPositive > pointNegative) {
            Toast.makeText(this, R.string.text_commentPositive, Toast.LENGTH_SHORT).show()
            Toast.makeText(this, R.string.text_rightRating, Toast.LENGTH_SHORT).show()
            return
        }
        if (rating > 2.5.toString() && pointNegative > pointPositive) {
            Toast.makeText(this, R.string.text_commentNegative, Toast.LENGTH_SHORT).show()
            Toast.makeText(this, R.string.text_rightRating, Toast.LENGTH_SHORT).show()
            return
        }

        addComment()
    }

    @SuppressLint("SimpleDateFormat")
    private fun addComment() {
        var categoryId = ""
        val refPlace = FirebaseDatabase.getInstance().getReference("Places")
        refPlace.child(placeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryId = "${snapshot.child("categoryId").value}"
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val date = sdf.format(Date())
        val timestamp = "${System.currentTimeMillis()}"
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["date"] = "$date"
        hashMap["placeId"] = "$placeId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["rating"] = "$rating"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.text_addCom_complite, Toast.LENGTH_SHORT).show()
                submitRating()

                addSelection(categoryId)

                if (rating >= 3.0.toString()) {
                    checkDeleteNegativePlace()
                }

                if (rating <= 2.5.toString()) {
                    checkAlreadyNegativePlace()
                }

                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.text_addCom_notComplite, Toast.LENGTH_SHORT).show()
            }
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
                    } else {
                        Log.e("TAG","Not Negative Place")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
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
        removeComSelection()
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

    private fun removeComSelection() {
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
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId).child("Comments").orderByChild("uid").equalTo(firebaseAuth.uid!!)
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

    private fun downloadModelSentiment() {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelDownloader.getInstance()
            .getModel("sentiment_analysis", DownloadType.LOCAL_MODEL, conditions)
            .addOnSuccessListener { model: CustomModel? ->
                try {
                    textClassifier = NLClassifier.createFromFile(model!!.file)
                    btnAddComment.setEnabled(true)
                } catch (e: IOException) {
                    Log.e("TAG", "Failed to initialize the model. ", e)
                    Toast.makeText(this,R.string.error_modelInitial, Toast.LENGTH_SHORT).show()
                    btnAddComment.setEnabled(false)
                    onBackPressed()
                }
            }
            .addOnFailureListener { e: java.lang.Exception? ->
                Log.e("TAG","Failed to download the model. ", e)
                Toast.makeText(this,R.string.error_modelDownload, Toast.LENGTH_SHORT).show()
                Toast.makeText(this,R.string.text_reqWIFI, Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
    }
}