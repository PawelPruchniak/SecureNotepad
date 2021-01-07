package com.example.notatnik.screens.notes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notatnik.database.NotesDatabaseDao
import com.example.notatnik.screens.security.biometric.CipherSerializable

class NotesViewModelFactory(
        private val dataSource: NotesDatabaseDao,
        private val application: Application,
        private val status: Boolean,
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(dataSource, application, status) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}