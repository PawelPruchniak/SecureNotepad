package com.example.notatnik.screens.notes

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
import com.example.notatnik.R
import com.example.notatnik.database.NotesDatabase
import com.example.notatnik.databinding.NotesFragmentBinding
import java.nio.charset.Charset

class NotesFragment : Fragment() {

    private lateinit var binding : NotesFragmentBinding
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
        binding = DataBindingUtil.inflate(inflater, R.layout.notes_fragment, container, false)

        application = requireNotNull(this.activity).application
        val dataSource = NotesDatabase.getInstance(application).notesDatabaseDao

        val arguments =  NotesFragmentArgs.fromBundle(requireArguments())

        val viewModelFactory = NotesViewModelFactory(dataSource, application, arguments.status)
        val notesViewModel = ViewModelProvider(this, viewModelFactory).get(
            NotesViewModel::class.java
        )
        binding.viewModel = notesViewModel
        binding.lifecycleOwner = this

        initializationVector = "".toByteArray()

        binding.saveButton.setOnClickListener { authenticateToEncrypt() }
        binding.decryptButton.setOnClickListener { authenticateToDecrypt() }

        cryptographyManager = CryptographyManager()
        secretKeyName = "default_key_name"
        biometricPrompt = createBiometricPrompt()
        promptInfo = createPromptInfo()


        // Event obserwujący zmienną w której są zaszyfrowane dane
        notesViewModel.noteDatabase.observe(viewLifecycleOwner, { note ->
            if(note != null){
                notesViewModel.initializeNote()
                println("The noteEncrypted was successfully loaded")
            }
            else{
                println("Note is null")
            }
        })
        return binding.root
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d("NotesFragment", "$errorCode :: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d("NotesFragment", "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d("NotesFragment", "Authentication was successful")
                processData(result.cryptoObject)
            }
        }

        //The API requires the client/Activity context for displaying the prompt view
        val biometricPrompt = BiometricPrompt(this, executor, callback)
        return biometricPrompt
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

    private fun processData(cryptoObject: BiometricPrompt.CryptoObject?) {
        val data = if (readyToEncrypt) {
            val text = binding.noteTextView.text.toString()
            val encryptedData = cryptographyManager.encryptData(text, cryptoObject?.cipher!!)
            ciphertext = encryptedData.ciphertext
            initializationVector = encryptedData.initializationVector

            String(ciphertext, Charset.forName("UTF-8"))
        } else {
            cryptographyManager.decryptData(ciphertext, cryptoObject?.cipher!!)
        }
        binding.viewModel?.saveEncryptedNote(data, initializationVector)
    }

    private fun authenticateToEncrypt() {
        println("ENTERED HERE")
        readyToEncrypt = true
        if (BiometricManager.from(application).canAuthenticate() == BiometricManager
                .BIOMETRIC_SUCCESS) {
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun authenticateToDecrypt() {
        println("ENTERED HERE de")
        readyToEncrypt = false
        if (initializationVector.size == 12) {
            println("ENTERED HERE de2")
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