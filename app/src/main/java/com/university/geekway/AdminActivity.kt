package com.university.geekway

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.university.geekway.admin.AddCategoryActivity
import com.university.geekway.admin.AddCityActivity
import com.university.geekway.admin.AddPlaceActivity
import com.university.geekway.admin.DataBaseActivity
import com.university.geekway.models.Users

class AdminActivity : BaseActivity() {

    private lateinit var adminName: TextView
    private lateinit var adminShowName: TextView
    private lateinit var adminEmail: TextView
    private lateinit var adminShowEmail: TextView
    private lateinit var btnAdminDB: Button
    private lateinit var btnAdminAddCity: Button
    private lateinit var btnAdminAddCategory: Button
    private lateinit var btnAdminAddPlace: Button
    private lateinit var btnAdminSettings: Button
    private lateinit var btnExitAdmin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    var refUsers: DatabaseReference? = null
    var fbUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        //Проверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        adminName = findViewById(R.id.textNameAdmin)
        adminShowName = findViewById(R.id.admin_name)
        adminEmail = findViewById(R.id.textEmailAdmin)
        adminShowEmail = findViewById(R.id.admin_email)
        btnAdminDB = findViewById(R.id.buttonAdminDB)
        btnAdminAddCity = findViewById(R.id.buttonAdminAddCity)
        btnAdminAddCategory = findViewById(R.id.buttonAdminAddCategory)
        btnAdminAddPlace = findViewById(R.id.buttonAdminAddPlace)
        btnAdminSettings = findViewById(R.id.buttonAdminSettings)
        btnExitAdmin = findViewById(R.id.buttonAdminExit)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        fbUser = auth.currentUser
        refUsers = db.reference.child("Users").child(fbUser!!.uid)

        btnAdminDB.setOnClickListener {
            val dbActivity = Intent(this, DataBaseActivity::class.java)
            startActivity(dbActivity)
        }

        btnAdminAddCity.setOnClickListener {
            val addCityActivity = Intent(this, AddCityActivity::class.java)
            startActivity(addCityActivity)
        }

        btnAdminAddCategory.setOnClickListener {
            val addCategoryActivity = Intent(this, AddCategoryActivity::class.java)
            startActivity(addCategoryActivity)
        }

        btnAdminAddPlace.setOnClickListener {
            val addPlaceActivity = Intent(this, AddPlaceActivity::class.java)
            startActivity(addPlaceActivity)
        }

        btnAdminSettings.setOnClickListener {
            val epActivity = Intent(this, SettingsActivity::class.java)
            startActivity(epActivity)
        }

        btnExitAdmin.setOnClickListener {
            auth.signOut()
            val enterActivity = Intent(this, EnterActivity::class.java)
            startActivity(enterActivity)
            finish()
        }

        showData()
    }

    private fun showData() {
        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: Users? = p0.getValue(Users::class.java)
                    adminShowName.text = user!!.getName()
                    adminShowEmail.text = user!!.getEmail()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}