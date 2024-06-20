package com.university.geekway.utils

object AddCityUtil {

    fun validateAddCityInput(
        cityname: String,
        citydescription: String
    ): Boolean {
        //Проверка заполнения полей
        if (cityname.isBlank() || citydescription.isBlank()) {
            return false
        }

        //Проверка правильности написания названия города
        if (!cityname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            return false
        }

        return true
    }
}