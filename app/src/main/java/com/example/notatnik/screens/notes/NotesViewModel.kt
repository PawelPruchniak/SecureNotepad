package com.example.notatnik.screens.notes

import android.app.Application
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
        note.addSource(database.getLastNote(), note::setValue)
    }

    private fun initializeNewPassword() {
        val newPassword = password.toCharArray()
        val note = "Enter your notes here".toByteArray(Charsets.UTF_8)

        val dataEncrypted = Encryption().encrypt(note, newPassword)
        saveDataToNoteDatabase(dataEncrypted)
    }

    private fun saveDataToNoteDatabase(dataEncrypted: HashMap<String, ByteArray>) {
        uiScope.launch {
            withContext(Dispatchers.IO){
                val newNote = Notes

                val ba: ByteArray? = null
            }
        }
    }

    // funkcja służąca do czyszczenia bazy danych
    private fun clearDatabase() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.clear()
            }
        }
        Log.i("PasswordCreateViewModel", "Database was cleared!")
    }

    // Funkcja służąca do zapisania nowej notatki w bazie danych
    fun addNoteToDatabase(noteText: String){
        uiScope.launch {
            withContext(Dispatchers.IO) {
                if(note.value == null)
                {
                    val newNote = Notes()
                    newNote.noteVar = noteText

                    database.insert(newNote)
                }
                else{
                    val newNote = Notes()
                    newNote.noteId = note.value?.noteId!!
                    newNote.noteVar = noteText

                    database.update(newNote)
                }
            }
        }
        Log.i("NotesViewModel", "Note was added to database!")
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

