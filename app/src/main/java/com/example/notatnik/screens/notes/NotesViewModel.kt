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
import com.example.notatnik.screens.security.Encryption
import kotlinx.coroutines.*
import java.util.*

class NotesViewModel(
        val database: NotesDatabaseDao,
        application: Application,
        private val password: String,
        private val newPasswordBoolean: Boolean
) : AndroidViewModel(application) {

    // zmienne do porozumiewania się z bazą danych
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // zmienna z notatką
    var note = MediatorLiveData<Notes>()
    var noteString: String = ""

    // Event aktywowany po kliknięciu przycisku zapisania notatki
    private val _eventSaveButtonClicked = MutableLiveData<Boolean>()
    val eventSaveButtonClicked: LiveData<Boolean>
        get() = _eventSaveButtonClicked

    // Event aktywowany po kliknięciu przycisku zmiany hasła
    private val _eventNavigateToPasswordChangeFragment = MutableLiveData<Boolean>()
    val eventNavigateToPasswordChangeFragment: LiveData<Boolean>
        get() = _eventNavigateToPasswordChangeFragment


    init {
        //clearDatabase()
        if(newPasswordBoolean){
            initializeNewPassword()
        }
        // Pobranie notatki z bazy danych
        // note.addSource(database.getLastNote(), note::setValue)
    }

    private fun initializeNewPassword() {
        val note = "Enter your notes here".toByteArray(Charsets.UTF_8)

        val dataEncrypted = Encryption().encrypt(note, password.toCharArray())
        saveDataToNoteDatabase(dataEncrypted)
        decryptNote(dataEncrypted)
    }

    private fun saveDataToNoteDatabase(map: HashMap<String, ByteArray>) {
        uiScope.launch {
            withContext(Dispatchers.IO){
                val newNote = Notes()

                val encryptedBase64String = Base64.encodeToString(map["encrypted"], Base64.NO_WRAP)
                val saltBase64String = Base64.encodeToString(map["salt"], Base64.NO_WRAP)
                val ivBase64String = Base64.encodeToString(map["iv"], Base64.NO_WRAP)

                newNote.noteSalt = saltBase64String
                newNote.noteIv = ivBase64String
                newNote.noteEncrypted = encryptedBase64String

                database.insert(newNote)
            }
        }
        Log.i("NotesViewModel", "Note was added to database!")
    }

    private fun decryptNote(dataEncrypted: HashMap<String, ByteArray>) {
        val dataDecrypted = Encryption().decrypt(dataEncrypted, password.toCharArray())
        var dataDecryptedString: String? = null
        dataDecrypted?.let {
            dataDecryptedString = String(it, Charsets.UTF_8)
        }
        noteString = dataDecryptedString.toString()
    }

    // Kliknięcie przycisku Save
    fun onSaveButtonClicked(){
        _eventSaveButtonClicked.value = true
    }
    fun onEventSaveButtonClickedComplete(){
        _eventSaveButtonClicked.value = false
    }

    // Kliknięcie przycisku Change your Password
    fun onChangePasswordButtonClicked(){
        _eventNavigateToPasswordChangeFragment.value = true
    }
    fun onEventNavigateTopasswordChangeFragmentComplete() {
        _eventNavigateToPasswordChangeFragment.value = false
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("NotesViewModel", "NotesViewModel destroyed!")
    }
}

