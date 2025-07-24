package com.zitsav.memoir.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zitsav.memoir.data.entry.Entry
import com.zitsav.memoir.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {
    private val _entries = mutableStateListOf<Entry>()
    val entries: List<Entry> get() = _entries

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAll()
            withContext(Dispatchers.Main) {
                _entries.clear()
                _entries.addAll(all)
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun hasEntryForToday(): Boolean {
//        val today = LocalDate.now().toEpochDay()
//        return entries.any {
//            LocalDate.ofEpochDay(it.date) == LocalDate.now()
//        }
//    }
}