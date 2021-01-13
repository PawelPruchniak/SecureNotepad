package com.example.notatnik.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="booleanPassword_database")
data class BooleanPassword(

    @PrimaryKey(autoGenerate = true)
    var booleanPasswordId: Long = 0L,

    @ColumnInfo(name = "booleanPassword")
    var passwordBool: Boolean = false

)