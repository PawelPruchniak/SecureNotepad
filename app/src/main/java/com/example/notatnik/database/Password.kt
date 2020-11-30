package com.example.notatnik.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="password_database")
data class Password(

    @PrimaryKey(autoGenerate = true)
    var passwordId: Long = 0L,

    @ColumnInfo(name = "password")
    var passwordBool: Boolean = false

)