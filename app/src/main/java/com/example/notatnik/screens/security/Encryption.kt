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
            // Generowanie salt
            val random = SecureRandom()
            val salt = ByteArray(256)
            random.nextBytes(salt)

            // 2
            //PBKDF2 - derive the key from the password, don't use passwords directly
            val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance(secretKeyFactory_ALGORITHM)
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, keySpec_ALGORITHM)

            // 3
            //Create initialization vector for AES
            val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
            val iv = ByteArray(16)
            ivRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            // 4
            //Encrypt
            val cipher = Cipher.getInstance(cipher_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(dataToEncrypt)

            // 5
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
            val salt = encryptedData["salt"]
            val iv = encryptedData["iv"]
            val encrypted = encryptedData["encrypted"]

            // 2
            //regenerate key from password
            val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance(secretKeyFactory_ALGORITHM)
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, keySpec_ALGORITHM)

            // 3
            //Decrypt
            val cipher = Cipher.getInstance(cipher_TRANSFORMATION)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            decryptedData = cipher.doFinal(encrypted)
        } catch (e: Exception) {
            Log.e("Notatnik Encryption()", "Decryption exception", e)
        }

        return decryptedData
    }

    companion object {
        private const val cipher_TRANSFORMATION = "AES/CBC/PKCS5Padding" // AES/CBC/PKCS5Padding or AES/GCM/NoPadding
        private const val secretKeyFactory_ALGORITHM = "PBKDF2WithHmacSHA1" // PBKDF2WithHmacSHA1 or PBKDF2WithHmacSHA256 or PBKDF2WithHmacSHA512
        private const val keySpec_ALGORITHM = "AES"
    }
}