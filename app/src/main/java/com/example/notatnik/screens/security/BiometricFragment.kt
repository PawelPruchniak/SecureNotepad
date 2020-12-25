package com.example.notatnik.screens.security

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.*
import android.util.Base64
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
import com.example.notatnik.screens.security.biometric.DEFAULT_KEY_NAME
import com.example.notatnik.screens.security.biometric.FingerprintAuthenticationDialogFragment
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*

class BiometricFragment : Fragment(), FingerprintAuthenticationDialogFragment.Callback {

    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var application: Application
    private lateinit var binding: BiometricFragmentBinding
    private var passwordBool: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

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

        setupKeyStoreAndKeyGenerator()
        val (defaultCipher: Cipher, cipherNotInvalidated: Cipher) = setupCiphers()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        biometricPrompt = createBiometricPrompt()
        setUpAuthorizeButton(cipherNotInvalidated, defaultCipher)

        // Event sprawdzający czy hasło zostało już stworzone, jeżeeli tak to naviguje
        biometricViewModel.passwordExists.observe(viewLifecycleOwner, { password ->
            if(password != null && password.passwordBool){
                println("Password was created!")
                this.passwordBool = password.passwordBool
            }
            else{
                println("Password jest null!")
            }
        })

        return binding.root
    }

    /**
     * Enables or disables purchase buttons and sets the appropriate click listeners.
     *
     * @param cipherNotInvalidated cipher for the not invalidated purchase button
     * @param defaultCipher the default cipher, used for the purchase button
     */
    private fun setUpAuthorizeButton(cipherNotInvalidated: Cipher, defaultCipher: Cipher) {
        val authorizeButton = binding.authorizeBtn

        if (BiometricManager.from(
                application).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            createKey(DEFAULT_KEY_NAME)
            createKey(KEY_NAME_NOT_INVALIDATED, false)

            authorizeButton.run {
                isEnabled = true
                setOnClickListener(PurchaseButtonClickListener(defaultCipher, DEFAULT_KEY_NAME))
            }
        } else {
            print(getString(R.string.setup_lock_screen))
            authorizeButton.isEnabled = false
        }
    }

    /**
     * Sets up KeyStore and KeyGenerator
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
     * Sets up default cipher and a non-invalidated cipher
     */
    private fun setupCiphers(): Pair<Cipher, Cipher> {
        val defaultCipher: Cipher
        val cipherNotInvalidated: Cipher
        try {
            val cipherString = "$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7"
            defaultCipher = Cipher.getInstance(cipherString)
            cipherNotInvalidated = Cipher.getInstance(cipherString)
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchPaddingException ->
                    throw RuntimeException("Failed to get an instance of Cipher", e)
                else -> throw e
            }
        }
        return Pair(defaultCipher, cipherNotInvalidated)
    }

    /**
     * Initialize the [Cipher] instance with the created key in the [createKey] method.
     *
     * @param keyName the key name to init the cipher
     * @return `true` if initialization succeeded, `false` if the lock screen has been disabled or
     * reset after key generation, or if a fingerprint was enrolled after key generation.
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
     * Proceed with the purchase operation
     *
     * @param withBiometrics `true` if the purchase was made by using a fingerprint
     * @param crypto the Crypto object
     */
    override fun onPurchased(withBiometrics: Boolean, crypto: BiometricPrompt.CryptoObject?) {
        if (withBiometrics) {
            // If the user authenticated with fingerprint, verify using cryptography and then show
            // the confirmation message.
            crypto?.cipher?.let { tryEncrypt(it) }
        }
    }

    // Show confirmation message. Also show crypto information if fingerprint was used.
    private fun showConfirmation(encrypted: ByteArray? = null) {
        if (encrypted != null) {
            binding.encryptedMessage.run {
                visibility = View.VISIBLE
                text = Base64.encodeToString(encrypted, 0 /* flags */)
            }
        }
    }

    /**
     * Tries to encrypt some data with the generated key from [createKey]. This only works if the
     * user just authenticated via fingerprint.
     */
    private fun tryEncrypt(cipher: Cipher) {
        try {
            showConfirmation(cipher.doFinal(SECRET_MESSAGE.toByteArray()))
        } catch (e: Exception) {
            when (e) {
                is BadPaddingException,
                is IllegalBlockSizeException -> {
                    Log.e(TAG, "Failed to encrypt the data with the generated key. ${e.message}")
                }
                else -> throw e
            }
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with a fingerprint.
     *
     * @param keyName the name of the key to be created
     * @param invalidatedByBiometricEnrollment if `false` is passed, the created key will not be
     * invalidated even if a new fingerprint is enrolled. The default value is `true` - the key will
     * be invalidated if a new fingerprint is enrolled.
     */

    override fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of enrolled
        // fingerprints has changed.
        try {
            keyStore.load(null)

            val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val builder = KeyGenParameterSpec.Builder(keyName, keyProperties)
                .setBlockModes(BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7)
                .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)

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
                onPurchased(true, result.cryptoObject)
                startNavigate()
            }
        }

        val biometricPrompt = BiometricPrompt(this, executor, callback)
        return biometricPrompt
    }

    private fun startNavigate() {
        if(passwordBool){
            this.findNavController().navigate(BiometricFragmentDirections.actionBiometricFragmentToNotesFragment("fwa92q.gwalg23ga32kga22a1!y1gsa23332hSaw", false))
        }
        else{
            binding.viewModel!!.passwordCreated()
            this.findNavController().navigate(BiometricFragmentDirections.actionBiometricFragmentToNotesFragment("fwa92q.gwalg23ga32kga22a1!y1gsa23332hSaw", true))
        }
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_info_title))
            .setDescription(getString(R.string.prompt_info_description))
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.prompt_info_use_app_password))
            //.setDeviceCredentialAllowed(true) // Allow PIN/pattern/password authentication.
            // Also note that setDeviceCredentialAllowed and setNegativeButtonText are
            // incompatible so that if you uncomment one you must comment out the other
            .build()
        return promptInfo
    }

    private inner class PurchaseButtonClickListener internal constructor(
        internal var cipher: Cipher,
        internal var keyName: String
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
        private const val DIALOG_FRAGMENT_TAG = "myFragment"
        private const val KEY_NAME_NOT_INVALIDATED = "key_not_invalidated"
        private const val SECRET_MESSAGE = "Very secret message"
        private const val TAG = "MainActivity"
    }
}