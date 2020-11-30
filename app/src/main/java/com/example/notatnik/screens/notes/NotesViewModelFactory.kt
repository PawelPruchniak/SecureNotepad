package com.example.notatnik.screens.notes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notatnik.database.NotesDatabaseDao

class NotesViewModelFactory(
        private val dataSource: NotesDatabaseDao,
        private val application: Application,
        private val passwordArg: String
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(dataSource, application, passwordArg) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}