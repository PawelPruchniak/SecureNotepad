package com.example.notatnik.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory

@Database(entities = [BooleanPassword::class], version = 5, exportSchema = false)
abstract class BooleanPasswordDatabase : RoomDatabase() {

    abstract val booleanPasswordDatabaseDao: BooleanPasswordDatabaseDao

    companion object {
        @Volatile
        private var booleanPasswordDatabaseSecure: BooleanPasswordDatabase? = null

        fun getInstance(context: Context): BooleanPasswordDatabase {
            return booleanPasswordDatabaseSecure ?: synchronized(this) {
                booleanPasswordDatabaseSecure ?: buildDatabase(context).also {
                    booleanPasswordDatabaseSecure = it
                }
            }
        }

        private fun buildDatabase(context: Context):  BooleanPasswordDatabase {
            val dbname = "booleanPassword_database_secure"
            val builder = Room.databaseBuilder(
                    context.applicationContext,
                    BooleanPasswordDatabase::class.java, "${dbname}.db"
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