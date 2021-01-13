package com.example.notatnik.screens.security

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.BooleanPassword
import com.example.notatnik.database.BooleanPasswordDatabaseDao
import com.example.notatnik.database.Password
import com.example.notatnik.database.PasswordDatabaseDao
import kotlinx.coroutines.*

class PasswordCreateViewModel(
        val databaseBooleanPassword: BooleanPasswordDatabaseDao,
        val databasePassword: PasswordDatabaseDao,
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
    lateinit private var password: String

    init {
        // Pobieranie zmiennej z hasłem z bazy danych
        passwordExists.addSource(databaseBooleanPassword.getLastBooleanPassword(), passwordExists::setValue)
    }

    fun passwordIsGood(password_1: String, password_2: String): Boolean{
        if (password_1.length  >= 8 && password_1 == password_2){
            password = password_1
            return true
        }
        return false
    }

    private fun updateDatabase(){
        uiScope.launch {
            withContext(Dispatchers.IO){
                val booleanPassword = BooleanPassword()
                booleanPassword.passwordBool = true
                databaseBooleanPassword.insert(booleanPassword)
            }
        }
        Log.i("PasswordCreateViewModel", "BooleanPassword was added to booleanPasswordDatabase!")
    }

    fun saveEncryptedPassword(password: ByteArray, iv: ByteArray) {
        uiScope.launch {
            withContext(Dispatchers.IO){
                val newPassword = Password()

                val password64 = Base64.encodeToString(password, Base64.DEFAULT)
                val iv64 = Base64.encodeToString(iv, Base64.DEFAULT)

                newPassword.passwordEncrypted = password64
                newPassword.passwordIv = iv64

                databasePassword.insert(newPassword)
            }
        }
        Log.i("PasswordCreateViewModel", "Password was added to passwordDatabase!")
    }

    fun getPassword(): String {
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
        updateDatabase()
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