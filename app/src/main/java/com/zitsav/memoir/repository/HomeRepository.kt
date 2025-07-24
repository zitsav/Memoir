package com.zitsav.memoir.repository

import com.zitsav.memoir.data.entry.Entry
import com.zitsav.memoir.data.entry.EntryRepository

class HomeRepository(
    private val entryRepository: EntryRepository
) {
    fun getAll(): Collection<Entry> {
        return entryRepository.getAll()
    }
}