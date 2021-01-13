package com.example.notatnik.screens.security

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.BooleanPassword
import com.example.notatnik.database.BooleanPasswordDatabaseDao
import kotlinx.coroutines.*

class PasswordCreateViewModel(
        val database: BooleanPasswordDatabaseDao,
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

    // Event aktywowany przy tworzeniu hasła
    private val _fingerprintEnrollment = MutableLiveData<Boolean>()
    val fingerprintEnrollment: LiveData<Boolean>
        get() = _fingerprintEnrollment

    // Zmienna z hasłem z bazy danych
    var passwordExists = MediatorLiveData<BooleanPassword>()
    private var password: String? = null

    init {
        // Pobieranie zmiennej z hasłem z bazy danych
        passwordExists.addSource(database.getLastBooleanPassword(), passwordExists::setValue)
    }

    fun passwordIsGood(password_1: String, password_2: String): Boolean{
        if (password_1.length  >= 8 && password_1 == password_2){
            password = password_1
            updateDatabase()
            return true
        }
        return false
    }

    private fun updateDatabase(){
        uiScope.launch {
            withContext(Dispatchers.IO){
                val passwordExists = BooleanPassword()
                passwordExists.passwordBool = true
                database.insert(passwordExists)
            }
        }
        Log.i("PasswordCreateViewModel", "PasswordExists was added to database!")
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

    fun startFingerprintEnrollment() {
        _fingerprintEnrollment.value = true
    }

    fun onStartFingerprintEnrollmentComplete() {
        _fingerprintEnrollment.value = false
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("PasswordCreateViewModel", "PasswordCreateViewModel destroyed!")
    }
}