package com.example.notatnik.screens.security

import android.app.Application
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.notatnik.R
import com.example.notatnik.database.PasswordDatabase
import com.example.notatnik.databinding.BiometricFragmentBinding
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class BiometricFragment : Fragment() {

    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var application: Application
    private lateinit var binding: BiometricFragmentBinding
    private var passwordBool: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Setting binding
        binding = DataBindingUtil.inflate(inflater, R.layout.biometric_fragment, container, false)

        application = requireNotNull(this.activity).application
        val dataSource = PasswordDatabase.getInstance(application).passwordDatabaseDao

        val viewModelFactory = BiometricViewModelFactory(dataSource, application)
        val biometricViewModel = ViewModelProvider(this, viewModelFactory).get(
                BiometricViewModel::class.java
        )
        binding.viewModel = biometricViewModel
        binding.lifecycleOwner = this

        // keyStore i KeyGenerator
        setupKeyStoreAndKeyGenerator()

        // Cipher
        val cipher: Cipher = setupCiphers()

        // BiometricPrompt
        biometricPrompt = createBiometricPrompt()

        // AuthorizeButton
        setUpAuthorizeButton(cipher)

        // Event sprawdzający czy hasło zostało już stworzone
        biometricViewModel.passwordExists.observe(viewLifecycleOwner, { password ->
            if(password != null && password.passwordBool){
                println("Password was created!")
                this.passwordBool = password.passwordBool
            }
            else{
                println("Password was not created!")
            }
        })

        return binding.root
    }

    /**
     * Tworzy authorizeButton i przypisuje mu odpowiedni ClickListener
     *
     * @param cipher obiekt Cipher, używany do uwierzytelniania.
     */
    private fun setUpAuthorizeButton(defaultCipher: Cipher) {
        val authorizeButton = binding.authorizeBtn

        if (BiometricManager.from(
                application).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            createKey("default_key")

            authorizeButton.run {
                isEnabled = true
                setOnClickListener(PurchaseButtonClickListener(defaultCipher, "default_key"))
            }
        } else {
            print(getString(R.string.setup_lock_screen))
            authorizeButton.isEnabled = false
        }
    }

    /**
     * Funkcja tworząca KeyStore i KeyGenerator
     */
    private fun setupKeyStoreAndKeyGenerator() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to get an instance of KeyStore", e)
        }

        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchProviderException ->
                    throw RuntimeException("Failed to get an instance of KeyGenerator", e)
                else -> throw e
            }
        }
    }


    /**
     * Funkcja tworząca obiekt Cipher
     */
    private fun setupCiphers(): Cipher {
        val defaultCipher: Cipher
        try {
            val cipherString = "$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7"
            defaultCipher = Cipher.getInstance(cipherString)
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchPaddingException ->
                    throw RuntimeException("Failed to get an instance of Cipher", e)
                else -> throw e
            }
        }
        return defaultCipher
    }

    /**
     * Inicjalizowanie obiektu [Cipher] z stworzonym kluczem w metodzie [createKey].
     *
     * @param cipher obiekt Cipher
     * @param keyName nazwa klucza do inicjalizacji cipher
     */
    private fun initCipher(cipher: Cipher, keyName: String): Boolean {
        try {
            keyStore.load(null)
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(keyName, null) as SecretKey)
            return true
        } catch (e: Exception) {
            when (e) {
                is KeyPermanentlyInvalidatedException -> return false
                is KeyStoreException,
                is CertificateException,
                is UnrecoverableKeyException,
                is IOException,
                is NoSuchAlgorithmException,
                is InvalidKeyException -> throw RuntimeException("Failed to init Cipher", e)
                else -> throw e
            }
        }
    }


    /**
     * Funkcja tworząca symetryczny klucz w Android Key Store, który może być użyty tylko po
     * uwierzytelnieniu przez odcisk palca użytkownika
     *
     * @param keyName nazwa klucza który zostanie stworzony w tym wypadku "default_key"
     */
    fun createKey(keyName: String) {
        try {
            keyStore.load(null)

            val keyProperties = PURPOSE_ENCRYPT or PURPOSE_DECRYPT
            val builder = KeyGenParameterSpec.Builder(keyName, keyProperties)
                .setBlockModes(BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7)

            keyGenerator.run {
                init(builder.build())
                generateKey()
            }
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is InvalidAlgorithmParameterException,
                is CertificateException,
                is IOException -> throw RuntimeException(e)
                else -> throw e
            }
        }
    }

    /**
     * Funkcja tworząca BiometricPrompt oraz obsługująca Error Failed Succeeded uwierzytelniania odciskiem palca
     */
    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "$errorCode :: $errString")
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Log.d(TAG, "Authentication closed")
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                startNavigate()
            }
        }

        return BiometricPrompt(this, executor, callback)
    }


    /**
     * Funkcja nawigująca do fragmentu z notatkami
     */
    private fun startNavigate() {
        if(passwordBool){
            this.findNavController().navigate(BiometricFragmentDirections.actionBiometricFragmentToNotesFragment(false))
        }
        else{
            binding.viewModel!!.passwordCreated()
            this.findNavController().navigate(BiometricFragmentDirections.actionBiometricFragmentToNotesFragment(true))
        }
    }


    /**
     * Funkcja tworząca PromptInfo (planszę pokazująca "chęć" uwierzytelniania)
     */
    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_info_title))
            .setDescription(getString(R.string.prompt_info_description))
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.prompt_info_use_app_password))
            .build()
    }

    /**
     * Funkcja ustawiająca ClickListener
     */
    private inner class PurchaseButtonClickListener(
         var cipher: Cipher,
         var keyName: String
    ) : View.OnClickListener {

        override fun onClick(view: View) {
            binding.encryptedMessage.visibility = View.GONE

            val promptInfo = createPromptInfo()

            if (initCipher(cipher, keyName)) {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val TAG = "MainActivity"
    }
}