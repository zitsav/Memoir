package com.zitsav.memoir.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zitsav.memoir.data.entry.EntryRepositoryImpl
import com.zitsav.memoir.layout.AiGenerationOverlay
import com.zitsav.memoir.layout.NotesScreen
import com.zitsav.memoir.repository.NotesRepository
import com.zitsav.memoir.viewmodel.NotesViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class CreateOrEditNoteActivity : ComponentActivity() {
    private lateinit var viewModel: NotesViewModel
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private lateinit var micPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var tempImageUri: Uri? = null

    private val speechPermission = Manifest.permission.RECORD_AUDIO
    private val cameraPermission = Manifest.permission.CAMERA

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbPass = "password" // TODO: Secure this later
        val repo = NotesRepository(EntryRepositoryImpl(this, dbPass))
        viewModel = NotesViewModel(repo)

        val entryId = intent.getLongExtra("ENTRY_ID", -1L)
        if (entryId != -1L) viewModel.loadEntry(entryId)

        initializeLaunchers()
        setUpSpeechRecognition()

        setContent {
            val isRecording by viewModel.isRecording.collectAsState()
            val micTranscript by viewModel.micTranscript.collectAsState()
            val isAiGenerating by viewModel.isAiGenerating.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                NotesScreen(
                    title = viewModel.title,
                    description = viewModel.description,
                    attachmentUri = viewModel.attachmentUri,
                    isRecording = isRecording,
                    micTranscript = micTranscript,
                    onTitleChange = { viewModel.onTitleChanged(it) },
                    onDescriptionChange = { viewModel.onDescriptionChanged(it) },
                    onSaveClick = { viewModel.save() },
                    onBack = { launchHomeActivity() },
                    onMicStart = { handleMicStart() },
                    onMicStopAndSave = {
                        speechRecognizer.stopListening()
                        viewModel.onRecordingFinished(save = true)
                    },
                    onMicStopAndCancel = {
                        speechRecognizer.stopListening()
                        viewModel.onRecordingFinished(save = false)
                    },
                    onAttachmentClick = { showAttachmentDialog() },
                    onAttachmentRemoved = { viewModel.onAttachmentChanged(null) },
                    onAiClick = { viewModel.generateAiText() }
                )
            }

            if (isAiGenerating) {
                AiGenerationOverlay(onClose = { viewModel.closeAiGeneration() })
            }
        }

        setUpListeners()
        setupOnBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleMicStart() {
        if (ContextCompat.checkSelfPermission(this, speechPermission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.onRecordingStarted()
            speechRecognizer.startListening(recognizerIntent)
        } else {
            micPermissionLauncher.launch(speechPermission)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun launchHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityFinish.collect { shouldFinish ->
                    if (shouldFinish) launchHomeActivity()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showErrorToast.collect { showToast(it) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeLaunchers() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { viewModel.onAttachmentChanged(it.toString()) }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                tempImageUri?.let { viewModel.onAttachmentChanged(it.toString()) }
            }
        }

        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                showToast("Camera permission is required to take a photo.")
            }
        }

        micPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) { handleMicStart() } else { showToast("Mic permission denied") }
        }
    }

    private fun showAttachmentDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Add Attachment")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> pickImageLauncher.launch("image/*")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    private fun launchCamera() {
        tempImageUri = createImageUri()
        takePictureLauncher.launch(tempImageUri!!)
    }

    private fun createImageUri(): Uri {
        val imageFile = File(cacheDir, "images/${System.currentTimeMillis()}.jpg")
        imageFile.parentFile?.mkdirs()
        return FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpSpeechRecognition() {
        micPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                handleMicStart()
            } else {
                showToast("Mic permission denied")
            }
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // For live transcript
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResults(results: Bundle?) {
                val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                spokenText?.let { viewModel.onRecordingFinished(save = true) }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onPartialResults(partialResults: Bundle?) {
                val partialText = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                partialText?.let { viewModel.onPartialSpeechInput(it) }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onError(error: Int) {
                showToast("Speech error: $error")
                viewModel.onRecordingFinished(save = false)
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun setupOnBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                launchHomeActivity()
            }
        } else {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    launchHomeActivity()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}