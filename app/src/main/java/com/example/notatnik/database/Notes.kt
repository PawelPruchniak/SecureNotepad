package com.example.notatnik.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="notes_database")
data class Notes(

    @PrimaryKey(autoGenerate = true)
    var noteId: Long = 0L,

    @ColumnInfo(name = "noteSalt")
    var noteSalt: ByteArray?,

    @ColumnInfo(name = "noteIv")
    var noteIv: ByteArray,

    @ColumnInfo(name = "noteEncrypted")
    var noteEncrypted: ByteArray,


    )