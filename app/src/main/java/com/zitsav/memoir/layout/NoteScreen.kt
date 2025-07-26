package com.zitsav.memoir.layout

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.zitsav.memoir.R
import java.util.regex.PatternSyntaxException

private val aiPattern = Regex("/ai\\{([\\s\\S]*?)\\}")

@Composable
fun NotesScreen(
    title: String,
    description: String,
    attachmentUri: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit,
    onMicStart: () -> Unit,
    onMicStop: () -> Unit,
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF6200EE).copy(alpha = 0.3F)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { /* TODO: AI click */ }) {
                Icon(painterResource(id = R.drawable.baseline_auto_awesome_24), contentDescription = "AI")
            }
            IconButton(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onMicStart()
                                tryAwaitRelease()
                                onMicStop()
                            }
                        )
                    },
                onClick = {}
            ) {
                Icon(painterResource(id = R.drawable.baseline_mic_24), contentDescription = "Mic")
            }
            IconButton(onClick = { /* TODO: Attachment click */ }) {
                Icon(painterResource(id = R.drawable.baseline_attach_file_24), contentDescription = "Attachment")
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
            try {
                val oldBlockCount = aiPattern.findAll(text).count()
                val newBlockCount = aiPattern.findAll(newValue).count()
                if (newBlockCount <= oldBlockCount) {
                    onTextChange(newValue)
                }
            } catch (e: PatternSyntaxException) {
                Log.e("RegexError", "Invalid regex pattern", e)
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

    NotesScreen(
        title = title,
        description = description,
        attachmentUri = "content://dummy/path.jpg",
        onTitleChange = { title = it },
        onDescriptionChange = { description = it },
        onSaveClick = {},
        onBack = {},
        onMicStart = {},
        onMicStop = {},
    )
}