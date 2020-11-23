package com.example.notatnik

import com.example.notatnik.screens.security.Encryption
import org.junit.Assert
import org.junit.Test
import java.text.DateFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun check_encrypt_if_password_is_wrong() {
        val pass = "hasło123"
        var password = CharArray(pass.length)
        password = pass.toCharArray()

        val pass2 = "haslo123"
        var password2 = CharArray(pass2.length)
        password2 = pass2.toCharArray()

        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        val password_encrypted = Encryption().encrypt(currentDateTimeString.toByteArray(Charsets.UTF_8), password)

        println("password encrypted: $password_encrypted")

        val password_decrypted = Encryption().decrypt(password_encrypted, password2)

        println("password decrypted: $password_decrypted")

        Assert.assertNotNull("Verify password_decrypted is not null", password_encrypted)
        Assert.assertNull("Verify password_decrypted is null", password_decrypted)

    }
}