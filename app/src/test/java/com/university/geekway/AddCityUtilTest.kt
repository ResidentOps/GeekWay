package com.university.geekway

import com.google.common.truth.Truth
import com.university.geekway.utils.AddCityUtil
import org.junit.Test

class AddCityUtilTest {

    @Test
    fun `empty cityname or citydescription returns false`() {
        val result = AddCityUtil.validateAddCityInput(
            "Санкт-Петербург",
            "Культурная столица России, окно в Европу, IT-центр, кузница комиксов и ещё много других эпитетов, суть которых заключается в одном: гикам здесь скучать не приходится! За последнее десятилетие в Питере успело появиться (и исчезнуть) огромное количество комикс-шопов, гик-фестивалей и даже тематических кафе и баров."
        )
        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `valid cityname`() {
        val result = AddCityUtil.validateAddCityInput(
            "Санкт_Петербург",
            "Культурная столица России, окно в Европу, IT-центр, кузница комиксов и ещё много других эпитетов, суть которых заключается в одном: гикам здесь скучать не приходится! За последнее десятилетие в Питере успело появиться (и исчезнуть) огромное количество комикс-шопов, гик-фестивалей и даже тематических кафе и баров."
        )
        Truth.assertThat(result).isTrue()
    }
}