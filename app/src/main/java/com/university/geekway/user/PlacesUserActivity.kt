package com.university.geekway.user

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.adapters.AdapterPlaceUser
import com.university.geekway.databinding.ActivityPlacesUserBinding
import com.university.geekway.models.Categories
import com.university.geekway.models.Comments
import com.university.geekway.models.Places
import com.university.geekway.models.RecommendPlaces

class PlacesUserActivity : BaseActivity() {
    private lateinit var binding: ActivityPlacesUserBinding
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var placesRecArrayList: ArrayList<RecommendPlaces>
    private lateinit var commentsArrayList: ArrayList<Comments>
    private lateinit var categoriesArrayList: ArrayList<Categories>
    private lateinit var adapterPlace: AdapterPlaceUser
    private lateinit var firebaseAuth: FirebaseAuth
    private var isInMyPreference = false
    private var categoryId = ""
    private var categoryname = ""

    private companion object {
        const val TAG = "PLACES_LIST_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Проверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        categoryId = intent.getStringExtra("categoryId")!!
        categoryname = intent.getStringExtra("categoryname")!!

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            checkIsPreference()
        }

        binding.buttonCancelPlaces.setOnClickListener {
            onBackPressed()
        }

        binding.editPlaceName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPlace.filter!!.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.buttonPreference.setOnClickListener() {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, R.string.text_placeDetails_authNot, Toast.LENGTH_SHORT).show()
            } else {
                if (isInMyPreference) {
                    removePreference(this, categoryId)
                } else {
                    addPreference()
                }
            }
        }

        loadPlaces()
    }

    private fun loadPlaces() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref?.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(Places::class.java)
                        if (model != null) {
                            placesArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.placename} ${model.categoryname} ${model.placeImage}")
                        }
                    }
                    adapterPlace = AdapterPlaceUser(this@PlacesUserActivity, placesArrayList)
                    binding.placesRv.adapter = adapterPlace
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addPreference() {
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = categoryId
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Preferences").child(categoryId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.text_categoryAdd_preference, Toast.LENGTH_SHORT).show()
                addSelection()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_categoryAdd_preference, Toast.LENGTH_SHORT).show()
            }
    }

    private fun addSelection() {
        placesRecArrayList = ArrayList()
        categoriesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.orderByChild("categoryname").equalTo(categoryname)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoriesArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelCat = ds.getValue(Categories::class.java)
                        if (modelCat != null) {
                            categoriesArrayList.add(modelCat)
                        }
                        val refPlace = FirebaseDatabase.getInstance().getReference("Places")
                        refPlace.orderByChild("categoryId").equalTo(modelCat!!.id)
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
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        Toast.makeText(this, R.string.text_selectionUpdate, Toast.LENGTH_SHORT).show()
        removeRatSelection()
        removeFavSelection()
        removeNegSelection()
    }

    private fun removeRatSelection() {
        commentsArrayList = ArrayList()
        placesRecArrayList = ArrayList()
        categoriesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.orderByChild("categoryname").equalTo(categoryname)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoriesArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelCat = ds.getValue(Categories::class.java)
                        if (modelCat != null) {
                            categoriesArrayList.add(modelCat)
                        }
                        val refPlace = FirebaseDatabase.getInstance().getReference("Places")
                        refPlace.orderByChild("categoryId").equalTo(modelCat!!.id)
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
                                                    commentsArrayList.clear()
                                                    for (ds in snapshot.children) {
                                                        val modelRat = ds.getValue(Comments::class.java)
                                                        if (modelRat != null) {
                                                            commentsArrayList.add(modelRat)
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
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun removeFavSelection() {
        placesRecArrayList = ArrayList()
        categoriesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.orderByChild("categoryname").equalTo(categoryname)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoriesArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelCat = ds.getValue(Categories::class.java)
                        if (modelCat != null) {
                            categoriesArrayList.add(modelCat)
                        }
                        val refPlace = FirebaseDatabase.getInstance().getReference("Places")
                        refPlace.orderByChild("categoryId").equalTo(modelCat!!.id)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    placesRecArrayList.clear()
                                    for (ds in snapshot.children) {
                                        val modelPlace = ds.getValue(RecommendPlaces::class.java)
                                        if (modelPlace != null) {
                                            placesRecArrayList.add(modelPlace)
                                        }
                                        val refFa = FirebaseDatabase.getInstance().getReference("Users")
                                        refFa.child(firebaseAuth.uid!!).child("Favorites").orderByChild("id").equalTo(modelPlace!!.id)
                                            .addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    placesRecArrayList.clear()
                                                    for (ds in snapshot.children) {
                                                        val modelFa = ds.getValue(RecommendPlaces::class.java)
                                                        if (modelFa != null) {
                                                            placesRecArrayList.add(modelFa)
                                                        }
                                                        if (modelPlace!!.id == modelFa!!.id) {
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
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun removeNegSelection() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.addListenerForSingleValueEvent (object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelPl = ds.getValue(Places::class.java)
                        if (modelPl != null) {
                            placesArrayList.add(modelPl)
                        }
                        val refNeg = FirebaseDatabase.getInstance().getReference("Users")
                        refNeg.child(firebaseAuth.uid!!).child("Negatives").orderByChild("id").equalTo(modelPl!!.id)
                            .addListenerForSingleValueEvent (object : ValueEventListener {
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

    private fun removePreference(context: Context, categoryId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Preferences").child(categoryId)
            .removeValue().addOnSuccessListener {
                Toast.makeText(this, R.string.text_categoryDel_preference, Toast.LENGTH_SHORT).show()
                removeSelection()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_categoryDel_preference, Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeSelection() {
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
        Toast.makeText(this, R.string.text_selectionUpdate, Toast.LENGTH_SHORT).show()
    }

    private fun checkIsPreference() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Preferences").child(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyPreference = snapshot.exists()
                    if (isInMyPreference) {
                        binding.buttonPreference.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_preferred_fullblack,
                            0, 0, 0)
                        binding.buttonPreference.text = getText(R.string.button_place_remPref)
                    } else {
                        binding.buttonPreference.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_preferred_black,
                            0, 0, 0)
                        binding.buttonPreference.text = getText(R.string.button_place_addPref)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}