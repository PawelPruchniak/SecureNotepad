package com.example.notatnik.screens.security

import android.app.Activity
import android.content.Context
import android.graphics.Color.RED
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
import com.example.notatnik.databinding.PasswordCheckFragmentBinding

// PasswordCheckFragment, PasswordCheckViewModel, PasswordCheckViewModelFactory służą do SPRAWDZENIA POPRAWNOŚCI HASŁA
class PasswordCheckFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        // Setting binding
        val binding: PasswordCheckFragmentBinding =
                DataBindingUtil.inflate(inflater, R.layout.password_check_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = NotesDatabase.getInstance(application).notesDatabaseDao

        val viewModelFactory = PasswordCheckViewModelFactory(dataSource, application)
        val passwordViewModel = ViewModelProvider(this, viewModelFactory).get(
                PasswordCheckViewModel::class.java
        )
        binding.viewModel = passwordViewModel
        binding.lifecycleOwner = this


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

        // Event obserwujący zmienną w której są zaszyfrowane dane
        passwordViewModel.note.observe(viewLifecycleOwner, { note ->
            if(note != null){
                println("The note was successfully loaded")
            }
            else{
                println("Password is null")
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

