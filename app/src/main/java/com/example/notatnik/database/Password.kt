package com.example.notatnik.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="password_database")
data class Password(

        @PrimaryKey(autoGenerate = true)
        var passwordId: Long = 0L,

        @ColumnInfo(name = "passwordIv")
        var passwordIv: String? = null,

        @ColumnInfo(name = "passwordEncrypted")
        var passwordEncrypted: String? = null
)