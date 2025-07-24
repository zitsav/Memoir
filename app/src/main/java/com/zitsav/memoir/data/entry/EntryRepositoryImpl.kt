package com.zitsav.memoir.data.entry

import android.content.ContentValues
import android.content.Context
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
            put("title", entry.title)
            put("text", entry.text)
            put("date", entry.date)
            put("mood", entry.mood)
            put("attachment", entry.attachment)
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
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
                        date = cursor.getLong(cursor.getColumnIndexOrThrow("date")),
                        mood = cursor.getInt(cursor.getColumnIndexOrThrow("mood")),
                        attachment = cursor.getString(cursor.getColumnIndexOrThrow("attachment"))
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
            "date = ?",
            arrayOf(date.toString()),
            null, null, null
        )

        cursor.use {
            if (cursor.moveToFirst()) {
                return Entry(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow("date")),
                    mood = cursor.getInt(cursor.getColumnIndexOrThrow("mood")),
                    attachment = cursor.getString(cursor.getColumnIndexOrThrow("attachment"))
                )
            }
        }
        return null
    }

    override fun deleteById(id: Long): Boolean {
        return db.delete(
            MemoirDatabaseHelper.TABLE_ENTRIES,
            "id = ?",
            arrayOf(id.toString())
        ) > 0
    }

    override fun update(entry: Entry): Boolean {
        val values = ContentValues().apply {
            put("title", entry.title)
            put("text", entry.text)
            put("date", entry.date)
            put("mood", entry.mood)
            put("attachment", entry.attachment)
        }

        val rowsAffected = db.update(
            MemoirDatabaseHelper.TABLE_ENTRIES,
            values,
            "id = ?",
            arrayOf(entry.id.toString())
        )

        return rowsAffected > 0
    }
}
