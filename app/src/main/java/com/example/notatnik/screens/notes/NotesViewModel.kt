package com.example.notatnik.screens.notes

import android.app.Application
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.Notes
import com.example.notatnik.database.NotesDatabaseDao
import com.example.notatnik.screens.security.Encryption
import com.example.notatnik.screens.security.biometric.CipherSerializable
import kotlinx.coroutines.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey

class NotesViewModel(
        private val database: NotesDatabaseDao,
        application: Application,
        private val password: String,
        status: Boolean,
        cipherSerializable: CipherSerializable
) : AndroidViewModel(application) {

    // zmienne do porozumiewania się z bazą danych
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // zmienna z notatką
    var noteDatabase = MediatorLiveData<Notes>()

    private val _noteString = MutableLiveData<String>()
    val noteString: LiveData<String>
        get() = _noteString

    // Event aktywowany po kliknięciu przycisku zapisania notatki
    private val _eventSaveButtonClicked = MutableLiveData<Boolean>()
    val eventSaveButtonClicked: LiveData<Boolean>
        get() = _eventSaveButtonClicked

    private lateinit var cipher: Cipher
    private lateinit var keyStore: KeyStore

    init {
        _noteString.value = "loading..."
        noteDatabase.addSource(database.getLastNote(), noteDatabase::setValue)

        cipher = cipherSerializable.cipher
        if(status){
            initializeNewPassword()
        }
    }

     fun initializeNote() {
        val base64Encrypted = noteDatabase.value?.noteEncrypted

        val encrypted = Base64.decode(base64Encrypted, Base64.NO_WRAP)

        val dataEncrypted = HashMap<String, ByteArray>()
        dataEncrypted["encrypted"] = encrypted

        decryptNote(dataEncrypted)
    }

     private fun initializeNewPassword() {
        val note = "Enter your notes here"
         val dataEncrypted = HashMap<String, ByteArray>()
         val cipherString = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
         val cipher2 = Cipher.getInstance(cipherString)
         keyStore = KeyStore.getInstance("AndroidKeyStore")
         keyStore.load(null)
         cipher2.init(Cipher.ENCRYPT_MODE, keyStore.getKey("default_key", null) as SecretKey)
         dataEncrypted["encrypted"]  = cipher2.doFinal(note.toByteArray(Charsets.UTF_8))
         println(dataEncrypted)
         println(dataEncrypted)
         println(dataEncrypted)
         println(dataEncrypted)

         saveDataToNoteDatabase(dataEncrypted)
    }

    private fun saveDataToNoteDatabase(map: HashMap<String, ByteArray>) {
        uiScope.launch {
            withContext(Dispatchers.IO){
                val newNote = Notes()

                val encryptedBase64String = Base64.encodeToString(map["encrypted"], Base64.NO_WRAP)
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

    fun saveNewNote(note: String) {
        val dataEncrypted = Encryption().encrypt(note.toByteArray(Charsets.UTF_8), password.toCharArray())
        saveDataToNoteDatabase(dataEncrypted)
        decryptNote(dataEncrypted)
    }

    private fun decryptNote(dataEncrypted: HashMap<String, ByteArray>) {
        val dataDecrypted = Encryption().decrypt(dataEncrypted, password.toCharArray())
        var dataDecryptedString: String? = null
        dataDecrypted?.let {
            dataDecryptedString = String(it, Charsets.UTF_8)
        }
        _noteString.value = dataDecryptedString
    }

    // Kliknięcie przycisku Save
    fun onSaveButtonClicked(){
        _eventSaveButtonClicked.value = true
    }
    fun onEventSaveButtonClickedComplete(){
        _eventSaveButtonClicked.value = false
    }
    

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("NotesViewModel", "NotesViewModel destroyed!")
    }

}

