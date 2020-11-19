package com.example.notatnik.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PasswordDatabaseDao {
    @Insert
    fun insert(password: Password)

    @Update
    fun update(password: Password)

    @Delete
    fun delete(password: Password)

    @Query("SELECT * FROM password_database ORDER BY passwordId DESC LIMIT 1")
    fun getLastPassword(): LiveData<Password>

    @Query("DELETE FROM password_database")
    fun clear()
}