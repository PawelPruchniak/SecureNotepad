package com.example.notatnik.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="notes_database")
data class Notes(

    @PrimaryKey(autoGenerate = true)
    var noteId: Long = 0L,

    @ColumnInfo(name = "noteSalt")
    var noteSalt: String = "",

    @ColumnInfo(name = "noteIv")
    var noteIv: String = "",

    @ColumnInfo(name = "noteEncrypted")
    var noteEncrypted: String = "",
    )