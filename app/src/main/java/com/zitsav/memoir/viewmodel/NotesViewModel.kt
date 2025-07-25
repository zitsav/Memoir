package com.zitsav.memoir.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zitsav.memoir.data.entry.Entry
import com.zitsav.memoir.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class NotesViewModel(
    private val repo: NotesRepository
) : ViewModel() {
    private var entryId: Long? = null
    var title by mutableStateOf("${LocalDate.now()}")
    var description by mutableStateOf("")
    var attachmentUri by mutableStateOf<String?>(null)

    private val _activityFinish = MutableStateFlow(false)
    val activityFinish: StateFlow<Boolean> = _activityFinish

    fun loadEntry(id: Long) {
        val entry = repo.getEntryById(id)
        if (entry != null) {
            entryId = entry.id
            if (entry.title != null){
                title = entry.title
            }
            description = entry.text
            attachmentUri = entry.attachment
        }
    }

    fun save() {
        //todo: form validation

        val entry = Entry(
            id = entryId ?: 0,
            date = LocalDate.now().toEpochDay(),
            title = title,
            text = description,
            attachment = attachmentUri,
            mood = null
        )
        val success = if (entryId == 0L) {
            (repo.saveEntry(entry) != -1L)
        } else {
            repo.updateEntry(entry)
        }
        if (success) {
            _activityFinish.value = true
        }
    }

    fun appendAiText(aiText: String) {
        val wrapped = "/ai{${aiText}}\n"
        if (!description.contains("/ai{")) {
            description += wrapped
        }
    }
}