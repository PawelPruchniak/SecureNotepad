package com.example.notatnik.screens.security

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notatnik.database.NotesDatabaseDao
import com.example.notatnik.database.PasswordDatabaseDao

class PasswordCheckViewModelFactory(
        private val dataSource1: NotesDatabaseDao,
        private val dataSource2: PasswordDatabaseDao,
        private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordCheckViewModel::class.java)) {
            return PasswordCheckViewModel(dataSource1, dataSource2, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}