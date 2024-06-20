package com.university.geekway

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EnterActivity : BaseActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnEnter: Button
    private lateinit var textHaveNotAccount: TextView
    private lateinit var textFP: TextView
    private lateinit var textReadme: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        btnEnter = findViewById(R.id.buttonEnter)
        etEmail = findViewById(R.id.editTextEmailEnter)
        etPass = findViewById(R.id.editTextPasswordEnter)
        textHaveNotAccount = findViewById(R.id.textHaveNotAccount)
        textReadme = findViewById(R.id.textReadme)
        textFP = findViewById(R.id.textForgotPassword)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()

        btnEnter.setOnClickListener {
            login()
        }

        textReadme.setOnClickListener {
            val readmeActivity = Intent(this, ReadmeActivity::class.java)
            startActivity(readmeActivity)
        }

        textFP.setOnClickListener {
            val forgotPasswordActivity = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(forgotPasswordActivity)
        }

        val buttonRegistration: Button = findViewById (R.id.buttonRegistration)
        buttonRegistration.setOnClickListener {
            val registrationActivity = Intent(this, RegistrationActivity::class.java)
            startActivity(registrationActivity)
        }
    }

    private fun login() {
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        Log.d("TAG", "onClick" + etEmail.text.toString())

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, this.resources.getString(R.string.error_enter_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, this.resources.getString(R.string.error_right_email), Toast.LENGTH_SHORT).show()
            return
        }

        showProgressDialog(resources.getString(R.string.text_progress))

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {

            checkUserExists()

            if (it.isSuccessful) {
                checkUserAccessLevel(auth.uid)
            } else {
                Toast.makeText(this, this.resources.getString(R.string.error_right_auth), Toast.LENGTH_SHORT).show()
            }

            hideProgressDialog()
        }
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

    private fun checkUserExists() {
        val user = auth.currentUser
            if (user === null) {
                Toast.makeText(this, this.resources.getString(R.string.error_auth), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, this.resources.getString(R.string.text_wellcome), Toast.LENGTH_SHORT).show()
            }
    }
}