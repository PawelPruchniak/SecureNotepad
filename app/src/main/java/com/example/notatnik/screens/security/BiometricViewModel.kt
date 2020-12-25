package com.example.notatnik.screens.security

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import com.example.notatnik.database.Password
import com.example.notatnik.database.PasswordDatabaseDao
import kotlinx.coroutines.*

class BiometricViewModel(
        val database: PasswordDatabaseDao,
        application: Application,
) : AndroidViewModel(application) {

    // zmienne do porozumiewania się z bazą danych
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Zmienna z Boolem który oznaczy czy hasło zostało już stworzone czy nie.
    var passwordExists = MediatorLiveData<Password>()


    init {
        // Pobieranie zmiennej z boolem z bazy danych
        passwordExists.addSource(database.getLastPassword(), passwordExists::setValue)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("BiometricViewModel", "BiometricViewModel destroyed!")
    }


    fun passwordCreated() {
        uiScope.launch {
            withContext(Dispatchers.IO){
                val passwordExists = Password()
                passwordExists.passwordBool = true
                database.insert(passwordExists)
            }
        }
        Log.i("BiometricViewModel", "PasswordExists was added to database!")
    }
}