package com.example.notatnik.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BooleanPasswordDatabaseDao {
    @Insert
    fun insert(booleanPassword: BooleanPassword)

    @Update
    fun update(booleanPassword: BooleanPassword)

    @Delete
    fun delete(booleanPassword: BooleanPassword)

    @Query("SELECT * FROM booleanPassword_database ORDER BY booleanPasswordId DESC LIMIT 1")
    fun getLastBooleanPassword(): LiveData<BooleanPassword>

    @Query("DELETE FROM booleanPassword_database")
    fun clear()
}