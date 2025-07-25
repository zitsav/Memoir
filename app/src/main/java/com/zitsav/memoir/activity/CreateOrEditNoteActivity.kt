package com.zitsav.memoir.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zitsav.memoir.data.entry.EntryRepositoryImpl
import com.zitsav.memoir.layout.NotesScreen
import com.zitsav.memoir.repository.NotesRepository
import com.zitsav.memoir.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

class CreateOrEditNoteActivity : ComponentActivity() {
    private lateinit var viewModel: NotesViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //todo: change it and add it to an env later on
        val dbPass = "password"

        val repo = NotesRepository(EntryRepositoryImpl(this, dbPass))
        viewModel = NotesViewModel(repo)

        val entryId = intent.getLongExtra("ENTRY_ID", -1L)
        if (entryId != -1L) {
            viewModel.loadEntry(entryId)
        }

        setUpListeners()

        setContent {
            NotesScreen(
                title = viewModel.title,
                description = viewModel.description,
                attachmentUri = viewModel.attachmentUri,
                onTitleChange = {
                    viewModel.onTitleChanged(it)
                },
                onDescriptionChange = {
                    viewModel.onDescriptionChanged(it)
                },
                onSaveClick = {
                    viewModel.save()
                },
                onBack = {
                    finish()
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityFinish.collect { shouldFinish ->
                    if (shouldFinish) {
                        finish()
                    }
                }
            }
        }
    }
}