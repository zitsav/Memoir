package com.zitsav.memoir.ui.layout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zitsav.memoir.R
import com.zitsav.memoir.data.entry.Entry
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Utsav",
    notes: List<Entry>,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Entry) -> Unit,
) {
    val today = LocalDate.now()
    val showFab = notes.none { LocalDate.ofEpochDay(it.date) == today }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome, $userName",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DaySelectorRow(today = today)

        Spacer(modifier = Modifier.height(24.dp))

        if (notes.isEmpty()) {
            EmptyNotesPlaceholder()
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(notes){
                    NoteItem(
                        entry = it,
                        onNoteClick = { onNoteClick(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showFab) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            FloatingActionButton(
                onClick = onAddNoteClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text("How was your day today?",
                    modifier = Modifier
                        .padding(16.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DaySelectorRow(today: LocalDate) {
    val days = (-3..3).map { today.plusDays(it.toLong()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEach { date ->
            val isToday = date == today
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek.name.take(3),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isToday) MaterialTheme.colorScheme.primary else Color.LightGray)
                        .padding(horizontal = 10.dp)
                )
            }
        }
    }
}

private val aiPattern = Regex("/ai\\{([\\s\\S]*?)\\}")

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteItem(
    entry: Entry,
    onNoteClick: () -> Unit
) {
    val maxLines = 7

    val filteredText = entry.text.replace(aiPattern, "").trim()

    // FIX: Use LocalDate.ofEpochDay to correctly interpret the date
    val dateStr = LocalDate.ofEpochDay(entry.date).toString()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable(onClick = onNoteClick)
    ) {
        if (!entry.title.isNullOrBlank()) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = filteredText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = dateStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyNotesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                "Looks like your notebook is empty. What’s on your mind today?",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Image(
                painter = painterResource(R.drawable.home_empty_state),
                contentDescription = "Empty",
                modifier = Modifier.size(300.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleNotes = listOf(
        Entry(
            id = 1,
            title = "First Note",
            text = "Lorem ipsum dolor sit amet consectetur adipiscing elit. Quisque faucibus ex sapien vitae pellentesque sem placerat. In id cursus mi pretium tellus duis convallis. Tempus leo eu aenean sed diam urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum egestas. Iaculis massa nisl malesuada lacinia integer nunc posuere. Ut hendrerit semper vel class aptent taciti sociosqu. Ad litora torquent per conubia nostra inceptos himenaeos.\n",
            date = LocalDate.now().toEpochDay(),
            mood = null,
            attachment = null
        )
    )

    HomeScreen(
        userName = "Utsav",
        notes = sampleNotes,
        onAddNoteClick = {},
        onNoteClick = {}
    )
}