package com.example.notatnik.screens.notes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.Notes
import com.example.notatnik.database.NotesDatabaseDao
import kotlinx.coroutines.*
import java.security.KeyStore
import javax.crypto.Cipher

class NotesViewModel(
        private val database: NotesDatabaseDao,
        application: Application,
        status: Boolean
) : AndroidViewModel(application) {

    // zmienne do porozumiewania się z bazą danych
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // zmienna z notatką
    var noteDatabase = MediatorLiveData<Notes>()

    private val _noteString = MutableLiveData<String>()
    val noteString: LiveData<String>
        get() = _noteString


    private lateinit var cipher: Cipher
    private lateinit var keyStore: KeyStore

    init {
        _noteString.value = "loading..."
        noteDatabase.addSource(database.getLastNote(), noteDatabase::setValue)

        if(status){
            initializeNewPassword()
        }
    }

     fun initializeNote() {
        val base64Encrypted = noteDatabase.value?.noteEncrypted
         _noteString.value = base64Encrypted
    }

     private fun initializeNewPassword() {
        val note = "Enter your notes here"
         _noteString.value = note
         saveDataToNoteDatabase(note, "".toByteArray())
    }

    private fun saveDataToNoteDatabase(note: String, iv: ByteArray) {
        uiScope.launch {
            withContext(Dispatchers.IO){
                val newNote = Notes()

                newNote.noteEncrypted = note
                if(iv.size == 12){
                    newNote.noteIv = iv
                }

                if (noteDatabase.value == null){
                    database.insert(newNote)
                }
                else{
                    newNote.noteId = noteDatabase.value?.noteId!!
                    database.update(newNote)
                }
            }
        }
        Log.i("NotesViewModel", "Note was added to database!")
    }

    fun saveEncryptedNote(note: String, iv: ByteArray){
        saveDataToNoteDatabase(note)
        println("Note added to be saved in database!")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("NotesViewModel", "NotesViewModel destroyed!")
    }

}

