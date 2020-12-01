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
import com.example.notatnik.database.PasswordDatabase
import com.example.notatnik.databinding.PasswrdCreateFragmentBinding

// PasswordCreate, PasswordCreateViewModel, PasswordCreateViewModelFactory służą do STWORZENIA PIERWSZEGO HASŁA
class PasswordCreate : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        // Setting binding
        val binding: PasswrdCreateFragmentBinding =
                DataBindingUtil.inflate(inflater, R.layout.passwrd_create_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = PasswordDatabase.getInstance(application).passwordDatabaseDao

        val viewModelFactory = PasswordCreateViewModelFactory(dataSource, application)
        val securityViewModel = ViewModelProvider(this, viewModelFactory).get(
                PasswordCreateViewModel::class.java
        )
        binding.viewModel = securityViewModel
        binding.lifecycleOwner = this

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
                val createdPassword: String = securityViewModel.getPassword().toString()
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
                    securityViewModel.navigateToNoteFragment()
                }
                else{
                    binding.errorTxt.text = "Passwords don't match"
                }
                securityViewModel.onNewPasswordEventComplete()
            }
        })

        // Event sprawdzający czy hasło zostało już stworzone, jeżeeli tak to naviguje
        securityViewModel.passwordExists.observe(viewLifecycleOwner, { password ->
            if(password != null && password.passwordBool){
                securityViewModel.navigateToPasswordCheckFragment()
            }
            else{
                println("Password jest null!")
            }
        })

        return binding.root
    }


    // FUNKCJE DO UKRYCIA KLAWIATURY PO KLIKNIECIU PRZYCISKU
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}