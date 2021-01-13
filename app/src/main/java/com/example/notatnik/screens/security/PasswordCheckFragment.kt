package com.example.notatnik.screens.security

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color.RED
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
import com.example.notatnik.database.NotesDatabase
import com.example.notatnik.database.PasswordDatabase
import com.example.notatnik.databinding.PasswordCheckFragmentBinding
import com.example.notatnik.screens.security.biometric.CryptographyManager

// PasswordCheckFragment, PasswordCheckViewModel, PasswordCheckViewModelFactory służą do SPRAWDZENIA POPRAWNOŚCI HASŁA
class PasswordCheckFragment : Fragment() {

    private lateinit var binding : PasswordCheckFragmentBinding
    private lateinit var application: Application

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var readyToEncrypt: Boolean = false
    private lateinit var cryptographyManager: CryptographyManager
    private lateinit var secretKeyName: String
    private lateinit var ciphertext:ByteArray
    private lateinit var initializationVector: ByteArray

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        // Setting binding
        binding = DataBindingUtil.inflate(inflater, R.layout.password_check_fragment, container, false)

        application = requireNotNull(this.activity).application
        val dataSource1 = NotesDatabase.getInstance(application).notesDatabaseDao
        val dataSource2 = PasswordDatabase.getInstance(application).passwordDatabaseDao

        val viewModelFactory = PasswordCheckViewModelFactory(dataSource1, dataSource2, application)
        val passwordViewModel = ViewModelProvider(this, viewModelFactory).get(
                PasswordCheckViewModel::class.java
        )
        binding.viewModel = passwordViewModel
        binding.lifecycleOwner = this


        cryptographyManager = CryptographyManager()
        secretKeyName = "default_key_name"
        biometricPrompt = createBiometricPrompt()
        promptInfo = createPromptInfo()

        // Event który sprawdza poprawność wpisanego hasła
        passwordViewModel.checkPasswordEvent.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                hideKeyboard()
                if (passwordViewModel.checkPassword(binding.passwordTxt.text.toString())) {
                    passwordViewModel.passwordMatch()
                } else {
                    passwordViewModel.passwordDontMatch()
                }
                passwordViewModel.onCheckPasswordEventComplete()
            }
        })

        // Event zmieniający txtView aby wyświetlał error po niepoprawnym wpisaniu hasła
        passwordViewModel.changeLoginTxtEvent.observe(viewLifecycleOwner) { isTrue ->
            if (isTrue) {
                binding.loginTxt.text = getString(R.string.wrong_password_string)
                binding.loginTxt.setTextColor(RED)
                passwordViewModel.onChangeLoginTxtEventComplete()
            }
        }

        // Event navigujący do NotesFragment
        passwordViewModel.navigateToNotesFragment.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                val password = passwordViewModel.getPassword().toString()
                this.findNavController().navigate(
                        PasswordCheckFragmentDirections.actionPasswordCheckToNotesFragment(password, false)
                )
                passwordViewModel.onNavigateToNotesFragmentComplete()
            }
        })

        // Event navigujący do NotesFragment
        passwordViewModel.checkFingerPrintEvent.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                authenticateToDecrypt()
                passwordViewModel.onCheckFingerPrintEventComplete()
            }
        })

        // Event obserwujący zmienną w której są zaszyfrowane dane
        passwordViewModel.note.observe(viewLifecycleOwner, { note ->
            if(note != null){
                println("PasswordCheckFragment: Notatka została pobrana z bazy danych i nie jest pusta!")
            }
            else{
                println("PasswordCheckFragment: Notatka jest pusta!")
            }
        })

        // Event obserwujący zmienną w której są zaszyfrowane dane
        passwordViewModel.passwordDatabase.observe(viewLifecycleOwner, { password ->
            if(password != null){
                println("PasswordCheckFragment: Hasło zostało pobrane z bazy danych i nie jest puste!")
            }
            else{
                println("PasswordCheckFragment: Hasło jest puste!")
            }
        })

        passwordViewModel.databaseIv.observe(viewLifecycleOwner, { iv ->
            if(iv != null){
                initializationVector = iv
                println("PasswordCheckFragment: IV zostało pobrane!")
            }
            else{
                println("PasswordCheckFragment: IV jest puste!")
            }
        })

        passwordViewModel.databasePasswordEncrypted.observe(viewLifecycleOwner, { text ->
            if(text != null){
                ciphertext = text
                println("PasswordCheckFragment: CipherText zostało pobrane!")
            }
            else{
                println("PasswordCheckFragment: CipherText jest puste!")
            }
        })

        return binding.root
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d("PasswordCheckFragment", "$errorCode :: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d("PasswordCheckFragment", "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d("PasswordCheckFragment", "Authentication was successful")
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

    private fun processData(cryptoObject: BiometricPrompt.CryptoObject?) {
        if (binding.viewModel!!.checkPassword(cryptographyManager.decryptData(ciphertext, cryptoObject?.cipher!!))) {
            binding.viewModel!!.passwordMatch()
        } else {
            binding.viewModel!!.passwordDontMatch()
        }
    }

    private fun authenticateToDecrypt() {
        readyToEncrypt = false
        if(initializationVector.size != 0){
            if (BiometricManager.from(application).canAuthenticate() == BiometricManager
                    .BIOMETRIC_SUCCESS) {
                val cipher = cryptographyManager.getInitializedCipherForDecryption(secretKeyName, initializationVector)
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
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

