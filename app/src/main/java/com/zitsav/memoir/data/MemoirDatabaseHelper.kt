package com.zitsav.memoir.data

import android.content.Context
import net.sqlcipher.database.SQLiteOpenHelper
import net.sqlcipher.database.SQLiteDatabase as EncryptedSQLiteDatabase

class MemoirDatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "memoir.db"
        const val DATABASE_VERSION = 1
        const val TABLE_ENTRIES = "entries"
    }

    override fun onCreate(db: EncryptedSQLiteDatabase) {
        val createTableSQL = """
            CREATE TABLE $TABLE_ENTRIES (
                ${DatabaseConstants.ENTRY_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${DatabaseConstants.ENTRY_TITLE} TEXT,
                ${DatabaseConstants.ENTRY_DESCRIPTION} TEXT NOT NULL,
                ${DatabaseConstants.ENTRY_DATE} INTEGER NOT NULL,
                ${DatabaseConstants.ENTRY_MOOD} INTEGER,
                ${DatabaseConstants.ENTRY_ATTACHMENT} TEXT,
                UNIQUE(date)
            );
        """.trimIndent()
        db.execSQL(createTableSQL)
        db.execSQL("CREATE INDEX index_date ON $TABLE_ENTRIES(date);")
    }

    override fun onUpgrade(db: EncryptedSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        /*
            I don't have any plans for upgrading the DB schema for now.
        */
    }
}