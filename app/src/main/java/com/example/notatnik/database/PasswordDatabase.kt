package com.example.notatnik.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory

@Database(entities = [Password::class], version = 4, exportSchema = false)
abstract class PasswordDatabase : RoomDatabase() {

    abstract val passwordDatabaseDao: PasswordDatabaseDao

    companion object {
        @Volatile
        private var passwordDatabaseSecure: PasswordDatabase? = null

        fun getInstance(context: Context): PasswordDatabase {
            return passwordDatabaseSecure ?: synchronized(this) {
                passwordDatabaseSecure ?: buildDatabase(context).also {
                    passwordDatabaseSecure = it
                }
            }
        }

        private fun buildDatabase(context: Context):  PasswordDatabase {
            val dbname = "password_database_secure"
            val builder = Room.databaseBuilder(
                    context.applicationContext,
                    PasswordDatabase::class.java, "${dbname}.db"
            )
            val passphrase: ByteArray = SQLiteDatabase.getBytes("P@s5P4ras3VeryL0n9forPaaS1g1wo7rd".toCharArray())
            val factory = SupportFactory(passphrase, object : SQLiteDatabaseHook {
                override fun preKey(database: SQLiteDatabase?) = Unit
                override fun postKey(database: SQLiteDatabase?) {
                    database?.rawExecSQL("PRAGMA cipher_memory_security = ON")
                }
            })
            builder.openHelperFactory(factory)
            return builder.build()
        }
    }
}