package com.example.notatnik

import com.example.notatnik.screens.security.Encryption
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.RepeatedTest
import java.text.DateFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @RepeatedTest(10)
    fun check_encrypt_if_password_is_wrong() {
        val passwordCorrect = "hasło123".toCharArray()
        val passwordWrong = "haslo123".toCharArray()
        val dataToEncrypt = DateFormat.getDateTimeInstance().format(Date())

        val dataEncrypted = Encryption().encrypt(dataToEncrypt.toByteArray(Charsets.UTF_8), passwordCorrect)
        val dataDecrypted = Encryption().decrypt(dataEncrypted, passwordWrong)

        Assert.assertNotNull("Verify dataEncrypted is not null", dataEncrypted)
        Assert.assertNull("Verify dataDecrypted is null", dataDecrypted)
    }

    @Test
    fun check_encrypt_if_password_is_wrong2() {
        val passwordCorrect = "eagwejkgba34Y a34y 4a3a wEAG 1RT1G1g!132G238465 ?h345 Q".toCharArray()
        val passwordWrong = "eagwejkgba34Y a34y 4a3a WEAG 1RT1G1g!132G238465 ?h345 Q".toCharArray()
        val dataToEncrypt = DateFormat.getDateTimeInstance().format(Date())

        val dataEncrypted = Encryption().encrypt(dataToEncrypt.toByteArray(Charsets.UTF_8),passwordCorrect)
        val dataDecrypted = Encryption().decrypt(dataEncrypted, passwordWrong)

        Assert.assertNotNull("Verify dataEncrypted is not null", dataEncrypted)
        Assert.assertNull("Verify dataDecrypted is null", dataDecrypted)

    }

    @Test
    fun check_encrypt_if_password_is_correct() {
        val passwordCorrect = "hasło123".toCharArray()
        val passwordCorrect2 = "hasło123".toCharArray()
        val dataToEncrypt = DateFormat.getDateTimeInstance().format(Date())

        val dataEncrypted = Encryption().encrypt(dataToEncrypt.toByteArray(Charsets.UTF_8),passwordCorrect)
        val dataDecrypted = Encryption().decrypt(dataEncrypted, passwordCorrect2)

        Assert.assertNotNull("Verify dataEncrypted is not null", dataEncrypted)
        Assert.assertNotNull("Verify dataDecrypted is not null", dataDecrypted)

    }

    @Test
    fun check_encrypt_if_password_is_correct2() {
        val passwordCorrect = "eagwejkgba34Y a34y 4a3a WEAG 1RT1G1g!132G238465 ?h345 Q".toCharArray()
        val passwordCorrect2 = "eagwejkgba34Y a34y 4a3a WEAG 1RT1G1g!132G238465 ?h345 Q".toCharArray()
        val dataToEncrypt = DateFormat.getDateTimeInstance().format(Date())

        val dataEncrypted = Encryption().encrypt(dataToEncrypt.toByteArray(Charsets.UTF_8),passwordCorrect)
        val dataDecrypted = Encryption().decrypt(dataEncrypted, passwordCorrect2)

        Assert.assertNotNull("Verify dataEncrypted is not null", dataEncrypted)
        Assert.assertNotNull("Verify dataDecrypted is not null", dataDecrypted)

    }
    @Test
    fun check_if_data_is_decrypted_correctly() {
        val passwordCorrect = "eagwejkgba34Y a34y 4a3a WEAG 1RT1G1g!132G238465 ?h345 Q".toCharArray()
        val passwordCorrect2 = "eagwejkgba34Y a34y 4a3a WEAG 1RT1G1g!132G238465 ?h345 Q".toCharArray()
        val dataToEncryptString = "dane do zaszyfrowania"

        val dataEncrypted = Encryption().encrypt(dataToEncryptString.toByteArray(Charsets.UTF_8), passwordCorrect)
        val dataDecrypted = Encryption().decrypt(dataEncrypted, passwordCorrect2)

        var dataDecryptedString: String? = null
        dataDecrypted?.let {
            dataDecryptedString = String(it, Charsets.UTF_8)
        }

        Assert.assertNotNull("Verify dataEncrypted is not null", dataEncrypted)
        Assert.assertNotNull("Verify dataDecrypted is not null", dataDecrypted)
        Assert.assertEquals("Verify dataToEncryptString is equals to dataDecryptedString", dataToEncryptString, dataDecryptedString)
    }

    @Test
    fun check_if_data_is_decrypted_correctly2() {
        val passwordCorrect = "eagwejkgba34Y a34y 4a3a WEAG 1RT1G1g!132G238465 ?h345 Q".toCharArray()
        val passwordCorrect2 = "eagwejkgba34Y a34y 4a3a WEAG 1RT1G1g!132G238465 ?h345 Q".toCharArray()
        val dataToEncryptString = "VAWEGHEEWARHHRA EHRA EHRAE RAEH Erh$%HJA$%A  AEJHAg a3wGAG# A1"

        val dataEncrypted = Encryption().encrypt(dataToEncryptString.toByteArray(Charsets.UTF_8), passwordCorrect)
        val dataDecrypted = Encryption().decrypt(dataEncrypted, passwordCorrect2)

        var dataDecryptedString: String? = null
        dataDecrypted?.let {
            dataDecryptedString = String(it, Charsets.UTF_8)
        }

        Assert.assertNotNull("Verify dataEncrypted is not null", dataEncrypted)
        Assert.assertNotNull("Verify dataDecrypted is not null", dataDecrypted)
        Assert.assertEquals("Verify dataToEncryptString is equals to dataDecryptedString", dataToEncryptString, dataDecryptedString)
    }

}