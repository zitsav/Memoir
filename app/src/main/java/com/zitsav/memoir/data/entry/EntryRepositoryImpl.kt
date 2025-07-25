package com.zitsav.memoir.data.entry

import android.content.ContentValues
import android.content.Context
import com.zitsav.memoir.data.DatabaseConstants
import com.zitsav.memoir.data.MemoirDatabaseHelper
import net.sqlcipher.database.SQLiteDatabase

class EntryRepositoryImpl(
    context: Context,
    private val passphrase: String
) : EntryRepository {

    private val dbHelper = MemoirDatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.getWritableDatabase(passphrase)

    override fun insert(entry: Entry): Long {
        val values = ContentValues().apply {
            put(DatabaseConstants.ENTRY_TITLE, entry.title)
            put(DatabaseConstants.ENTRY_DESCRIPTION, entry.text)
            put(DatabaseConstants.ENTRY_DATE, entry.date)
            put(DatabaseConstants.ENTRY_MOOD, entry.mood)
            put(DatabaseConstants.ENTRY_ATTACHMENT, entry.attachment)
        }
        return db.insert(MemoirDatabaseHelper.TABLE_ENTRIES, null, values)
    }

    override fun getAll(): List<Entry> {
        val cursor = db.query(
            MemoirDatabaseHelper.TABLE_ENTRIES,
            null, null, null, null, null, "date DESC"
        )

        val entries = mutableListOf<Entry>()
        cursor.use {
            while (cursor.moveToNext()) {
                entries.add(
                    Entry(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_TITLE)),
                        text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_DESCRIPTION)),
                        date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_DATE)),
                        mood = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_MOOD)),
                        attachment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_ATTACHMENT))
                    )
                )
            }
        }
        return entries
    }

    override fun getByDate(date: Long): Entry? {
        val cursor = db.query(
            MemoirDatabaseHelper.TABLE_ENTRIES,
            null,
            "${DatabaseConstants.ENTRY_DATE} = ?",
            arrayOf(date.toString()),
            null, null, null
        )

        cursor.use {
            if (cursor.moveToFirst()) {
                return Entry(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_TITLE)),
                    text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_DESCRIPTION)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_DATE)),
                    mood = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_MOOD)),
                    attachment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_ATTACHMENT))
                )
            }
        }
        return null
    }

    override fun getById(id: Long): Entry? {
        val cursor = db.query(
            MemoirDatabaseHelper.TABLE_ENTRIES,
            null,
            "${DatabaseConstants.ENTRY_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        cursor.use {
            if (cursor.moveToFirst()) {
                return Entry(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_TITLE)),
                    text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_DESCRIPTION)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_DATE)),
                    mood = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_MOOD)),
                    attachment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENTRY_ATTACHMENT))
                )
            }
        }
        return null
    }

    override fun deleteById(id: Long): Boolean {
        return db.delete(
            MemoirDatabaseHelper.TABLE_ENTRIES,
            "${DatabaseConstants.ENTRY_ID} = ?",
            arrayOf(id.toString())
        ) > 0
    }

    override fun update(entry: Entry): Boolean {
        val values = ContentValues().apply {
            put(DatabaseConstants.ENTRY_TITLE, entry.title)
            put(DatabaseConstants.ENTRY_DESCRIPTION, entry.text)
            put(DatabaseConstants.ENTRY_DATE, entry.date)
            put(DatabaseConstants.ENTRY_MOOD, entry.mood)
            put(DatabaseConstants.ENTRY_ATTACHMENT, entry.attachment)
        }

        val rowsAffected = db.update(
            MemoirDatabaseHelper.TABLE_ENTRIES,
            values,
            "${DatabaseConstants.ENTRY_ID} = ?",
            arrayOf(entry.id.toString())
        )

        return rowsAffected > 0
    }
}
