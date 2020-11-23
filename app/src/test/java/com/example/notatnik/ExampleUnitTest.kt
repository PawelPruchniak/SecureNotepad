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
        val passwordFirst = "hasło123".toCharArray()
        val passwordSecond = "haslo123".toCharArray()

        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        val passwordEncrypted = Encryption().encrypt(currentDateTimeString.toByteArray(Charsets.UTF_8), passwordFirst)
        println("password encrypted: $passwordEncrypted")

        val passwordDecrypted = Encryption().decrypt(passwordEncrypted, passwordSecond)
        println("password decrypted: $passwordDecrypted")

        Assert.assertNotNull("Verify password encrypted is not null", passwordEncrypted)
        Assert.assertNull("Verify password decrypted is null", passwordDecrypted)
    }

    @Test
    fun check_encrypt_if_password_is_correct() {
        val passwordFirst = "hasło123".toCharArray()
        val passwordSecond = "hasło123".toCharArray()

        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        val passwordEncrypted = Encryption().encrypt(currentDateTimeString.toByteArray(Charsets.UTF_8), passwordFirst)
        println("password encrypted: $passwordEncrypted")

        val passwordDecrypted = Encryption().decrypt(passwordEncrypted, passwordSecond)
        println("password decrypted: $passwordDecrypted")

        Assert.assertNotNull("Verify password encrypted is not null", passwordEncrypted)
        Assert.assertNotNull("Verify password decrypted is not null", passwordDecrypted)

    }
}