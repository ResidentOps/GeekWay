package com.university.geekway.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.university.geekway.EnterActivity
import com.university.geekway.HOME
import com.university.geekway.R

class ForgotPasswordFragment : Fragment() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var textForgotPassword: TextView
    private lateinit var textEnterEmailFP1: TextView
    private lateinit var textEnterEmailFP2: TextView
    private lateinit var etEmailFP: EditText
    private lateinit var btnSubmitFP: Button

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    private lateinit var rf: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        //Инициализация виджетов, текстов и кнопок
        textForgotPassword = root.findViewById(R.id.textForgotPasswordFragment)
        textEnterEmailFP1 = root.findViewById(R.id.textEnterEmailForgotPassword1Fragment)
        textEnterEmailFP2 = root.findViewById(R.id.textEnterEmailForgotPassword2Fragment)
        etEmailFP = root.findViewById(R.id.editEmailForgotPasswordFragment)
        btnSubmitFP = root.findViewById(R.id.buttonSubmitForgotPasswordFragment)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        rf = db.getReference()

        //Кнопка "Отправить" (отправляет ссылку на введенный Email на восстановление пароля пользователя)
        btnSubmitFP.setOnClickListener {
            submitFP()
        }

        //Кнопка "Отмена" (открывает ResetPasswordFragment)
        val buttonCancelFP: Button = root.findViewById(R.id.buttonCancelForgotPasswordFragment)
        buttonCancelFP.setOnClickListener {
            HOME.navController.navigate(R.id.action_forgotPasswordFragment_to_resetPasswordFragment)
        }

        return root
    }

    //Восстановление пароля пользователя
    private fun submitFP() {
        val email = etEmailFP.text.toString()

        //Проверка заполнения поля
        if (email.isBlank()) {
            Toast.makeText(HOME, "Поле должно быть заполнено!", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка правильности написания Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(HOME, "Проверьте правильность написания E-mail адреса!", Toast.LENGTH_SHORT).show()
            return
        }

        //Процесс запроса для отправки ссылки
        auth.sendPasswordResetEmail(email).addOnCompleteListener(HOME) {

            //Проверка на зарегестрированного пользователя
            checkUserExistsFP()

            if (it.isSuccessful) {
                auth.signOut()
                val enterActivity = Intent(this. requireContext(), EnterActivity::class.java)
                startActivity(enterActivity)
                Toast.makeText(HOME,"Откройте полученное письмо в введеном E-mail.", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(HOME, "Введенный E-mail не зарегестрирован или был удален!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Проверка на зарегестрированного пользователя
    private fun checkUserExistsFP() {
        val user = auth.currentUser
        if (user === null) {
            Toast.makeText(HOME, "Ошибка отправки ссылки для восстановления пароля!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(HOME, "Ссылка для восстановления пароля успешно отправлена.", Toast.LENGTH_SHORT).show()
        }
    }
}