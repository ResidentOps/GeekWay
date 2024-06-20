package com.university.geekway.utils

object AddCategoryUtil {

    fun validateAddCategoryInput(
        categoryname: String,
        cityname: String
    ): Boolean {
        //Проверка заполнения полей
        if (categoryname.isBlank() || cityname.isBlank()) {
            return false
        }

        //Проверка правильности написания названия категории
        if (!categoryname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            return false
        }

        //Проверка правильности написания названия города
        if (!cityname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            return false
        }

        return true
    }
}