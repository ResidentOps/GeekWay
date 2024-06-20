package com.university.geekway.utils

import androidx.core.util.PatternsCompat

object AddPlaceUtil {

    fun validatePlaceInput(
        placename: String,
        placedescription: String,
        placecategoryname: String,
        placecityname: String,
        placeage: String,
        placetime: String,
        placeaddress: String,
        placetelephone: String,
        placeweb: String,
        placepublic: String,
        placeLat: String,
        placeLng: String
    ): Boolean {
        //Проверка заполнения полей
        if (placename.isBlank() || placedescription.isBlank() || placecategoryname.isBlank() || placecityname.isBlank() || placeage.isBlank()) {
            return false
        }

        //Проверка правильности написания названия категории
        if (!placecategoryname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            return false
        }

        //Проверка правильности написания названия города
        if (!placecityname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            return false
        }

        //Проверка правильности написания ссылки
        if (!PatternsCompat.WEB_URL.matcher(placeweb).matches()) {
            return false
        }

        //Проверка правильности написания ссылки на соцсеть
        if (!PatternsCompat.WEB_URL.matcher(placepublic).matches()) {
            return false
        }

        //Проверка правильности написания телефона
        if (placetelephone.length < 12) {
            return false
        }
        if (placetelephone.length > 12) {
            return false
        }
        if (placetelephone[0] != '+') {
            return false
        }
        if (!placetelephone.matches(".*[0-9].*".toRegex())) {
            return false
        }

        return true
    }
}