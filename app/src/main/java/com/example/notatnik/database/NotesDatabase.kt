package com.example.notatnik.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory

@Database(entities = [Notes::class], version = 5, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {

    abstract val notesDatabaseDao: NotesDatabaseDao

    companion object {
        @Volatile
        private var NotesDatabaseSecure: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase {
            return NotesDatabaseSecure ?: synchronized(this) {
                NotesDatabaseSecure ?: buildDatabase(context).also {
                    NotesDatabaseSecure = it
                }
            }
        }

        private fun buildDatabase(context: Context):  NotesDatabase {
            val dbname = "notes_database_secure"
            val builder = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java, "${dbname}.db"
            )
            val passphrase: ByteArray = SQLiteDatabase.getBytes("P@s5P4ras3VeryL0n9".toCharArray())
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