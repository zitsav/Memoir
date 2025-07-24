package com.zitsav.memoir.data.entry

import com.zitsav.memoir.data.entry.Entry

interface EntryRepository {
    fun insert(entry: Entry): Long
    fun getAll(): List<Entry>
    fun getByDate(date: Long): Entry?
    fun deleteById(id: Long): Boolean
    fun update(entry: Entry): Boolean
}