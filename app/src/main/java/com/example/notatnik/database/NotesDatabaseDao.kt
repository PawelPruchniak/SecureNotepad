package com.example.notatnik.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotesDatabaseDao {
    @Insert
    fun insert(note: Notes)

    @Update
    fun update(note: Notes)

    @Delete
    fun delete(note: Notes)

    @Query("SELECT * FROM notes_database ORDER BY noteId DESC LIMIT 1")
    fun getLastNote(): LiveData<Notes>

    @Query("DELETE FROM notes_database")
    fun clear()
}