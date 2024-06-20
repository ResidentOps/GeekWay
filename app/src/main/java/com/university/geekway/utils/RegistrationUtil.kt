package com.university.geekway.utils

import androidx.core.util.PatternsCompat

object RegistrationUtil {

    fun validateRegistrationInput(
        username: String,
        email: String,
        pass: String,
        confirmPassword: String
    ): Boolean {
        //Проверка заполнения полей
        if (username.isBlank() || email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            return false
        }

        //Проверка правильности написания имени
        if (username.length > 20) {
            return false
        }
        if (username.length < 2) {
            return false
        }
        if (!username.matches("[A-Z,a-z,А-Я,а-я]*".toRegex())) {
            return false
        }

        //Проверка правильности написания Email
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }

        //Проверка правильности написания пароля
        if (pass.length < 8) {
            return false
        }
        if (!pass.matches(".*[A-Z].*".toRegex())) {
            return false
        }
        if (!pass.matches(".*[a-z].*".toRegex())) {
            return false
        }
        if (!pass.matches(".*[0-9].*".toRegex())) {
            return false
        }
        if (!pass.matches(".*[!@#\$%^&_+=].*".toRegex())) {
            return false
        }

        //Проверка совпадения паролей
        if (pass != confirmPassword) {
            return false
        }

        return true
    }
}