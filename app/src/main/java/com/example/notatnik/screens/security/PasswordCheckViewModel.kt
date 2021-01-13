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
import com.example.notatnik.database.Password
import com.example.notatnik.database.PasswordDatabaseDao
import java.util.*

class PasswordCheckViewModel(
        databaseNote: NotesDatabaseDao,
        databasePassword: PasswordDatabaseDao,
        application: Application
) : AndroidViewModel(application) {

    // Event aktywowany po poprawnym wpisaniu hasła
    private val _navigateToNotesFragment = MutableLiveData<Boolean>()
    val navigateToNotesFragment: LiveData<Boolean>
        get() = _navigateToNotesFragment

    // Event aktywowany po kliknięciu przycisku Login
    private val _checkPasswordEvent = MutableLiveData<Boolean>()
    val checkPasswordEvent: LiveData<Boolean>
        get() = _checkPasswordEvent

    // Event aktywowany po kliknięciu przycisku Login
    private val _checkFingerPrintEvent = MutableLiveData<Boolean>()
    val checkFingerPrintEvent: LiveData<Boolean>
        get() = _checkFingerPrintEvent

    // Event aktywoowany po niepoprawnym wpisaniu hasła
    private val _changeLoginTxtEvent = MutableLiveData<Boolean>()
    val changeLoginTxtEvent: LiveData<Boolean>
        get() = _changeLoginTxtEvent

    private val _databaseIv = MutableLiveData<ByteArray>()
    val databaseIv: LiveData<ByteArray>
        get() = _databaseIv

    private val _databasePasswordEncrypted = MutableLiveData<ByteArray>()
    val databasePasswordEncrypted: LiveData<ByteArray>
        get() = _databasePasswordEncrypted

    private var password: String? = null


    var note = MediatorLiveData<Notes>()
    var passwordDatabase = MediatorLiveData<Password>()

    init {
        // Pobieranie zaszyfrowanej notatki
        note.addSource(databaseNote.getLastNote(), note::setValue)
        passwordDatabase.addSource(databasePassword.getLastPassword(), passwordDatabase::setValue)
    }

    // Funkcja sprawdzająca poprawność wpisanego hasła
    fun checkPassword(password: String): Boolean {

        val base64Encrypted = note.value?.noteEncrypted
        val base64Salt = note.value?.noteSalt
        val base64Iv  = note.value?.noteIv

        val encrypted = Base64.decode(base64Encrypted, Base64.NO_WRAP)
        val iv = Base64.decode(base64Iv, Base64.NO_WRAP)
        val salt = Base64.decode(base64Salt, Base64.NO_WRAP)

        val encryptedData = HashMap<String, ByteArray>()
        encryptedData["salt"] = salt
        encryptedData["iv"] = iv
        encryptedData["encrypted"] = encrypted

        val dataDecrypted = Encryption().decrypt(encryptedData, password.toCharArray())

        this.password = password
        return dataDecrypted != null
    }

    private fun initizalizePassword() {
        val base64Encrypted = passwordDatabase.value?.passwordEncrypted
        val iv = passwordDatabase.value?.passwordIv
        _databasePasswordEncrypted.value = Base64.decode(base64Encrypted, Base64.DEFAULT)
        _databaseIv.value = Base64.decode(iv, Base64.DEFAULT)
    }

    fun getPassword(): String? {
        return password
    }
    fun authorizeButtonClicked() {
        initizalizePassword()
        _checkFingerPrintEvent.value = true
    }

    fun onCheckFingerPrintEventComplete() {
        _checkFingerPrintEvent.value = false
    }

    fun loginButtonClicked() {
        _checkPasswordEvent.value = true
    }
    fun onCheckPasswordEventComplete() {
        _checkPasswordEvent.value = false
    }

    fun passwordMatch() {
        _navigateToNotesFragment.value = true
    }
    fun onNavigateToNotesFragmentComplete(){
        _navigateToNotesFragment.value = false
    }

    fun passwordDontMatch() {
        _changeLoginTxtEvent.value = true
    }
    fun onChangeLoginTxtEventComplete(){
        _changeLoginTxtEvent.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("PasswordCheckViewModel", "PasswordCheckViewModel destroyed!")
    }
}