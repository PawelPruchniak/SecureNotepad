package com.example.notatnik.screens.security

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.Password
import com.example.notatnik.database.PasswordDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class PasswordCreateViewModel(
        val database: PasswordDatabaseDao,
        application: Application,
) : AndroidViewModel(application) {

    // zmienne do porozumiewania się z bazą danych
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Event aktywowany po pierwszym poprawnym stworzeniu hasła lub po już wczesniejszym jego stworzeniu
    private val _navigateToPasswordCheckFragment = MutableLiveData<Boolean>()
    val navigateToPasswordCheckFragment: LiveData<Boolean>
        get() = _navigateToPasswordCheckFragment

    private val _navigateToNoteFragment = MutableLiveData<Boolean>()
    val navigateToNoteFragment: LiveData<Boolean>
        get() = _navigateToNoteFragment

    // Event aktywowany przy tworzeniu hasła
    private val _newPasswordEvent = MutableLiveData<Boolean>()
    val newPasswordEvent: LiveData<Boolean>
        get() = _newPasswordEvent

    // Zmienna z hasłem z bazy danych
    var passwordExists = MediatorLiveData<Password>()
    private var password: String? = null

    init {

        // Pobieranie zmiennej z hasłem z bazy danych
        passwordExists.addSource(database.getLastPassword(), passwordExists::setValue)
    }

    fun passwordIsGood(password_1: String, password_2: String): Boolean{
        if (password_1.length  >= 8 && password_1 == password_2){
            password = password_1
            return true
        }
        return false
    }

    fun getPassword(): String? {
        return password
    }

    fun saveButtonClicked(){
        _newPasswordEvent.value = true
    }

    fun onNewPasswordEventComplete() {
        _newPasswordEvent.value = false
    }

    fun onNavigationToPasswordCheckFragmentComplete() {
        _navigateToPasswordCheckFragment.value = false
    }

    fun navigateToPasswordCheckFragment() {
        _navigateToPasswordCheckFragment.value = true
    }

    fun navigateToNoteFragment(){
        _navigateToNoteFragment.value = true
    }

    fun onNavigateToNoteFragmentComplete(){
        _navigateToNoteFragment.value = false
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("PasswordCreateViewModel", "PasswordCreateViewModel destroyed!")
    }
}