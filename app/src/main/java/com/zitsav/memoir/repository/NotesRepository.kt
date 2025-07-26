package com.zitsav.memoir.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.zitsav.memoir.data.entry.Entry
import com.zitsav.memoir.data.entry.EntryRepository
import java.time.LocalDate

class NotesRepository(
    private val entryRepo: EntryRepository
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodayEntry(): Entry? {
        val today = LocalDate.now().toEpochDay()
        return entryRepo.getByDate(today)
    }

    fun getEntryById(id: Long): Entry? {
        return entryRepo.getById(id)
    }

    fun saveEntry(entry: Entry): Long {
        return entryRepo.insert(entry)
    }

    fun updateEntry(entry: Entry): Boolean {
        return entryRepo.update(entry)
    }
}