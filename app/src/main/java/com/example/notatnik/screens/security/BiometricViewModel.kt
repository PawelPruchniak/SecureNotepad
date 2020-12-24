package com.example.notatnik.screens.security

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import com.example.notatnik.database.Password
import com.example.notatnik.database.PasswordDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

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
}