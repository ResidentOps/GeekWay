package com.university.geekway

import com.google.common.truth.Truth.assertThat
import com.university.geekway.utils.RegistrationUtil
import org.junit.Test

class RegistrationUtilTest{

    @Test
    fun `empty username or email or pass or confirmPassword returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            "Grigorii",
            "user@gmail.com",
            "!Aa12345678",
            "!Aa12345678"
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `valid username`() {
        val result = RegistrationUtil.validateRegistrationInput(
            "Grigorii",
            "user@gmail.com",
            "!Aa12345678",
            "!Aa12345678"
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `valid email`() {
        val result = RegistrationUtil.validateRegistrationInput(
            "Grigorii",
            "usergmail.com",
            "!Aa12345678",
            "!Aa12345678"
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `valid pass`() {
        val result = RegistrationUtil.validateRegistrationInput(
            "Grigorii",
            "user@gmail.com",
            "!Aa12345678",
            "!Aa12345678"
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `pass and confirmPassword same`() {
        val result = RegistrationUtil.validateRegistrationInput(
            "Grigorii",
            "user@gmail.com",
            "!Aa12345678",
            "!Aa12345678"
        )
        assertThat(result).isTrue()
    }
}