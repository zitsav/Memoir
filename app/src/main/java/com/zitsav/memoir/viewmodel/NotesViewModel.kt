package com.zitsav.memoir.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zitsav.memoir.data.entry.Entry
import com.zitsav.memoir.repository.NotesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class NotesViewModel(
    private val repo: NotesRepository
) : ViewModel() {
    private var entryId: Long? = null

    var title by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set

    var attachmentUri by mutableStateOf<String?>(null)
        private set

    fun onTitleChanged(newTitle: String) {
        title = newTitle
    }

    fun onDescriptionChanged(newDescription: String) {
        description = newDescription
    }

    private val _activityFinish = MutableStateFlow(false)
    val activityFinish: StateFlow<Boolean> = _activityFinish

    private val _showErrorToast = MutableSharedFlow<String>()
    val showErrorToast: SharedFlow<String> = _showErrorToast

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun save() {
        //todo: move all hardcoded strings to strings.xml

        if (description.trim().isEmpty()){
            _showErrorToast.tryEmit(
                "Cannot save an empty entry"
            )
            return
        }

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
        } else {
            _showErrorToast.tryEmit(
                "Something went wrong. Try again."
            )
        }
    }

    fun appendAiText(aiText: String) {
        val wrapped = "/ai{${aiText}}\n"
        //todo: check whether the last text is not the ai one
        description += wrapped
    }
}