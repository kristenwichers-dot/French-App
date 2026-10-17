package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChapterEntity
import com.example.data.CourseDocumentEntity
import com.example.viewmodel.FrenchCompanionViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: FrenchCompanionViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.documents.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val selectedChapterId by viewModel.selectedChapterId.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedChapterFilter by remember { mutableStateOf("ALL") }
    var viewingDoc by remember { mutableStateOf<CourseDocumentEntity?>(null) }

    val quickSearchTopics = listOf(
        "Possessifs",
        "Futur proche",
        "Verbe faire",
        "Rendre visite à",
        "Météo & Saisons",
        "Famille",
        "Haïti & Sénégal",
        "Faux amis",
        "Verbe être",
        "Verbe avoir",
        "Adjectifs BANGS",
        "Verbes en -ir"
    )

    // Filter documents by query and chapter
    val filteredDocs = documents.filter { doc ->
        val matchesQuery = searchQuery.isBlank() ||
                doc.title.contains(searchQuery, ignoreCase = true) ||
                doc.contentText.contains(searchQuery, ignoreCase = true) ||
                doc.pageOrTimestamp.contains(searchQuery, ignoreCase = true) ||
                doc.professorGuidance.contains(searchQuery, ignoreCase = true)
        val matchesChapter = selectedChapterFilter == "ALL" || doc.chapterId == selectedChapterFilter
        matchesQuery && matchesChapter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar (testTag preserved for UI tests)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search textbook (e.g. 'possessifs', 'futur proche', 'faire')...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("doc_search_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Search Suggested Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickSearchTopics) { topic ->
                val isSelected = searchQuery.equals(topic, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            searchQuery = if (isSelected) "" else topic
                        }
                ) {
                    Text(
                        text = topic,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chapter Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = if (selectedChapterFilter == "ALL") 0 else (chapters.indexOfFirst { it.id == selectedChapterFilter } + 1).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedChapterFilter == "ALL",
                onClick = { selectedChapterFilter = "ALL" },
                text = {
                    Text(
                        text = "All Chapters (${documents.size})",
                        fontSize = 11.sp,
                        fontWeight = if (selectedChapterFilter == "ALL") FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            chapters.forEach { ch ->
                val count = documents.count { it.chapterId == ch.id }
                Tab(
                    selected = selectedChapterFilter == ch.id,
                    onClick = { selectedChapterFilter = ch.id },
                    text = {
                        Text(
                            text = "Ch. ${ch.number} ($count)",
                            fontSize = 11.sp,
                            fontWeight = if (selectedChapterFilter == ch.id) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search status & result summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Results for \"$searchQuery\" (${filteredDocs.size})"
                } else {
                    val currentChName = chapters.find { it.id == selectedChapterFilter }?.let { "Ch. ${it.number}" } ?: "All 5 Chapters"
                    "$currentChName (${filteredDocs.size} sections)"
                },
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Backend Knowledge Base",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Results & Textbook Sections List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (filteredDocs.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No textbook sections found",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for another topic like 'aller', 'avoir', 'nombres', or choose 'All Chapters' above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(filteredDocs, key = { it.id }) { doc ->
                    val ch = chapters.find { it.id == doc.chapterId }
                    TextbookSectionCard(
                        document = doc,
                        chapterName = ch?.let { "Chapitre ${it.number}" } ?: "Chapitre",
                        searchQuery = searchQuery,
                        onReadClick = { viewingDoc = doc },
                        onListenClick = {
                            val firstFrench = doc.contentText.lines().find { it.contains(":") || it.contains("•") } ?: doc.title
                            viewModel.speakFrench(firstFrench.take(120))
                        },
                        onAskTutorClick = {
                            viewModel.selectChapter(doc.chapterId)
                            viewModel.navigateTo(Screen.ASK_ANYTHING)
                            viewModel.sendChatMessage("Can you explain '${doc.title}' from ${ch?.title ?: "the textbook"}?")
                        },
                        onStudyNotesClick = {
                            viewModel.selectChapter(doc.chapterId)
                            viewModel.navigateTo(Screen.STUDY)
                        }
                    )
                }
            }
        }
    }

    // Modal to View Full Textbook Document with clean typography
    viewingDoc?.let { doc ->
        val docChapter = chapters.find { it.id == doc.chapterId }
        Dialog(onDismissRequest = { viewingDoc = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = docChapter?.let { "Ch. ${it.number} • ${doc.pageOrTimestamp}" } ?: doc.pageOrTimestamp,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(onClick = { viewingDoc = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    if (doc.isProfessorEmphasis) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⭐ Professor's Guidance: ${doc.professorGuidance.ifEmpty { "High exam priority" }}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        item {
                            Text(
                                text = doc.contentText,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 21.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Textbook Content", doc.contentText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    viewModel.speakFrench(doc.contentText.take(250))
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pronounce", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            FilledTonalButton(
                                onClick = {
                                    val targetCh = doc.chapterId
                                    viewingDoc = null
                                    viewModel.selectChapter(targetCh)
                                    viewModel.navigateTo(Screen.ASK_ANYTHING)
                                    viewModel.sendChatMessage("Explain this section from Chapter ${docChapter?.number ?: ""}: ${doc.title}")
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ask Tutor", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextbookSectionCard(
    document: CourseDocumentEntity,
    chapterName: String,
    searchQuery: String,
    onReadClick: () -> Unit,
    onListenClick: () -> Unit,
    onAskTutorClick: () -> Unit,
    onStudyNotesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onReadClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("doc_card_${document.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Chapter & Page Citation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = chapterName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = document.pageOrTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Section Title
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Professor exam warning if applicable
            if (document.isProfessorEmphasis && document.professorGuidance.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Exam Tip: ${document.professorGuidance.take(65)}...",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Excerpt preview (shows match context if search active)
            Spacer(modifier = Modifier.height(6.dp))
            val previewText = remember(document.contentText, searchQuery) {
                if (searchQuery.isNotBlank()) {
                    val idx = document.contentText.indexOf(searchQuery, ignoreCase = true)
                    if (idx != -1) {
                        val start = (idx - 35).coerceAtLeast(0)
                        val end = (idx + searchQuery.length + 80).coerceAtMost(document.contentText.length)
                        val prefix = if (start > 0) "... " else ""
                        val suffix = if (end < document.contentText.length) " ..." else ""
                        prefix + document.contentText.substring(start, end).replace("\n", " ") + suffix
                    } else {
                        document.contentText.lines().take(2).joinToString(" ").take(120)
                    }
                } else {
                    document.contentText.lines().filter { it.isNotBlank() }.take(2).joinToString(" ").take(130) + "..."
                }
            }

            Text(
                text = previewText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReadClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Read Section", fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onListenClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Pronounce", modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    OutlinedButton(
                        onClick = onAskTutorClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ask Tutor", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    FilledTonalButton(
                        onClick = onStudyNotesClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Study", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
