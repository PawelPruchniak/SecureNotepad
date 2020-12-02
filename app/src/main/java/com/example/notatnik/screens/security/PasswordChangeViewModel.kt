package com.example.notatnik.screens.security

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
import java.util.*

class PasswordChangeViewModel(
        val database: NotesDatabaseDao,
        application: Application,
        private var password: String,
        private val note: String
) : AndroidViewModel(application) {

    // zmienne do porozumiewania się z bazą danych
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // zmienna z notatką
    var noteDatabase = MediatorLiveData<Notes>()

    // Event aktywowany po kliknięciu przycisku Save
    private val _newPasswordEvent = MutableLiveData<Boolean>()
    val newPasswordEvent: LiveData<Boolean>
        get() = _newPasswordEvent

    private val _navigateToNoteFragmentEvent = MutableLiveData<Boolean>()
    val navigateToNoteFragmentEvent: LiveData<Boolean>
        get() = _navigateToNoteFragmentEvent

    init {
        noteDatabase.addSource(database.getLastNote(), noteDatabase::setValue)
    }

    fun getPassword(): String {
        return password
    }

    fun saveDataToDatabase() {
        val dataEncrypted = Encryption().encrypt(note.toByteArray(Charsets.UTF_8), password.toCharArray())
        saveDataToNoteDatabase(dataEncrypted)
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

    // kliknięcie przycisku Save
    fun saveButtonClicked(){
        _newPasswordEvent.value = true
    }
    fun onNewPasswordEventComplete() {
        _newPasswordEvent.value = false
    }

    fun navigateToNoteFragment(){
        _navigateToNoteFragmentEvent.value = true
    }

    fun onNavigateToNoteFragmentComplete(){
        _navigateToNoteFragmentEvent.value = false
    }

    // Funkcja sprawdzająca czy nowe hasło które próbujemy stworzyć zgadza się z jego powtórzeniem
    fun passwordIsGood(password_1: String, password_2: String): Boolean{
        if (password_1.length  >= 8 && password_1 == password_2 && password_1 != password){
            password = password_1
            return true
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("PasswordChangeViewModel", "PasswordChangeViewModel destroyed!")
    }

}