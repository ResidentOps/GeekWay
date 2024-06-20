package com.university.geekway

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ResetPasswordActivity : BaseActivity() {

    private lateinit var textResetPass: TextView
    private lateinit var etPassOld: EditText
    private lateinit var etPassNew: EditText
    private lateinit var etPassConfNew: EditText
    private lateinit var textFP: TextView
    private lateinit var btnSaveResetPass: Button
    private lateinit var btnCancelResetPass: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    var refUsers: DatabaseReference? = null
    var fbUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        textResetPass = findViewById(R.id.textResetPassword)
        etPassOld = findViewById(R.id.editResetPassOld)
        etPassNew = findViewById(R.id.editResetPassNew)
        etPassConfNew = findViewById(R.id.editResetPassNewConf)
        textFP = findViewById(R.id.textResetPassForgotPassword)
        btnSaveResetPass = findViewById(R.id.buttonSaveResetPassword)
        btnCancelResetPass = findViewById(R.id.buttonCancelResetPassword)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        fbUser = auth.currentUser
        refUsers = db.reference.child("Users").child(fbUser!!.uid)

        textFP.setOnClickListener {
            val forgotPasswordActivity = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(forgotPasswordActivity)
            finish()
        }

        btnSaveResetPass.setOnClickListener {
            resetPassword()
        }

        btnCancelResetPass.setOnClickListener {
            onBackPressed()
        }
    }

    private fun resetPassword() {
        val oldPass = etPassOld.text.toString()
        val newPass = etPassNew.text.toString()
        val confNewPass = etPassConfNew.text.toString()

        if (oldPass.isBlank() || newPass.isBlank() || confNewPass.isBlank()) {
            Toast.makeText(this, this.resources.getString(R.string.error_respass_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass.length < 8) {
            Toast.makeText(this, this.resources.getString(R.string.error_respass_pass_8), Toast.LENGTH_SHORT).show()
            return
        }
        if (!newPass.matches(".*[A-Z].*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_respass_pass_1A), Toast.LENGTH_SHORT).show()
            return
        }
        if (!newPass.matches(".*[a-z].*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_respass_pass_1a), Toast.LENGTH_SHORT).show()
            return
        }
        if (!newPass.matches(".*[0-9].*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_respass_pass_1n), Toast.LENGTH_SHORT).show()
            return
        }
        if (!newPass.matches(".*[!@#\$%^&_+=].*".toRegex())) {
            Toast.makeText(this, this.resources.getString(R.string.error_respass_pass_1spec), Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confNewPass) {
            Toast.makeText(this, this.resources.getString(R.string.error_respass_pass_compare), Toast.LENGTH_SHORT).show()
            return
        }

        saveNewPass()
    }

    private fun saveNewPass() {
        val oldPass = etPassOld.text.toString()
        val newPass = etPassNew.text.toString()
        val user = auth.currentUser
        if (user != null && user.email != null) {

            showProgressDialog(resources.getString(R.string.text_progress))

            val credential = EmailAuthProvider
                .getCredential(user.email!!, oldPass)
            user?.reauthenticate(credential)
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        user?.updatePassword(newPass)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, this.resources.getString(R.string.text_respass_pass_save), Toast.LENGTH_SHORT).show()
                                    val settingsActivity = Intent(this, SettingsActivity::class.java)
                                    startActivity(settingsActivity)
                                    finish()
                                }
                            }
                    } else {
                        Toast.makeText(this, this.resources.getString(R.string.error_respass_pass_not_right), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        hideProgressDialog()
    }
}