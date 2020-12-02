package com.example.notatnik.screens.security

import android.util.Log
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal class Encryption {


    fun encrypt(dataToEncrypt: ByteArray,
                password: CharArray): HashMap<String, ByteArray> {
        val encryptedData = HashMap<String, ByteArray>()

        try {
            // 1
            // Generowanie salt
            val random = SecureRandom.getInstanceStrong()
            val salt = ByteArray(256)
            random.nextBytes(salt)

            // 2
            // PBKDF2 za pomocą hasła, salt i iteracji tworzy klucz
            val pbKeySpec = PBEKeySpec(password, salt, 64000, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA512")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, "AES")

            // 3
            // Ponieważ używamy CBC musimy stworzyć Initialization Vector (IV)
            val ivRandom = SecureRandom.getInstanceStrong()
            val iv = ByteArray(16)
            ivRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            // 4
            // Szyfrujemy
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(dataToEncrypt)

            // 5
            // Zapisywanie
            encryptedData["salt"] = salt
            encryptedData["iv"] = iv
            encryptedData["encrypted"] = encrypted
        } catch (e: Exception) {
            Log.e("Notatnik Encryption()", "encryption exception", e)
        }

        return encryptedData
    }

    fun decrypt(encryptedData: HashMap<String, ByteArray>, password: CharArray): ByteArray? {
        var decryptedData: ByteArray? = null
        try {
            // 1
            // Pobieramy sól, IV, i zaszyfrowane dane
            val salt = encryptedData["salt"]
            val iv = encryptedData["iv"]
            val encrypted = encryptedData["encrypted"]

            // 2
            //  Wyciąganie klucza z hasła, tworzenie go
            val pbKeySpec = PBEKeySpec(password, salt, 64000, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA512")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, "AES")

            // 3
            // Rozszyfrowywanie
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            decryptedData = cipher.doFinal(encrypted)

        } catch (e: Exception) {
            Log.e("Notatnik Encryption()", "Decryption exception", e)
        }

        return decryptedData
    }

}