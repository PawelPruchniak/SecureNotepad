package com.example.notatnik.screens.security

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.notatnik.R
import com.example.notatnik.database.BooleanPasswordDatabase
import com.example.notatnik.database.PasswordDatabase
import com.example.notatnik.databinding.PasswrdCreateFragmentBinding
import com.example.notatnik.screens.security.biometric.CryptographyManager

// PasswordCreate, PasswordCreateViewModel, PasswordCreateViewModelFactory służą do STWORZENIA PIERWSZEGO HASŁA
class PasswordCreate : Fragment() {

    private lateinit var binding : PasswrdCreateFragmentBinding
    private lateinit var application: Application

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var cryptographyManager: CryptographyManager
    private lateinit var secretKeyName: String
    private lateinit var ciphertext:ByteArray
    private lateinit var initializationVector: ByteArray


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        // Setting binding
        binding = DataBindingUtil.inflate(inflater, R.layout.passwrd_create_fragment, container, false)

        application = requireNotNull(this.activity).application
        val dataSource1 = BooleanPasswordDatabase.getInstance(application).booleanPasswordDatabaseDao
        val dataSource2 = PasswordDatabase.getInstance(application).passwordDatabaseDao

        val viewModelFactory = PasswordCreateViewModelFactory(dataSource1, dataSource2, application)
        val securityViewModel = ViewModelProvider(this, viewModelFactory).get(
                PasswordCreateViewModel::class.java
        )
        binding.viewModel = securityViewModel
        binding.lifecycleOwner = this

        cryptographyManager = CryptographyManager()
        secretKeyName = "default_key_name"
        biometricPrompt = createBiometricPrompt()
        promptInfo = createPromptInfo()

        // Event navigujący do PasswordCheckFragment
        securityViewModel.navigateToPasswordCheckFragment.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                this.findNavController().navigate(
                        PasswordCreateDirections.actionSecurityFragmentToPasswordCheck()
                )
                securityViewModel.onNavigationToPasswordCheckFragmentComplete()
            }
        })

        securityViewModel.navigateToNoteFragment.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                val createdPassword: String = securityViewModel.getPassword()
                this.findNavController().navigate(
                        PasswordCreateDirections.actionSecurityFragmentToNotesFragment(createdPassword, true)
                )
                securityViewModel.onNavigateToNoteFragmentComplete()
            }
        })

        // Event tworzący nowe hasło
        securityViewModel.newPasswordEvent.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                hideKeyboard()
                if(securityViewModel.passwordIsGood(binding.Password1.text.toString(), binding.Password2.text.toString() )){
                    securityViewModel.startFingerprintEnrollment()
                }
                else{
                    binding.errorTxt.text = getString(R.string.password_dont_match_string)
                }
                securityViewModel.onNewPasswordEventComplete()
            }
        })

        // Event tworzący nowe hasło
        securityViewModel.fingerprintEnrollment.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                authenticateToEncrypt()
                securityViewModel.onStartFingerprintEnrollmentComplete()
            }
        })

        // Event sprawdzający czy hasło zostało już stworzone, jeżeli tak to naviguje
        securityViewModel.passwordExists.observe(viewLifecycleOwner, { password ->
            if(password != null && password.passwordBool){
                securityViewModel.navigateToPasswordCheckFragment()
                println("PasswordCreateFragment: Hasło zostało juz stworzone!")
            }
            else{
                println("PasswordCreateFragment: Hasło nie zostało stworzone!")
            }
        })

        return binding.root
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d("PasswordCreateFragment", "$errorCode :: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d("PasswordCreateFragment", "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d("PasswordCreateFragment", "Authentication was successful")
                processData(result.cryptoObject)
            }
        }

        val biometricPrompt = BiometricPrompt(this, executor, callback)
        return biometricPrompt
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.prompt_info_title))
                .setDescription(getString(R.string.prompt_info_description))
                .setConfirmationRequired(false)
                .setNegativeButtonText(getString(R.string.negative_button_text))
                .build()
        return promptInfo
    }

    private fun authenticateToEncrypt() {
        if (BiometricManager.from(application).canAuthenticate() == BiometricManager
                        .BIOMETRIC_SUCCESS) {
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun processData(cryptoObject: BiometricPrompt.CryptoObject?) {
            val text = binding.viewModel!!.getPassword()
            val encryptedData = cryptographyManager.encryptData(text, cryptoObject?.cipher!!)
            ciphertext = encryptedData.ciphertext
            initializationVector = encryptedData.initializationVector
            println("ciphertext: $ciphertext")
            println("iv: $initializationVector")
            binding.viewModel!!.saveEncryptedPassword(ciphertext, initializationVector)
            binding.viewModel!!.navigateToNoteFragment()
        }

    // FUNKCJE DO UKRYCIA KLAWIATURY PO KLIKNIECIU PRZYCISKU
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}