package com.university.geekway

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : BaseActivity() {

    private lateinit var textSettings: TextView
    private lateinit var etUserName: EditText
    private lateinit var etUserEmail: EditText
    private lateinit var etPassConfSettings: EditText
    private lateinit var textResetPass: TextView
    private lateinit var btnSaveSettings: Button
    private lateinit var btnCancelSettings: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    var refUsers: DatabaseReference? = null
    var fbUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        textSettings = findViewById(R.id.textSettings)
        etUserName = findViewById(R.id.editSettingsUserName)
        etUserEmail = findViewById(R.id.editSettingsEmail)
        etPassConfSettings = findViewById(R.id.editSettingsPassConf)
        textResetPass = findViewById(R.id.textResetPasswordSettings)
        btnSaveSettings = findViewById(R.id.buttonSaveSettings)
        btnCancelSettings = findViewById(R.id.buttonCancelSettings)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        fbUser = auth.currentUser
        refUsers = db.reference.child("Users").child(fbUser!!.uid)

        btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        textResetPass.setOnClickListener {
            val resetPasswordActivity = Intent(this, ResetPasswordActivity::class.java)
            startActivity(resetPasswordActivity)
            finish()
        }

        btnCancelSettings.setOnClickListener {
            onBackPressed()
        }

        showUserDataET()
    }

    private fun showUserDataET() {
        val user = auth.currentUser
        val ref = db.getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("Name").value}"
                    etUserName.setText(name)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        if (user != null) {
            etUserEmail.setText(user.email)
        }
    }

    private fun saveSettings() {
        val passConf = etPassConfSettings.text.toString()
        val userName = etUserName.text.toString()
        val userEmail = etUserEmail.text.toString()

        if (userName.length > 20) {
            Toast.makeText(this, this.resources.getString(R.string.error_set_name_long), Toast.LENGTH_SHORT).show()
            return
        }
        if (userName.length < 2) {
            Toast.makeText(this, this.resources.getString(R.string.error_set_name_short), Toast.LENGTH_SHORT).show()
            return
        }
        if (!userName.matches("[A-Z,a-z,А-Я,а-я]*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_set_name_contain), Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, this.resources.getString(R.string.error_set_email_right), Toast.LENGTH_SHORT).show()
            return
        }

        if (passConf.isBlank()) {
            Toast.makeText(this, this.resources.getString(R.string.error_set_pass_empty), Toast.LENGTH_SHORT).show()
            return
        }

        saveUserData()
    }

    private fun saveUserData() {
        val passConf = etPassConfSettings.text.toString()
        val userName = etUserName.text.toString()
        val userEmail = etUserEmail.text.toString()
        val user = auth.currentUser
        if (user != null && user.email != null) {

            showProgressDialog(resources.getString(R.string.text_progress))

            val userCredential = EmailAuthProvider
                .getCredential(user.email!!, passConf)
            user?.reauthenticate(userCredential)
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        user?.updateEmail(userEmail)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val hashMap: HashMap<String, Any> = HashMap()
                                    hashMap["Name"] = "$userName"
                                    val refNa = db.getReference("Users")
                                    refNa.child(auth.uid!!)
                                        .updateChildren(hashMap)
                                        .addOnCompleteListener {
                                        }
                                    refUsers?.child("Email")?.setValue(userEmail)
                                    Toast.makeText(this, this.resources.getString(R.string.text_set_save), Toast.LENGTH_SHORT).show()
                                    checkUserAccessLevel(auth.uid)
                                }
                            }
                    } else {
                        Toast.makeText(this, this.resources.getString(R.string.error_set_pass_not_right), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        hideProgressDialog()
    }

    private fun checkUserAccessLevel(uid: String?) {
        auth.currentUser?.let {
            fs.collection("Users") .document(it.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                        if (document.getString("role") != "user") {
                            val adminActivity = Intent(this, AdminActivity::class.java)
                            startActivity(adminActivity)
                            finish()
                        }
                        if (document.getString("role") != "admin") {
                            val userActivity = Intent(this, UserActivity::class.java)
                            startActivity(userActivity)
                            finish()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "Error getting documents.", exception)
                }
        }
    }
}