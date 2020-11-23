package com.example.notatnik.screens.security

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.notatnik.database.Password
import com.example.notatnik.database.PasswordDatabaseDao
import kotlinx.coroutines.*

class PasswordChangeViewModel(
    val database: PasswordDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    // zmienne do porozumiewania się z bazą danych
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Event aktywowany po poprawnej zmianie hasła
    private val _navigateToPasswordCheckFragment = MutableLiveData<Boolean>()
    val navigateToPasswordCheckFragment: LiveData<Boolean>
        get() = _navigateToPasswordCheckFragment

    // Event aktywowany po kliknięciu przycisku Save
    private val _newPasswordEvent = MutableLiveData<Boolean>()
    val newPasswordEvent: LiveData<Boolean>
        get() = _newPasswordEvent

    // Zmienna z hasłem z bazy danych
     var passwordDB = MediatorLiveData<Password>()

    init {
        // Pobieranie zmiennej z hasłem z bazy danych
        passwordDB.addSource(database.getLastPassword(), passwordDB::setValue)
    }

    // kliknięcie przycisku Save
    fun saveButtonClicked(){
        _newPasswordEvent.value = true
    }
    fun onNewPasswordEventComplete() {
        _newPasswordEvent.value = false
    }


    // Poprawna zmiana hasła
    fun startNavigation(){
        _navigateToPasswordCheckFragment.value = true
    }

    fun onNavigationToPasswordCheckFragmentComplete() {
        _navigateToPasswordCheckFragment.value = false
    }

    // Funkcja sprawdzająca czy nowe hasło które próbujemy stworzyć zgadza się z jego powtórzeniem
    fun PasswordIsGood(password_1: String, password_2: String): Boolean{
        if (password_1.length  >= 8 && password_1 == password_2){
            return true
        }
        return false
    }

    // Funkcja dodająca zmienione hasło do bazy danych
    fun UpdatePasswordInDatabase(new_password: String){
        uiScope.launch {
            withContext(Dispatchers.IO) {
                if(passwordDB.value == null){

                }
                else{
                    val newPassword = Password()
                    newPassword.passwordId = passwordDB.value?.passwordId!!
                    newPassword.passwordVar = new_password
                    database.update(newPassword)
                }
            }
        }
        Log.i("PasswordChangeViewModel", "Password was updated in database!")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("PasswordChangeViewModel", "PasswordChangeViewModel destroyed!")
    }
}