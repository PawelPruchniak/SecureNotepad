package com.example.notatnik.screens.security

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.notatnik.R
import com.example.notatnik.database.NotesDatabase
import com.example.notatnik.databinding.PasswordChangeFragmentBinding

// PasswordChange, PasswordChangeViewModel, PasswordChangeViewModelFactory służą do ZMIANY HASŁA
class PasswordChange : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Setting binding
        val binding: PasswordChangeFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.password_change_fragment, container, false)

        val arguments = PasswordChangeArgs.fromBundle(requireArguments())

        val application = requireNotNull(this.activity).application
        val dataSource = NotesDatabase.getInstance(application).notesDatabaseDao

        val viewModelFactory = PasswordChangeViewModelFactory(dataSource, application, arguments.passwordArg, arguments.noteString)
        val passwordViewModel = ViewModelProvider(this, viewModelFactory).get(
            PasswordChangeViewModel::class.java
        )
        binding.viewModel = passwordViewModel
        binding.lifecycleOwner = this


        // Event słuzący do stworzenia nowego hasła
        passwordViewModel.newPasswordEvent.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                hideKeyboard()
                if(passwordViewModel.passwordIsGood(binding.Password1.text.toString(), binding.Password2.text.toString() )){
                    passwordViewModel.saveDataToDatabase()
                    passwordViewModel.navigateToNoteFragment()
                }
                else{
                    binding.errorTxt.text = getString(R.string.password_change_error_txt)
                }
                passwordViewModel.onNewPasswordEventComplete()
            }
        })

        passwordViewModel.navigateToNoteFragmentEvent.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue){
                val newPassword = passwordViewModel.getPassword()
                this.findNavController().navigate(PasswordChangeDirections.actionPasswordChangeToNotesFragment(newPassword, false))
                passwordViewModel.onNavigateToNoteFragmentComplete()
            }
        })

        // Event obserwujący zmienną w której są zaszyfrowane dane
        passwordViewModel.noteDatabase.observe(viewLifecycleOwner, { note ->
            if(note != null){
                println("The noteEncrypted was successfully loaded")
            }
            else{
                println("Note is null")
            }
        })
        return binding.root
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