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
import com.example.notatnik.databinding.PasswordChangeFragmentBinding

// PasswordChange, PasswordChangeViewModel, PasswordChangeViewModelFactory służą do ZMIANY HASŁA
class PasswordChange : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting binding
        val binding: PasswordChangeFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.password_change_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = PasswordDatabase.getInstance(application).passwordDatabaseDao

        val viewModelFactory = PasswordChangeViewModelFactory(dataSource, application)
        val passwordViewModel = ViewModelProvider(this, viewModelFactory).get(
            PasswordChangeViewModel::class.java
        )
        binding.viewModel = passwordViewModel
        binding.lifecycleOwner = this


        // Event nawigujący do fragmentu sprawdzania hasła po jego zmianie
        passwordViewModel.navigateToPasswordCheckFragment.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                this.findNavController().navigate(
                    PasswordChangeDirections.actionPasswordChangeToPasswordCheck()
                )
                passwordViewModel.onNavigationToPasswordCheckFragmentComplete()
            }
        })

        /*
        // Event słuzący do stworzenia nowego hasła
        passwordViewModel.newPasswordEvent.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                hideKeyboard()
                if(passwordViewModel.PasswordIsGood(binding.Password1.text.toString(), binding.Password2.text.toString() )){
                    passwordViewModel.UpdatePasswordInDatabase(binding.Password1.text.toString())
                    passwordViewModel.startNavigation()
                }
                else{
                    binding.errorTxt.text = "Passwords don't match"
                }
                passwordViewModel.onNewPasswordEventComplete()
            }
        })

         */


        // Event obserwujący zmienną w której zapisane jest hasło
        passwordViewModel.passwordDB.observe(viewLifecycleOwner, { password ->
            if(password != null){
                println("The password was successfully loaded")
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

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}