package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AccentKeyboardRow
import com.example.viewmodel.FrenchCompanionViewModel

@Composable
fun TranslateScreen(
    viewModel: FrenchCompanionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRecording by viewModel.isRecordingSpeech.collectAsState()
    val liveTranscript by viewModel.liveSpokenTranscript.collectAsState()
    val livePhrases by viewModel.liveTranscripts.collectAsState()
    val audioRms by viewModel.speechAudioRms.collectAsState()

    // Type to translate state
    var inputText by remember { mutableStateOf("") }
    var addExplanation by remember { mutableStateOf(false) }
    var selectedDirection by remember { mutableStateOf("English ➔ French") }
    var directionMenuExpanded by remember { mutableStateOf(false) }

    // Displayed Translation & Language Detection State (Starts clean, NO fake examples!)
    var detectedLanguage by remember { mutableStateOf("Auto") }
    var targetLanguage by remember { mutableStateOf("French") }
    var currentSpoken by remember { mutableStateOf("") }
    var currentTranslated by remember { mutableStateOf("") }
    var currentPhonetics by remember { mutableStateOf("") }
    var currentExplanation by remember { mutableStateOf("") }
    var currentAlternatives by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentErrorMessage by remember { mutableStateOf<String?>(null) }
    var isTranslatingText by remember { mutableStateOf(false) }
    var isTranslatingSpoken by remember { mutableStateOf(false) }

    val isStreamingTranslation by viewModel.isStreamingTranslation.collectAsState()

    LaunchedEffect(livePhrases) {
        val latest = livePhrases.firstOrNull()
        if (latest != null) {
            detectedLanguage = latest.spokenLang
            targetLanguage = latest.targetLang
            currentSpoken = latest.spokenText.ifEmpty { latest.french }
            currentTranslated = latest.translatedText.ifEmpty { latest.english }
            currentPhonetics = latest.phonetics
            currentAlternatives = latest.alternatives
            currentExplanation = latest.explanation
            currentErrorMessage = latest.errorMessage
            isTranslatingSpoken = false
        }
    }

    val executeSpeechFinish = {
        isTranslatingSpoken = true
        viewModel.finishManualSpeechSession { transcript ->
            if (transcript.isNotBlank()) {
                currentSpoken = transcript
                currentErrorMessage = null
                currentTranslated = ""
                val detected = viewModel.detectLanguage(transcript)
                detectedLanguage = detected
                targetLanguage = if (detected == "English") "French" else "English"
                viewModel.startStreamingTranslation(
                    text = transcript,
                    addExplanation = false,
                    onChunk = { accumulated ->
                        isTranslatingSpoken = false
                        currentTranslated = accumulated
                    },
                    onComplete = { finalText ->
                        isTranslatingSpoken = false
                        currentTranslated = finalText
                    },
                    onError = { error ->
                        isTranslatingSpoken = false
                        currentErrorMessage = error
                    }
                )
            } else {
                isTranslatingSpoken = false
                currentErrorMessage = "No speech detected. Please touch the mic and speak clearly."
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("translate_screen_container")
    ) {
        // Centered Main Header
        Text(
            text = "Instant translation",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Auto-detects whether you speak English or French and gives the accurate translation with phonetic guide and alternative options.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Human French Voice & American Accent Tolerance Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val voiceName by viewModel.speechEngine.voiceName.collectAsState()
            AssistChip(
                onClick = {
                    viewModel.speakFrench("Bonjour ! Voici la voix humaine avec un accent français naturel.")
                },
                leadingIcon = {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                },
                label = {
                    Text("Voice: $voiceName", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                },
                modifier = Modifier.weight(1f)
            )

            AssistChip(
                onClick = {
                    Toast.makeText(context, "American accent adaptation active: Phonetics automatically normalize your spoken French!", Toast.LENGTH_SHORT).show()
                },
                leadingIcon = {
                    Icon(Icons.Default.Hearing, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                },
                label = {
                    Text("American Accent: Active", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_listening_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOUCH-TO-SPEAK TRANSLATOR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (isRecording) "Listening • Tap to finish" else if (isTranslatingSpoken) "Translating..." else "Ready",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isRecording) MaterialTheme.colorScheme.error else if (isTranslatingSpoken) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prominent 1-Tap Language Direction Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedDirection == "English ➔ French",
                        onClick = {
                            if (!isRecording) selectedDirection = "English ➔ French"
                        },
                        label = { Text("🇺🇸 Speak English", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedDirection == "French ➔ English",
                        onClick = {
                            if (!isRecording) selectedDirection = "French ➔ English"
                        },
                        label = { Text("🇫🇷 Speak French", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedDirection == "Auto-Detect",
                        onClick = {
                            if (!isRecording) selectedDirection = "Auto-Detect"
                        },
                        label = { Text("🌐 Auto", fontSize = 12.sp) },
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isRecording) 1.08f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                // Large Rectangular Touch-to-Speak / Touch-to-Finish Button
                Surface(
                    onClick = {
                        if (isRecording) {
                            executeSpeechFinish()
                        } else {
                            val dirMode = when (selectedDirection) {
                                "English ➔ French" -> "ENGLISH_TO_FRENCH"
                                "French ➔ English" -> "FRENCH_TO_ENGLISH"
                                else -> "AUTO"
                            }
                            currentErrorMessage = null
                            viewModel.startManualSpeechSession(dirMode)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .testTag("tap_to_listen_btn"),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = if (isRecording) 6.dp else 2.dp,
                    border = if (!isRecording) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Large Icon Badge
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .scale(if (isRecording) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    if (isRecording) Color.White.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.primary
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Touch when finished speaking" else "Touch mic to speak",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Large Action Labels
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isRecording) "RECORDING • TAP TO FINISH" else "TAP TO SPEAK",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isRecording) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isRecording)
                                    "Tap to stop recording & translate live"
                                else
                                    "Tap anywhere to speak English or French",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = if (isRecording) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Audio Wave Indicator when recording, or audio graphic icon when ready
                        if (isRecording) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.height(24.dp)
                            ) {
                                val rmsNorm = ((audioRms + 2f) / 10f).coerceIn(0.15f, 1f)
                                val barHeights = listOf(
                                    (20f * rmsNorm).coerceAtLeast(6f).dp,
                                    (24f * rmsNorm * 0.85f).coerceAtLeast(8f).dp,
                                    (24f * rmsNorm * 1.15f).coerceAtLeast(10f).dp,
                                    (18f * rmsNorm * 0.9f).coerceAtLeast(6f).dp
                                )
                                barHeights.forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(h)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color.White)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Spoken Feedback when recording or translating
                if (isRecording) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_hearing_bubble")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Listening to your voice...",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                // 4 Dynamic audio level waveform bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(18.dp)
                                ) {
                                    val rmsNorm = ((audioRms + 2f) / 10f).coerceIn(0.15f, 1f)
                                    val barHeights = listOf(
                                        (16f * rmsNorm).coerceAtLeast(4f).dp,
                                        (18f * rmsNorm * 0.85f).coerceAtLeast(5f).dp,
                                        (18f * rmsNorm * 1.15f).coerceAtLeast(6f).dp,
                                        (14f * rmsNorm * 0.9f).coerceAtLeast(4f).dp
                                    )
                                    barHeights.forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(h)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(MaterialTheme.colorScheme.error)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (liveTranscript.isNotBlank()) "\"$liveTranscript\"" else "Speak now...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else if (isTranslatingSpoken) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Translating speech...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons based on recording state
                if (isRecording) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                executeSpeechFinish()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(2f)
                                .testTag("finish_listening_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish & Translate", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.cancelManualSpeechSession()
                                isTranslatingSpoken = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_listening_btn")
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (currentSpoken.isNotBlank() || currentTranslated.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    currentSpoken = ""
                                    currentTranslated = ""
                                    currentPhonetics = ""
                                    currentExplanation = ""
                                    currentAlternatives = emptyList()
                                    currentErrorMessage = null
                                    viewModel.clearLiveTranscripts()
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: TRANSLATION RESULTS (DYNAMIC SPOKEN & TRANSLATION WITH ALTERNATIVES)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("translation_result_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Language Detection Pill Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (detectedLanguage != "Auto")
                                "🌐 Detected: $detectedLanguage ➔ Translating to: $targetLanguage"
                            else
                                "🌐 Auto-Detect: English ⇄ French",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (currentSpoken.isBlank() && currentTranslated.isBlank() && currentErrorMessage == null) {
                    // Friendly prompt state when nothing has been spoken/typed yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Speak into the microphone or type below",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Translations, pronunciation, and alternative options appear here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    // Section 1: WHAT WAS SPOKEN / HEARD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WHAT I HEARD (${detectedLanguage.ifEmpty { "Input" }})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    if (detectedLanguage == "English") {
                                        viewModel.speakTranslation(currentSpoken, isEnglish = true)
                                    } else {
                                        viewModel.speakFrench(currentSpoken)
                                    }
                                },
                                enabled = currentSpoken.isNotBlank(),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Hear input", modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Input", currentSpoken))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                },
                                enabled = currentSpoken.isNotBlank(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Copy", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSpoken.ifEmpty { "Listening..." },
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Section 2: TRANSLATION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRANSLATION ($targetLanguage)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    if (targetLanguage == "English") {
                                        viewModel.speakTranslation(currentTranslated, isEnglish = true)
                                    } else {
                                        viewModel.speakFrench(currentTranslated)
                                    }
                                },
                                enabled = currentTranslated.isNotBlank(),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Hear translation with French voice/accent", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Translation", currentTranslated))
                                    Toast.makeText(context, "Translation copied", Toast.LENGTH_SHORT).show()
                                },
                                enabled = currentTranslated.isNotBlank(),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("copy_translated_btn")
                            ) {
                                Text("Copy", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentTranslated.ifEmpty {
                                if (isStreamingTranslation) "Streaming live translation..." else "Translation will appear here..."
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isStreamingTranslation) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(width = 8.dp, height = 18.dp)
                                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    if (currentErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = currentErrorMessage ?: "Error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = {
                                        if (currentSpoken.isNotBlank()) {
                                            viewModel.handleLiveSpeech(currentSpoken)
                                        }
                                    }
                                ) {
                                    Text("Retry", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    if (currentPhonetics.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Phonetic: $currentPhonetics",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    if (currentExplanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentExplanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Section 3: ALTERNATIVE OPTIONS & OTHER WAYS TO SAY THIS
                    if (currentAlternatives.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "OPTIONS & OTHER WAYS TO SAY THIS:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentAlternatives.forEach { alt ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = alt,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                val cleanPhrase = alt.substringBefore("(").trim()
                                                if (targetLanguage == "French") {
                                                    viewModel.speakFrench(cleanPhrase)
                                                } else {
                                                    viewModel.speakTranslation(cleanPhrase, isEnglish = true)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.VolumeUp,
                                                contentDescription = "Hear option",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: Hear & Save phrase
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val targetWord = if (targetLanguage == "French") currentTranslated else currentSpoken
                                viewModel.speakFrench(targetWord)
                            },
                            enabled = currentTranslated.isNotBlank() || currentSpoken.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hear_french_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hear French", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val f = if (targetLanguage == "French") currentTranslated else currentSpoken
                                val e = if (targetLanguage == "English") currentTranslated else currentSpoken
                                viewModel.saveBookmark(
                                    title = "Saved Translation",
                                    french = f,
                                    english = e,
                                    category = "Translation"
                                )
                                Toast.makeText(context, "Phrase saved to Study list!", Toast.LENGTH_SHORT).show()
                            },
                            enabled = currentTranslated.isNotBlank() || currentSpoken.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_phrase_btn")
                        ) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save phrase", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 3: TYPE TO TRANSLATE
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("type_to_translate_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TYPE TO TRANSLATE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                    AssistChip(
                        onClick = { /* Toggle */ },
                        label = { Text("Auto-detect ⇄", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type something in English or French...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("translate_input_text"),
                    shape = RoundedCornerShape(10.dp)
                )

                // French Accent Helper Keyboard
                AccentKeyboardRow(
                    onAccentClick = { char -> inputText += char }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { addExplanation = !addExplanation }
                    ) {
                        Checkbox(
                            checked = addExplanation,
                            onCheckedChange = { addExplanation = it },
                            modifier = Modifier.testTag("add_explanation_checkbox")
                        )
                        Text("Add explanation", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            if (inputText.isNotBlank() && !isTranslatingText && !isStreamingTranslation) {
                                isTranslatingText = true
                                currentSpoken = inputText
                                currentTranslated = ""
                                currentErrorMessage = null
                                val detected = viewModel.detectLanguage(inputText)
                                detectedLanguage = detected
                                targetLanguage = if (detected == "English") "French" else "English"
                                viewModel.startStreamingTranslation(
                                    text = inputText,
                                    addExplanation = addExplanation,
                                    onChunk = { accumulated ->
                                        isTranslatingText = false
                                        currentTranslated = accumulated
                                    },
                                    onComplete = { finalText ->
                                        isTranslatingText = false
                                        currentTranslated = finalText
                                    },
                                    onError = { error ->
                                        isTranslatingText = false
                                        currentErrorMessage = error
                                    }
                                )
                            }
                        },
                        enabled = inputText.isNotBlank() && !isTranslatingText && !isStreamingTranslation,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("translate_submit_btn")
                    ) {
                        if (isTranslatingText || isStreamingTranslation) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Translate →", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 4: LIVE TRANSCRIPT HISTORY
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_transcript_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE TRANSCRIPT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                    OutlinedButton(
                        onClick = {
                            val fullText = livePhrases.joinToString("\n\n") {
                                "[${it.spokenLang} ➔ ${it.targetLang}] ${it.spokenText.ifEmpty { it.french }} = ${it.translatedText.ifEmpty { it.english }}"
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Transcript", fullText))
                            Toast.makeText(context, "Copied all transcript phrases", Toast.LENGTH_SHORT).show()
                        },
                        enabled = livePhrases.isNotEmpty(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("copy_all_transcript_btn")
                    ) {
                        Text("Copy all", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (livePhrases.isEmpty()) {
                    Text(
                        text = "Live phrases appear here as you speak or listen. Language is automatically detected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    livePhrases.forEach { phrase ->
                        val spoken = phrase.spokenText.ifEmpty { phrase.french }
                        val trans = phrase.translatedText.ifEmpty { phrase.english }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${phrase.spokenLang} ➔ ${phrase.targetLang}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = spoken,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                Text(
                                    text = trans,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val toSpeak = if (phrase.targetLang == "French") trans else spoken
                                    viewModel.speakFrench(toSpeak)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Hear", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
