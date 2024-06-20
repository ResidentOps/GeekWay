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
import com.university.geekway.models.Users
import com.university.geekway.user.FavoritesActivity
import com.university.geekway.user.GuideActivity
import com.university.geekway.user.MapActivity
import com.university.geekway.user.PreferencesActivity
import com.university.geekway.user.SelectionActivity

class UserActivity : BaseActivity() {

    private lateinit var userName: TextView
    private lateinit var userShowName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userShowEmail: TextView
    private lateinit var btnUserDB: Button
    private lateinit var btnUserSelection: Button
    private lateinit var btnUserMap: Button
    private lateinit var btnUserFavorites: Button
    private lateinit var btnUserPreferences: Button
    private lateinit var btnUserSettings: Button
    private lateinit var btnExitUser: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    var refUsers: DatabaseReference? = null
    var fbUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        //Проверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        userName = findViewById(R.id.textNameUser)
        userShowName = findViewById(R.id.user_name)
        userEmail = findViewById(R.id.textEmailUser)
        userShowEmail = findViewById(R.id.user_email)
        btnUserDB = findViewById(R.id.buttonUserDB)
        btnUserSelection = findViewById(R.id.buttonUserSelection)
        btnUserMap = findViewById(R.id.buttonUserMap)
        btnUserFavorites = findViewById(R.id.buttonUserFavorites)
        btnUserPreferences = findViewById(R.id.buttonUserPreferences)
        btnUserSettings = findViewById(R.id.buttonUserSettings)
        btnExitUser = findViewById(R.id.buttonUserExit)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        fbUser = auth.currentUser
        refUsers = db.reference.child("Users").child(fbUser!!.uid)

        btnUserDB.setOnClickListener {
            val guideActivity = Intent(this, GuideActivity::class.java)
            startActivity(guideActivity)
        }

        btnUserSelection.setOnClickListener {
            val selectionActivity = Intent(this, SelectionActivity::class.java)
            startActivity(selectionActivity)
        }

        btnUserMap.setOnClickListener {
            val mapActivity = Intent(this, MapActivity::class.java)
            startActivity(mapActivity)
        }

        btnUserFavorites.setOnClickListener {
            val favoritesActivity = Intent(this, FavoritesActivity::class.java)
            startActivity(favoritesActivity)
        }

        btnUserPreferences.setOnClickListener {
            val preferencesActivity = Intent(this, PreferencesActivity::class.java)
            startActivity(preferencesActivity)
        }

        btnUserSettings.setOnClickListener {
            val epActivity = Intent(this, SettingsActivity::class.java)
            startActivity(epActivity)
        }

        btnExitUser.setOnClickListener {
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
                    userShowName.text = user!!.getName()
                    userShowEmail.text = user!!.getEmail()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}