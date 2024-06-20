package com.university.geekway.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.university.geekway.ForgotPasswordActivity
import com.university.geekway.HOME
import com.university.geekway.R
import com.university.geekway.SettingsActivity

class ResetPasswordFragment : Fragment() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var textResetPass: TextView
    private lateinit var etPassOld: EditText
    private lateinit var etPassNew: EditText
    private lateinit var etPassConfNew: EditText
    private lateinit var textFP: TextView
    private lateinit var btnSaveResetPass: Button
    private lateinit var btnCancelResetPass: Button

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    var refUsers: DatabaseReference? = null
    var fbUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_reset_password, container, false)

        //Инициализация виджетов, текстов и кнопок
        textResetPass = root.findViewById(R.id.textResetPasswordFragment)
        etPassOld = root.findViewById(R.id.editResetPassOldFragment)
        etPassNew = root.findViewById(R.id.editResetPassNewFragment)
        etPassConfNew = root.findViewById(R.id.editResetPassNewConfFragment)
        textFP = root.findViewById(R.id.textResetPassForgotPasswordFragment)
        btnSaveResetPass = root.findViewById(R.id.buttonSaveResetPasswordFragment)
        btnCancelResetPass = root.findViewById(R.id.buttonCancelResetPasswordFragment)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        fbUser = auth.currentUser
        refUsers = db.reference.child("users").child(fbUser!!.uid)

        //Текст "Забыли пароль?" (открывает ForgotPasswordActivity)
        textFP.setOnClickListener {
            HOME.navController.navigate(R.id.action_resetPasswordFragment_to_forgotPasswordFragment)
        }

        //Кнопка "Сохранить" (сохраняет новый пароль пользователя и открывает SettingsFragment)
        btnSaveResetPass.setOnClickListener {
            resetPassword()
        }

        //Кнопка "Отмена" (открывает SettingsFragment)
        btnCancelResetPass.setOnClickListener {
            HOME.navController.navigate(R.id.action_resetPasswordFragment_to_settingsFragment)
        }

        return root
    }

    //Изменение пароля пользователя
    private fun resetPassword() {
        val oldPass = etPassOld.text.toString()
        val newPass = etPassNew.text.toString()
        val confNewPass = etPassConfNew.text.toString()

        //Проверка заполнения полей
        if (oldPass.isBlank() || newPass.isBlank() || confNewPass.isBlank()) {
            Toast.makeText(HOME, "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка правильности написания пароля
        if (newPass.length < 8) {
            Toast.makeText(HOME, "Пароль должен содержать минимум 8 символов!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!newPass.matches(".*[A-Z].*".toRegex())) {
            Toast.makeText(HOME, "Пароль должен содержать минимум 1 заглавный символ!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!newPass.matches(".*[a-z].*".toRegex())) {
            Toast.makeText(HOME, "Пароль должен содержать минимум 1 строчный символ!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!newPass.matches(".*[0-9].*".toRegex())) {
            Toast.makeText(HOME, "Пароль должен содержать минимум 1 цифру!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!newPass.matches(".*[!@#\$%^&_+=].*".toRegex())) {
            Toast.makeText(HOME, "Пароль должен содержать минимум 1 специальный символ! (!@#\$%^&_+=)", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка совпадения паролей
        if (newPass != confNewPass) {
            Toast.makeText(HOME, "Пароли не совпадают!", Toast.LENGTH_SHORT).show()
            return
        }

        //Сохранение нового пароля пользователя
        saveNewPass()
    }

    //Сохранение нового пароля пользователя
    private fun saveNewPass() {
        val oldPass = etPassOld.text.toString()
        val newPass = etPassNew.text.toString()
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider
                .getCredential(user.email!!, oldPass)
            user?.reauthenticate(credential)
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        //Toast.makeText(this, "Изменение пароля успешно завершено.", Toast.LENGTH_SHORT).show()
                        user?.updatePassword(newPass)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    refUsers?.child("Password")?.setValue(newPass)
                                    Toast.makeText(HOME, "Пароль успешно изменен.", Toast.LENGTH_SHORT).show()
                                    HOME.navController.navigate(R.id.action_resetPasswordFragment_to_settingsFragment)
                                }
                            }
                    } else {
                        Toast.makeText(HOME, "Введен неверный пароль!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}