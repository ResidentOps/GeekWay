package com.university.geekway.utils

import androidx.core.util.PatternsCompat

object EnterUtil {

    fun validateAuthorizationInput(
        email: String,
        pass: String
    ): Boolean {
        //Проверка заполнения полей
        if (email.isBlank() || pass.isBlank()) {
            return false
        }

        //Проверка правильности написания Email
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }

        return true
    }
}