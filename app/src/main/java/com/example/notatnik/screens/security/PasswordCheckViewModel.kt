package com.example.notatnik.screens.security

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.Password
import com.example.notatnik.database.PasswordDatabaseDao

class PasswordCheckViewModel(
        val database: PasswordDatabaseDao,
        application: Application
) : AndroidViewModel(application) {

    // Event aktywowany po poprawnym wpisaniu hasła
    private val _navigateToNotesFragment = MutableLiveData<Boolean>()
    val navigateToNotesFragment: LiveData<Boolean>
        get() = _navigateToNotesFragment

    // Event aktywowany po kliknięciu przycisku Login
    private val _checkPasswordEvent = MutableLiveData<Boolean>()
    val checkPasswordEvent: LiveData<Boolean>
        get() = _checkPasswordEvent

    // Event aktywoowany po niepoprawnym wpisaniu hasła
    private val _changeLoginTxtEvent = MutableLiveData<Boolean>()
    val changeLoginTxtEvent: LiveData<Boolean>
        get() = _changeLoginTxtEvent

    // zmienna z hasłem z bazy danych
    var passwordDB = MediatorLiveData<Password>()

    init {
        // Pobieranie zmiennej z hasłem z bazy danych
        passwordDB.addSource(database.getLastPassword(), passwordDB::setValue)
    }

    fun LoginButtonClicked() {
        _checkPasswordEvent.value = true
    }

    // Funkcja sprawdzająca poprawność wpisanego hasła
    fun CheckPassword(password: String): Boolean {
        if(password == passwordDB.value?.passwordVar){
            return true
        }
        return false
    }

    fun PasswordMatch() {
        _navigateToNotesFragment.value = true
    }

    fun onCheckPasswordEventComplete() {
        _checkPasswordEvent.value = false
    }

    fun onNavigateToNotesFragmentComplete(){
        _navigateToNotesFragment.value = false
    }

    fun PasswordDontMatch() {
        _changeLoginTxtEvent.value = true
    }

    fun onChangeLoginTxtEventComplete(){
        _changeLoginTxtEvent.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("PasswordCheckViewModel", "PasswordCheckViewModel destroyed!")
    }
}