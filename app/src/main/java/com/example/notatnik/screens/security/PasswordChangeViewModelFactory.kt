package com.example.notatnik.screens.security

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notatnik.database.NotesDatabaseDao

class PasswordChangeViewModelFactory(
        private val  database: NotesDatabaseDao,
        private val application: Application,
        private val password: String,
        private val note: String
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordChangeViewModel::class.java)) {
            return PasswordChangeViewModel(database, application, password, note) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}