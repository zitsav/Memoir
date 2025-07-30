package com.zitsav.memoir.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zitsav.memoir.data.entry.Entry
import com.zitsav.memoir.network.RetrofitInstance
import com.zitsav.memoir.network.request.Content
import com.zitsav.memoir.network.request.GeminiRequest
import com.zitsav.memoir.network.request.Part
import com.zitsav.memoir.repository.NotesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class NotesViewModel(
    private val repo: NotesRepository
) : ViewModel() {
    private var entryId: Long = 0L

    var title by mutableStateOf("")
        private set

    var description by mutableStateOf(TextFieldValue(""))
        private set

    var attachmentUri by mutableStateOf<String?>(null)
        private set

    fun onTitleChanged(newTitle: String) {
        title = newTitle
    }

    fun onDescriptionChanged(newValue: TextFieldValue) {
        description = newValue
    }

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating = _isAiGenerating.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _micTranscript = MutableStateFlow("")
    val micTranscript = _micTranscript.asStateFlow()

    private val _activityFinish = MutableStateFlow(false)
    val activityFinish = _activityFinish.asStateFlow()

    private val _showErrorToast = MutableSharedFlow<String>()
    val showErrorToast = _showErrorToast.asSharedFlow()

    fun generateAiText() {
        if (_isAiGenerating.value) return

        val currentText = description.text
        if (currentText.isBlank()) {
            viewModelScope.launch { _showErrorToast.emit("Cannot generate from an empty description.") }
            return
        }

        if (currentText.trim().endsWith("}")) {
            viewModelScope.launch { _showErrorToast.emit("Please add new text after the last AI block.") }
            return
        }

        viewModelScope.launch {
            _isAiGenerating.value = true
            try {
                val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = currentText)))))
                val response = RetrofitInstance.api.generateContent(request)

                if (response.isSuccessful) {
                    val generatedText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                    if (!generatedText.isNullOrBlank()) {
                        if (generatedText.startsWith("[") && generatedText.endsWith("]")) {
                            val errorMessage = generatedText.substring(1, generatedText.length - 1)
                            _showErrorToast.emit(errorMessage)
                        } else {
                            val newText = "$currentText\n/ai{$generatedText}\n"
                            description = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                        }
                    } else {
                        _showErrorToast.emit("Received an empty response from AI.")
                    }
                } else {
                    _showErrorToast.emit("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _showErrorToast.emit("Failed to connect: ${e.message}")
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun closeAiGeneration() {
        _isAiGenerating.value = false
    }

    fun onRecordingStarted() {
        _isRecording.value = true
        _micTranscript.value = ""
    }

    fun onRecordingFinished(save: Boolean) {
        _isRecording.value = false
        if (save) {
            onSpeechInput(_micTranscript.value)
        }
        _micTranscript.value = ""
    }

    fun onPartialSpeechInput(partialText: String) {
        if (_isRecording.value) {
            _micTranscript.value = partialText
        }
    }

    private fun onSpeechInput(spokenText: String) {
        if (spokenText.isBlank()) return
        val currentText = description.text
        val newText = if (currentText.isNotBlank()) {
            "$currentText $spokenText"
        } else {
            spokenText
        }
        description = TextFieldValue(
            text = newText,
            selection = TextRange(newText.length)
        )
    }

    fun loadEntry(id: Long) {
        val entry = repo.getEntryById(id)
        if (entry != null) {
            entryId = entry.id
            title = entry.title ?: ""
            description = TextFieldValue(
                text = entry.text,
                selection = TextRange(entry.text.length)
            )
            attachmentUri = entry.attachment
        }
    }

    fun onAttachmentChanged(uri: String?) {
        attachmentUri = uri
    }

    fun save() {
        if (description.text.isBlank()){
            viewModelScope.launch { _showErrorToast.emit("Cannot save an empty entry") }
            return
        }

        val entry = Entry(
            id = entryId,
            date = LocalDate.now().toEpochDay(),
            title = title,
            text = description.text,
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
            viewModelScope.launch { _showErrorToast.emit("Something went wrong. Try again.") }
        }
    }
}