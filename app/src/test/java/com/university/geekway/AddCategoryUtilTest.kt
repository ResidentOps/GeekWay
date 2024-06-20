package com.university.geekway

import com.google.common.truth.Truth
import com.university.geekway.utils.AddCategoryUtil
import org.junit.Test

class AddCategoryUtilTest {

    @Test
    fun `empty categoryname or cityname returns false`() {
        val result = AddCategoryUtil.validateAddCategoryInput(
            "Магазины",
            ""
        )
        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `valid categoryname`() {
        val result = AddCategoryUtil.validateAddCategoryInput(
            "Магазины",
            "Санкт-Петербург"
        )
        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `valid cityname`() {
        val result = AddCategoryUtil.validateAddCategoryInput(
            "Магазины",
            "Санкт-Петербург"
        )
        Truth.assertThat(result).isTrue()
    }
}