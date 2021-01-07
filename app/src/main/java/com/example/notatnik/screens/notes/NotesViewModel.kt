package com.example.notatnik.screens.notes

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.Notes
import com.example.notatnik.database.NotesDatabaseDao
import kotlinx.coroutines.*

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

    private val _databaseIV = MutableLiveData<ByteArray>()
    val databaseIV: LiveData<ByteArray>
        get() = _databaseIV

    private val _databaseNote = MutableLiveData<ByteArray>()
    val databaseNote: LiveData<ByteArray>
        get() = _databaseNote

    init {
        _noteString.value = "loading..."
        noteDatabase.addSource(database.getLastNote(), noteDatabase::setValue)
        if(status){
            initializeNewPassword()
        }
    }

     fun initializeNote() {
        val base64Encrypted = noteDatabase.value?.noteEncrypted
        val iv = noteDatabase.value?.noteIv
         _noteString.value = base64Encrypted
         _databaseNote.value = Base64.decode(base64Encrypted, Base64.DEFAULT)
         _databaseIV.value = Base64.decode(iv, Base64.DEFAULT)
    }

     private fun initializeNewPassword() {
        val note = "Enter your notes here"
         _noteString.value = note
         saveDataToNoteDatabase(Base64.decode(note, Base64.NO_WRAP), "".toByteArray())
    }

    private fun saveDataToNoteDatabase(note: ByteArray, iv: ByteArray) {
        uiScope.launch {
            withContext(Dispatchers.IO){
                val newNote = Notes()

                val note64 = Base64.encodeToString(note, Base64.DEFAULT)
                val iv64 = Base64.encodeToString(iv, Base64.DEFAULT)

                println("zapisuję notatkę: $note64")
                newNote.noteEncrypted = note64
                newNote.noteIv = iv64

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

    fun saveEncryptedNote(note: ByteArray, iv: ByteArray){
        saveDataToNoteDatabase(note, iv)
    }

    fun showDecryptedNote(note: String){
        _noteString.value = note
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("NotesViewModel", "NotesViewModel destroyed!")
    }

}

