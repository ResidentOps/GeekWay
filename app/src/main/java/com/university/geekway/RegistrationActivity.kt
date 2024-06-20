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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : BaseActivity() {

    private lateinit var etUserName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var etConfPass: EditText
    private lateinit var btnRegAcc: Button
    private lateinit var textHaveAccount: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    private lateinit var rf: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        etUserName = findViewById(R.id.editTextUserName)
        etEmail = findViewById(R.id.editTextEmail)
        etPass = findViewById(R.id.editTextPassword)
        etConfPass = findViewById(R.id.editTextPasswordConfirm)
        btnRegAcc = findViewById(R.id.buttonRegistrationAccount)
        textHaveAccount = findViewById(R.id.textHaveAccount)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db= FirebaseDatabase.getInstance()
        rf = db.getReference()

        btnRegAcc.setOnClickListener {
            signUpUser()
        }

        val buttonEnterAccount: Button = findViewById (R.id.buttonEnterAccount)
        buttonEnterAccount.setOnClickListener {
            onBackPressed()
        }
    }

    private fun signUpUser() {
        val username = etUserName.text.toString()
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        val confirmPassword = etConfPass.text.toString()

        if (username.isBlank() ||email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (username.length > 20) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_name_long), Toast.LENGTH_SHORT).show()
            return
        }
        if (username.length < 2) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_name_short), Toast.LENGTH_SHORT).show()
            return
        }
        if (!username.matches("[A-Z,a-z,А-Я,а-я]*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_name_contain), Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_email_right), Toast.LENGTH_SHORT).show()
            return
        }

        if (pass.length < 8) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_pass_8), Toast.LENGTH_SHORT).show()
            return
        }
        if (!pass.matches(".*[A-Z].*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_pass_1A), Toast.LENGTH_SHORT).show()
            return
        }
        if (!pass.matches(".*[a-z].*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_pass_1a), Toast.LENGTH_SHORT).show()
            return
        }
        if (!pass.matches(".*[0-9].*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_pass_1n), Toast.LENGTH_SHORT).show()
            return
        }
        if (!pass.matches(".*[!@#\$%^&_+=].*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_pass_1spec), Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPassword) {
            Toast.makeText(this, this.resources.getString(R.string.error_reg_pass_compare), Toast.LENGTH_SHORT).show()
            return
        }

        showProgressDialog(resources.getString(R.string.text_progress))

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {

            checkEmailExists()

            if (it.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(this, this.resources.getString(R.string.text_reg_complite), Toast.LENGTH_SHORT).show()
                val userInfo = hashMapOf(
                    "role" to "user"
                )

                if (user != null) {
                    fs.collection("Users").document(user.uid)
                        .set(userInfo)
                        .addOnSuccessListener {
                            Log.d("SuccessListener", "DocumentSnapshot added. ")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FailureListener", "Error adding document.", e)
                        }
                }

                val userData = hashMapOf(
                    "Name" to username,
                    "Email" to email
                )

                if (user != null) {
                    rf.child("Users")
                        .child(user.uid)
                        .setValue(userData)
                        .addOnSuccessListener {
                            Log.d("SuccessListener", "DocumentData added: ")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FailureListener", "Error adding document data.", e)
                        }
                }

                val userActivity = Intent(this, UserActivity::class.java)
                startActivity(userActivity)
                finish()
            } else {
                Toast.makeText(this, this.resources.getString(R.string.text_reg_not_complite), Toast.LENGTH_SHORT).show()
            }

            hideProgressDialog()
        }
    }

    private fun checkEmailExists() {
        val email = etEmail.text.toString()
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener() {
                val checkEmail = auth.currentUser?.email?.isEmpty()
                if (checkEmail === false) {
                    Toast.makeText(this, this.resources.getString(R.string.text_reg_wellcome), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, this.resources.getString(R.string.error_email_exist), Toast.LENGTH_SHORT).show()
                }
            }
    }
}