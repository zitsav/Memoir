package com.zitsav.memoir.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.zitsav.memoir.R

private val aiPattern = Regex("/ai\\{([\\s\\S]*?)\\}")

@Composable
fun NotesScreen(
    title: String,
    description: String,
    attachmentUri: String?,
    isRecording: Boolean,
    micTranscript: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit,
    onMicStart: () -> Unit,
    onMicStopAndSave: () -> Unit,
    onMicStopAndCancel: () -> Unit,
    onAttachmentClick: () -> Unit,
    onAiClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Color(0xFF6200EE).copy(alpha = 0.3F)
                        )
                        .size(30.dp)
                ) {
                    Icon(painterResource(
                        R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(6.dp)
                    )
                }

                Text(
                    text = "Let's talk about your day",
                    style = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )

                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Color(0xFF6200EE).copy(alpha = 0.3F)
                        )
                        .size(30.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_send_24),
                        contentDescription = "Save",
                        modifier = Modifier
                            .padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Enter a title...")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomRichTextEditor(
                text = description,
                onTextChange = onDescriptionChange
            )

            attachmentUri?.let { uri ->
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.Gray.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Attachment: $uri")
                }
            }
        }

        if (isRecording) {
            RecordingUi(
                transcript = micTranscript,
                onCancel = onMicStopAndCancel,
                onSave = onMicStopAndSave,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            DefaultBottomBar(
                onAiClick = onAiClick,
                onMicClick = onMicStart,
                onAttachmentClick = onAttachmentClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun DefaultBottomBar(
    onAiClick: () -> Unit,
    onMicClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF6200EE).copy(alpha = 0.3F)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAiClick) {
            Icon(painterResource(id = R.drawable.baseline_auto_awesome_24), contentDescription = "AI")
        }
        IconButton(onClick = onMicClick) {
            Icon(painterResource(id = R.drawable.baseline_mic_24), contentDescription = "Start Recording")
        }
        IconButton(onClick = onAttachmentClick) {
            Icon(painterResource(id = R.drawable.baseline_attach_file_24), contentDescription = "Attachment")
        }
    }
}

@Composable
fun RecordingUi(
    transcript: String,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel, modifier = Modifier.size(48.dp)) {
                    Icon(painterResource(id = R.drawable.baseline_cancel_24), "Cancel Recording", tint = Color.Red)
                }

                val micSize by animateDpAsState(targetValue = 64.dp, label = "micSizeAnimation")
                Box(
                    modifier = Modifier
                        .size(micSize)
                        .clip(CircleShape)
                        .background(Color(0xFF6200EE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(id = R.drawable.baseline_mic_24), "Recording", tint = Color.White)
                }

                IconButton(onClick = onSave, modifier = Modifier.size(48.dp)) {
                    Icon(painterResource(id = R.drawable.baseline_check_circle_24), "Save Recording", tint = Color(0xFF00C853))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF6200EE).copy(alpha = 0.3F))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transcript.ifBlank { "Listening..." },
                    color = if (transcript.isBlank()) Color.Gray else Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

class AiBlockVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder()
        var lastIndex = 0

        for (match in aiPattern.findAll(text.text)) {
            val start = match.range.first
            val end = match.range.last + 1
            builder.append(text.text.substring(lastIndex, start))
            builder.withStyle(SpanStyle(color = Color(0xFF6200EE), fontWeight = FontWeight.Bold)) {
                append(text.text.substring(start, end))
            }
            lastIndex = end
        }

        if (lastIndex < text.text.length) {
            builder.append(text.text.substring(lastIndex))
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

@Composable
fun CustomRichTextEditor(
    text: String,
    onTextChange: (String) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = { newValue ->
            val oldMatches = aiPattern.findAll(text).map { it.groupValues[1] }.toList()
            val newMatches = aiPattern.findAll(newValue).map { it.groupValues[1] }.toList()

            var isTampered = oldMatches.size > newMatches.size
            if (!isTampered) {
                var oldIndex = 0
                for (newIndex in newMatches.indices) {
                    if (oldIndex < oldMatches.size && newMatches[newIndex] == oldMatches[oldIndex]) {
                        oldIndex++
                    }
                }
                if (oldIndex != oldMatches.size) {
                    isTampered = true
                }
            }

            if (!isTampered) {
                onTextChange(newValue)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        textStyle = TextStyle.Default.copy(fontSize = 16.sp, lineHeight = 24.sp),
        visualTransformation = AiBlockVisualTransformation(),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (text.isEmpty()) {
                    Text(
                        "Begin writing here...",
                        style = TextStyle(color = Color.Gray, fontSize = 16.sp)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    var title by remember { mutableStateOf("2025-07-25") }
    var description by remember { mutableStateOf("This is a test note.\n/ai{Generated block}") }
    var isRecording by remember { mutableStateOf(false) }
    var transcript by remember { mutableStateOf("") }

    NotesScreen(
        title = title,
        description = description,
        attachmentUri = "content://dummy/path.jpg",
        isRecording = isRecording,
        micTranscript = transcript,
        onTitleChange = { title = it },
        onDescriptionChange = { description = it },
        onSaveClick = {},
        onBack = {},
        onMicStart = {
            isRecording = true
            transcript = ""
        },
        onMicStopAndSave = { isRecording = false },
        onMicStopAndCancel = { isRecording = false },
        onAttachmentClick = {},
        onAiClick = {}
    )
}