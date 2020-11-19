package com.example.notatnik.screens.security

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.Password
import com.example.notatnik.database.PasswordDatabaseDao
import kotlinx.coroutines.*

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

    // Event aktywowany przy tworzeniu hasła
    private val _newPasswordEvent = MutableLiveData<Boolean>()
    val newPasswordEvent: LiveData<Boolean>
        get() = _newPasswordEvent

    // Zmienna z hasłem z bazy danych
    var password = MediatorLiveData<Password>()

    init {
        //clearDatabase()

        // Pobieranie zmiennej z hasłem z bazy danych
        password.addSource(database.getLastPassword(), password::setValue)
    }

    // Funkcja służąca do czyszczenia bazy danych
    private fun clearDatabase() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.clear()
            }
        }
        Log.i("PasswordCreateViewModel", "Database was cleared!")
    }

    fun saveButtonClicked(){
        _newPasswordEvent.value = true
    }

    fun onNavigationToPasswordCheckFragmentComplete() {
        _navigateToPasswordCheckFragment.value = false
    }

    fun onNewPasswordEventComplete() {
        _newPasswordEvent.value = false
    }

    fun PasswordIsGood(password_1: String, password_2: String): Boolean{
        if (password_1.length  >= 8 && password_1 == password_2){
            return true
        }
        return false
    }

    // Funkcja dodająca hasło do bazy danych
    fun addPasswordToDatabase(password: String){
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val password_db = Password()
                password_db.passwordVar = password
                database.insert(password_db)
            }
        }
        Log.i("PasswordCreateViewModel", "Password was added to database!")
    }

    fun startNavigation(){
        _navigateToPasswordCheckFragment.value = true
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("PasswordCreateViewModel", "PasswordCreateViewModel destroyed!")
    }
}