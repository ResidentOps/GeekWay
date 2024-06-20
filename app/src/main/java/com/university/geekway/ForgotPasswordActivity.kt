package com.university.geekway

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var textForgotPassword: TextView
    private lateinit var textEnterEmailFP1: TextView
    private lateinit var textEnterEmailFP2: TextView
    private lateinit var etEmailFP: EditText
    private lateinit var btnSubmitFP: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    private lateinit var rf: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        textForgotPassword = findViewById(R.id.textForgotPassword)
        textEnterEmailFP1 = findViewById(R.id.textEnterEmailForgotPassword1)
        textEnterEmailFP2 = findViewById(R.id.textEnterEmailForgotPassword2)
        etEmailFP = findViewById(R.id.editEmailForgotPassword)
        btnSubmitFP = findViewById(R.id.buttonSubmitForgotPassword)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        rf = db.getReference()

        btnSubmitFP.setOnClickListener {
            submitFP()
        }

        val buttonCancelFP: Button = findViewById(R.id.buttonCancelForgotPassword)
        buttonCancelFP.setOnClickListener {
            onBackPressed()
        }
    }

    private fun submitFP() {
        val email = etEmailFP.text.toString()
        if (email.isBlank()) {
            Toast.makeText(this, this.resources.getString(R.string.error_email_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, this.resources.getString(R.string.error_email_right), Toast.LENGTH_SHORT).show()
            return
        }

        showProgressDialog(resources.getString(R.string.text_progress))

        auth.sendPasswordResetEmail(email).addOnCompleteListener(this) {

            checkUserExistsFP()

            if (it.isSuccessful) {
                auth.signOut()
                Toast.makeText(this, this.resources.getString(R.string.text_check_email), Toast.LENGTH_SHORT).show()
                val enterActivity = Intent(this, EnterActivity::class.java)
                startActivity(enterActivity)
                finish()
            } else {
                Toast.makeText(this, this.resources.getString(R.string.text_email_not_exist), Toast.LENGTH_SHORT).show()
            }

            hideProgressDialog()
        }
    }

    private fun checkUserExistsFP() {
        val user = auth.currentUser
        if (user === null) {
            Toast.makeText(this, this.resources.getString(R.string.error_email_fail), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, this.resources.getString(R.string.text_email_complite), Toast.LENGTH_SHORT).show()
        }
    }
}