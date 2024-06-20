package com.university.geekway

import com.google.common.truth.Truth.assertThat
import com.university.geekway.utils.EnterUtil
import org.junit.Test

class EnterUtilTest {

    @Test
    fun `empty email or pass returns false`() {
        val result = EnterUtil.validateAuthorizationInput(
            "user@gmail.com",
            ""
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `valid email`() {
        val result = EnterUtil.validateAuthorizationInput(
            "user@gmail.com",
            "!Aa12345678",
        )
        assertThat(result).isTrue()
    }
}