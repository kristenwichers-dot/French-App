package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.ui.components.AccentKeyboardRow
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.FrenchCompanionViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskAnythingScreen(
    viewModel: FrenchCompanionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    val translationMode by viewModel.translationMode.collectAsState()
    val showPhonetics by viewModel.showPhonetics.collectAsState()
    val isListening by viewModel.speechEngine.isListening.collectAsState()
    val liveTranscript by viewModel.speechEngine.liveTranscript.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    var isRecordingForChat by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Camera and homework visual capture state
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    var cameraTempFile by remember { mutableStateOf<File?>(null) }
    var capturedHomeworkBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showHomeworkConfirmDialog by remember { mutableStateOf(false) }
    var homeworkInstructionText by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = cameraTempUri
        val tempFile = cameraTempFile
        if (success && uri != null) {
            val bmp = decodeBitmapFromUri(context, uri)
            if (bmp != null) {
                capturedHomeworkBitmap = bmp
                showHomeworkConfirmDialog = true
            } else {
                Toast.makeText(context, "Could not decode homework photo", Toast.LENGTH_SHORT).show()
            }
            try {
                if (tempFile != null && tempFile.exists()) tempFile.delete()
                context.contentResolver.delete(uri, null, null)
            } catch (ignored: Exception) {}
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bmp = decodeBitmapFromUri(context, uri)
            if (bmp != null) {
                capturedHomeworkBitmap = bmp
                showHomeworkConfirmDialog = true
            }
        }
    }

    fun takeHomeworkPhoto() {
        try {
            val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
            val tempFile = File.createTempFile("homework_screen_", ".jpg", imagesFolder)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
            cameraTempFile = tempFile
            cameraTempUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(liveTranscript) {
        if (isRecordingForChat && liveTranscript.isNotBlank()) {
            inputPrompt = liveTranscript
        }
    }

    LaunchedEffect(isListening) {
        if (!isListening && isRecordingForChat) {
            isRecordingForChat = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("ask_anything_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Mode Toggle Pills: Auto (default), Quick Answer, Tutor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = translationMode == "AUTO",
                onClick = { viewModel.setTranslationMode("AUTO") },
                label = { Text("Auto", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tutor_mode_auto")
            )
            FilterChip(
                selected = translationMode == "QUICK",
                onClick = { viewModel.setTranslationMode("QUICK") },
                label = { Text("Quick Answer", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tutor_mode_quick")
            )
            FilterChip(
                selected = translationMode == "TUTOR",
                onClick = { viewModel.setTranslationMode("TUTOR") },
                label = { Text("Tutor Mode", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tutor_mode_tutor")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Homework Camera Solver Action Banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Computer Screen Homework Solver",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Point camera at your screen to answer questions like Gemini",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { takeHomeworkPhoto() },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("snap_screen_homework_btn")
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snap Screen", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    showPhonetics = showPhonetics,
                    onSpeak = { text -> viewModel.speakFrench(text) },
                    onSaveToLibrary = {
                        viewModel.saveHomeworkSolutionToKnowledgeBase(msg)
                        Toast.makeText(context, "Saved to Course Knowledge Base!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini is analyzing homework question & consulting course knowledge...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Voice to Text Active Listening Indicator
        if (isRecordingForChat && isListening) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Listening... Speak your question in English or French (American accent adapted)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // French Accent Helper Keyboard
        AccentKeyboardRow(
            onAccentClick = { char -> inputPrompt += char }
        )

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Ask a question, grammar rule, or homework...") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Camera Button (Point at computer screen)
            IconButton(
                onClick = { takeHomeworkPhoto() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("camera_homework_input_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Point camera at homework question",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Gallery / Photo Picker Button
            IconButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("gallery_homework_input_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Upload homework photo",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Voice to Text Microphone Button
            IconButton(
                onClick = {
                    if (isListening && isRecordingForChat) {
                        viewModel.speechEngine.stopListening()
                        isRecordingForChat = false
                    } else {
                        isRecordingForChat = true
                        viewModel.speechEngine.startListening("auto") { spoken ->
                            inputPrompt = spoken
                        }
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isRecordingForChat && isListening) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                    .testTag("voice_to_text_question_btn")
            ) {
                Icon(
                    imageVector = if (isRecordingForChat && isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                    contentDescription = "Voice to text question",
                    tint = if (isRecordingForChat && isListening) Color.White else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = {
                    if (inputPrompt.isNotBlank()) {
                        viewModel.sendChatMessage(inputPrompt)
                        inputPrompt = ""
                    }
                },
                enabled = inputPrompt.isNotBlank() && !isLoading,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (inputPrompt.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("chat_send_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputPrompt.isNotBlank()) Color.White else MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    // Modal to confirm captured homework image before solving
    if (showHomeworkConfirmDialog && capturedHomeworkBitmap != null) {
        Dialog(onDismissRequest = {
            showHomeworkConfirmDialog = false
            capturedHomeworkBitmap = null
        }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Solve Homework with Gemini", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        IconButton(onClick = {
                            showHomeworkConfirmDialog = false
                            capturedHomeworkBitmap = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = capturedHomeworkBitmap!!.asImageBitmap(),
                        contentDescription = "Captured homework problem",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = homeworkInstructionText,
                        onValueChange = { homeworkInstructionText = it },
                        placeholder = { Text("Optional instruction (e.g. 'Solve #3' or leave blank to solve)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showHomeworkConfirmDialog = false
                                takeHomeworkPhoto()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retake")
                        }
                        Button(
                            onClick = {
                                val bmp = capturedHomeworkBitmap!!
                                val note = homeworkInstructionText
                                showHomeworkConfirmDialog = false
                                capturedHomeworkBitmap = null
                                homeworkInstructionText = ""
                                viewModel.solveHomeworkWithCamera(bmp, note)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Solve Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    showPhonetics: Boolean,
    onSpeak: (String) -> Unit,
    onSaveToLibrary: () -> Unit = {}
) {
    val isUser = message.sender == "USER"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Tutor",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp, end = 6.dp)
            )
        }

        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // If user attached an image of homework:
                if (message.imageBitmap != null) {
                    Image(
                        bitmap = message.imageBitmap.asImageBitmap(),
                        contentDescription = "Captured homework",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (isUser) {
                    Text(
                        text = message.englishText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                } else {
                    if (message.isHomeworkQuestion) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HOMEWORK QUESTION SOLVED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (message.frenchText.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.frenchText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    val target = message.frenchAudioTarget.ifBlank { message.frenchText }
                                    onSpeak(target)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }

                        if (showPhonetics && message.phoneticText.isNotBlank()) {
                            Text(
                                text = "[ ${message.phoneticText} ]",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (message.englishText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.englishText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (message.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = message.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (message.isHomeworkQuestion) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onSaveToLibrary,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Course Knowledge Base", fontSize = 11.sp)
                        }
                    }

                    if (message.errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ ${message.errorMessage}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (message.citation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📖 Grounded in: ${message.citation}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

fun decodeBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1920): Bitmap? {
    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        val maxSide = maxOf(options.outWidth, options.outHeight)
        var sampleSize = 1
        while (maxSide / sampleSize > maxDimension) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    } catch (e: Exception) {
        null
    }
}
