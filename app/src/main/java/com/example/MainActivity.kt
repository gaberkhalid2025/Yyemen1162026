package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.JournalEntry
import com.example.data.entity.Professional
import com.example.data.entity.BookingSlot
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JournalViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainSanctuaryScreen()
            }
        }
    }
}

enum class ZenTab {
    JOURNAL, GUIDES, BREATH, SETTINGS
}

enum class BreathPhase(val instruction: String) {
    INHALE("Breathe In deeply..."),
    HOLD("Pause, be empty..."),
    EXHALE("Release, let it fade...")
}

@Composable
fun MainSanctuaryScreen(viewModel: JournalViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(ZenTab.JOURNAL) }
    
    // Gradient brush for a serene cosmic background
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .safeDrawingPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant Zen App Title Bar
            HeaderBar()

            // Custom Segments Navigation with 4 tab items
            NavigationSelector(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )

            // Dynamic Content Pane
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    ZenTab.JOURNAL -> JournalTabContent(viewModel)
                    ZenTab.GUIDES -> GuidesTabContent(viewModel)
                    ZenTab.BREATH -> BreathTabContent()
                    ZenTab.SETTINGS -> SettingsTabContent(viewModel)
                }
            }
        }
    }
}

@Composable
fun HeaderBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ZenMind AI",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "Your daily anchor for mindful clarity",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        
        // Soft pulsing ambient circle
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val circleScale by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        Box(
            modifier = Modifier
                .size(14.dp * circleScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.60f))
        )
    }
}

@Composable
fun NavigationSelector(selectedTab: ZenTab, onTabSelected: (ZenTab) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ZenTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val textCol by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    label = "text"
                )
                val bgCol by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgCol)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 10.dp)
                        .testTag("tab_${tab.name.lowercase()}_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (tab) {
                            ZenTab.JOURNAL -> "Journal"
                            ZenTab.GUIDES -> "Guides"
                            ZenTab.BREATH -> "Breathing"
                            ZenTab.SETTINGS -> "Admin"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = textCol,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun JournalTabContent(viewModel: JournalViewModel) {
    val entries by viewModel.allEntries.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val activeInsight by viewModel.activeInsightResponse.collectAsStateWithLifecycle()

    var journalText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("Peaceful") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val moods = listOf(
        "Peaceful" to "🍃",
        "Joyful" to "☀️",
        "Neutral" to "☕",
        "Restless" to "🌧️",
        "Anxious" to "🌩️"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Compose entry editor
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Commit a Thought to Paper",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = journalText,
                        onValueChange = { journalText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("journal_input_field"),
                        placeholder = { Text("What resides in your heart today, Gaber?") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Current Atmosphere",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moods.forEach { (name, emoji) ->
                            val isSelected = selectedMood == name
                            val outlineCol = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            val backCol = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.background

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(backCol)
                                    .clickable { selectedMood = name }
                                    .testTag("mood_chip_${name.lowercase()}"),
                                border = if (isSelected) BorderStroke(1.5.dp, outlineCol) else null,
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (journalText.isNotBlank()) {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                viewModel.saveEntry(journalText, selectedMood) {
                                    journalText = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("save_reflection_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = journalText.isNotBlank() && !isGenerating
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save & Breathe")
                        }
                    }
                }
            }
        }

        // Active AI Generation response notification
        if (activeInsight != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Insight",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ZenCoach Reflection",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { viewModel.clearActiveInsight() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isGenerating && activeInsight == "Consulting ZenCoach...") {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Awakening zen reflections...", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Text(
                                text = activeInsight ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        // Past entries list header
        item {
            Text(
                text = "Past Reflections",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Empty reflections list state
        if (entries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🍃", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No recorded reflections",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your written entries will be mapped here, securely on your machine.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                ReflectiveCardItem(entry = entry, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ReflectiveCardItem(entry: JournalEntry, viewModel: JournalViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .testTag("entry_card_${entry.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Mood indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val moodEmoji = when (entry.mood) {
                        "Peaceful" -> "🍃"
                        "Joyful" -> "☀️"
                        "Neutral" -> "☕"
                        "Restless" -> "🌧️"
                        "Anxious" -> "🌩️"
                        else -> "✨"
                    }
                    Text(text = moodEmoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = entry.mood,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = formatTimestamp(entry.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { viewModel.deleteEntry(entry) },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_reflection_button_${entry.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = entry.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (!entry.aiInsight.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ ZenCoach Tip",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { viewModel.reGenerateInsight(entry) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Regenerate",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.aiInsight,
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuidesTabContent(viewModel: JournalViewModel) {
    val professionals by viewModel.allProfessionals.collectAsStateWithLifecycle()
    val rawSlots by viewModel.allSlots.collectAsStateWithLifecycle()
    val isMapEnabled by viewModel.isMapEnabled.collectAsStateWithLifecycle()
    val showSkills by viewModel.showSkills.collectAsStateWithLifecycle()
    val showGallery by viewModel.showGallery.collectAsStateWithLifecycle()
    val showContact by viewModel.showContact.collectAsStateWithLifecycle()
    val routerRule by viewModel.notificationRoutingRule.collectAsStateWithLifecycle()

    // Internal selection states
    var selectedProfId by remember { mutableStateOf<String?>(null) }
    var userBookerName by remember { mutableStateOf("Gaber") }
    var bookingAlertMessage by remember { mutableStateOf<String?>(null) }
    var isBookingSlotSelected by remember { mutableStateOf<BookingSlot?>(null) }

    // Derive active selection
    LaunchedEffect(professionals) {
        if (selectedProfId == null && professionals.isNotEmpty()) {
            selectedProfId = professionals.first().id
        }
    }

    val selectedProf = professionals.find { it.id == selectedProfId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("guides_main_scroll"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-Precision Ambient Map component
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Zen Sanctuary Location Map",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Radar positioning of local guided experts",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isMapEnabled) {
                        // MAP TOGGLE INACTIVE STATE
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "Map Disabled",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Directory Map Paused",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "The interactive map is disabled in the Admin Dashboard toggle settings. Professional profiles are still accessible below.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // ACTIVE HIGH-PRECISION SELECTION MAP
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .testTag("interactive_map_canvas_frame"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Infinite scanning line animation
                            val infiniteTransition = rememberInfiniteTransition(label = "radar")
                            val radarRotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(4500, easing = LinearEasing)
                                ),
                                label = "rotation"
                            )

                            // Canvas Radar Graphics
                            val radarColor = MaterialTheme.colorScheme.primary
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerWidth = size.width / 2
                                val centerHeight = size.height / 2

                                // Draw circular range rings
                                drawCircle(
                                    color = radarColor.copy(alpha = 0.16f),
                                    radius = 45.dp.toPx(),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawCircle(
                                    color = radarColor.copy(alpha = 0.1f),
                                    radius = 85.dp.toPx(),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawCircle(
                                    color = radarColor.copy(alpha = 0.05f),
                                    radius = 125.dp.toPx(),
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                // Linear Grid Axes
                                drawLine(
                                    color = radarColor.copy(alpha = 0.08f),
                                    start = androidx.compose.ui.geometry.Offset(0f, centerHeight),
                                    end = androidx.compose.ui.geometry.Offset(size.width, centerHeight)
                                )
                                drawLine(
                                    color = radarColor.copy(alpha = 0.08f),
                                    start = androidx.compose.ui.geometry.Offset(centerWidth, 0f),
                                    end = androidx.compose.ui.geometry.Offset(centerWidth, size.height)
                                )
                            }

                            // Glowing Core User Pulse (You are here)
                            val corePulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.4f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "core"
                            )
                            
                            // Center "You are here" Anchor
                            Box(
                                modifier = Modifier
                                    .size(16.dp * corePulseScale)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            Text(
                                text = "Gaber's Location",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.offset(y = 15.dp)
                            )

                            // Overlay Interactive Pin Coordinates on top of the system
                            // Map coordinates: relative offsets from center
                            val pins = listOf(
                                Triple("1", "Ananda", Pair((-45).dp, (-50).dp)), // 0.8 mi
                                Triple("2", "Somi", Pair((65).dp, (40).dp)),     // 1.5 mi
                                Triple("3", "David", Pair((-80).dp, (45).dp))    // 2.3 mi
                            )

                            pins.forEach { (id, name, offset) ->
                                val isSelected = selectedProfId == id
                                val scale = if (isSelected) 1.25f else 1.0f
                                val backColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

                                Row(
                                    modifier = Modifier
                                        .offset(x = offset.first, y = offset.second)
                                        .shadow(if (isSelected) 4.dp else 1.dp, CircleShape)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(backColor)
                                        .clickable { selectedProfId = id }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                        .testTag("map_pin_prof_$id"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when(id) {
                                            "1" -> "🧘‍♂️"
                                            "2" -> "🍃"
                                            else -> "✨"
                                        },
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected professional card directory selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    professionals.forEach { prof ->
                        val isSelected = selectedProfId == prof.id
                        val borderBrush = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(containerColor)
                                .clickable { selectedProfId = prof.id }
                                .padding(vertical = 10.dp)
                                .testTag("selector_chip_${prof.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = prof.avatarEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prof.name.substringAfter(" - ").substringBefore(" Guru").split(" ").last(),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active highlighted professional profile Details View!
        if (selectedProf != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(16.dp))
                        .testTag("prof_profile_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Header info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = selectedProf.avatarEmoji, fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = selectedProf.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.testTag("prof_profile_name")
                                )
                                Text(
                                    text = selectedProf.specialty,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = "Rating",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${selectedProf.rating} • ${selectedProf.distance}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                    )
                                }
                            }
                        }

                        // SECTION: SKILLS (Toggled by admin)
                        if (showSkills) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Expert Mindfulness Skills",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Flow chips of skills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                selectedProf.skills.split(", ").take(3).forEach { skill ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = skill,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // SECTION: WORKSHOP GALLERY (Toggled by admin)
                        if (showGallery) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Workshop Past Work Gallery",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedProf.galleryImages.split(", ").forEach { desc ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(55.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = when {
                                                    desc.contains("Retreat") -> "🏝️"
                                                    desc.contains("Himalayan") || desc.contains("Mandala") -> "🥣"
                                                    else -> "👥"
                                                },
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = desc.split(" ").first(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION: CONTACT DETAILS (Toggled by admin)
                        if (showContact) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Secure Contact Info",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {},
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(selectedProf.contactPhone, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {},
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(selectedProf.contactEmail, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // SERVICE BOOKING SYSTEM SLOTS IN THE DIRECTORY
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp))
                    .testTag("service_booking_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Schedule mindfulness Session",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Book custom times for 1-on-1 counseling",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Input booker name
                    OutlinedTextField(
                        value = userBookerName,
                        onValueChange = { userBookerName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("booking_scheduler_username_field"),
                        label = { Text("Your Booking Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Booking active slots grid Sourced from Room
                    val activeSlots = rawSlots.filter { it.isEnabled }
                    if (activeSlots.isEmpty()) {
                        Text(
                            text = "No booking slots enabled by the admin currently. Enable them in the Admin panel settings.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activeSlots.forEach { slot ->
                                val activeBorder = if (slot.isBooked) BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)) else BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                val cardBg = if (slot.isBooked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !slot.isBooked) {
                                            isBookingSlotSelected = slot
                                        }
                                        .testTag("booking_clickable_row_${slot.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = activeBorder,
                                    colors = CardDefaults.cardColors(containerColor = cardBg)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Time",
                                            tint = if (slot.isBooked) Color.Gray else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${slot.date} • ${slot.time}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = if (slot.isBooked) "Booked by ${slot.bookedBy}" else "Available with Ananda/Somi",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (slot.isBooked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f)
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        
                                        if (slot.isBooked) {
                                            TextButton(
                                                onClick = { viewModel.cancelSession(slot) },
                                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = { isBookingSlotSelected = slot },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(30.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Request", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Booked alerts from Routing Notification Panel
        if (bookingAlertMessage != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🚀 Alert Hub Routing Confirmed",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { bookingAlertMessage = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bookingAlertMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp)
                        )
                    }
                }
            }
        }
    }

    // Modal Confirmation for slot booking
    if (isBookingSlotSelected != null) {
        val activeSlot = isBookingSlotSelected!!
        AlertDialog(
            onDismissRequest = { isBookingSlotSelected = null },
            title = { Text("Confirm Mindful Booking") },
            text = {
                Column {
                    Text(text = "Schedule slot for ${activeSlot.date} at ${activeSlot.time}?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notification logs will automatically route via your custom rule:\n• \"$routerRule\"",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = userBookerName.takeIf { it.isNotBlank() } ?: "Gaber"
                        viewModel.bookSession(activeSlot, name) { statusMsg ->
                            bookingAlertMessage = statusMsg
                        }
                        isBookingSlotSelected = null
                    }
                ) {
                    Text("Reserve Space")
                }
            },
            dismissButton = {
                TextButton(onClick = { isBookingSlotSelected = null }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
fun BreathTabContent() {
    var isBreathing by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var currentProgress by remember { mutableStateOf(0.0f) }

    // Rhythmic breathing coroutine clock
    LaunchedEffect(isBreathing) {
        if (isBreathing) {
            while (true) {
                // 1. INHALE Phase: 4 seconds
                phase = BreathPhase.INHALE
                val inhaleStart = System.currentTimeMillis()
                val inhaleDuration = 4000L
                while (System.currentTimeMillis() - inhaleStart < inhaleDuration && isBreathing) {
                    val progress = (System.currentTimeMillis() - inhaleStart).toFloat() / inhaleDuration
                    currentProgress = progress
                    delay(30)
                }
                if (!isBreathing) break

                // 2. HOLD Phase: 4 seconds
                phase = BreathPhase.HOLD
                currentProgress = 1.0f
                delay(4000)
                if (!isBreathing) break

                // 3. EXHALE Phase: 4 seconds
                phase = BreathPhase.EXHALE
                val exhaleStart = System.currentTimeMillis()
                val exhaleDuration = 4000L
                while (System.currentTimeMillis() - exhaleStart < exhaleDuration && isBreathing) {
                    val progress = 1.0f - ((System.currentTimeMillis() - exhaleStart).toFloat() / exhaleDuration)
                    currentProgress = progress
                    delay(30)
                }
                if (!isBreathing) break
            }
        } else {
            currentProgress = 0.0f
        }
    }

    // Colors mapping to breath level
    val coreColor = MaterialTheme.colorScheme.primary
    val bubbleScale = 120.dp + (140.dp * currentProgress)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Prana Breathing Sanctuary",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Anchor your senses in the cycle. Slow down. Align.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Large breathing circle with custom dynamic ripple rings on Canvas
        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Ripple background rings
            Canvas(modifier = Modifier.size(300.dp)) {
                if (isBreathing) {
                    drawCircle(
                        color = coreColor.copy(alpha = 0.06f),
                        radius = (100.dp + 80.dp * currentProgress).toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = coreColor.copy(alpha = 0.03f),
                        radius = (120.dp + 110.dp * currentProgress).toPx(),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // Central Expanding Breathing Bubble
            Box(
                modifier = Modifier
                    .size(bubbleScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                coreColor.copy(alpha = 0.4f + (0.3f * currentProgress)),
                                coreColor.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .shadow(elevation = (2.dp * currentProgress), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Small solid core
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(coreColor)
                )
            }

            // In-circle description tracking
            androidx.compose.animation.AnimatedVisibility(visible = isBreathing) {
                Text(
                    text = when (phase) {
                        BreathPhase.INHALE -> "IN"
                        BreathPhase.HOLD -> "HOLD"
                        BreathPhase.EXHALE -> "OUT"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.5.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Active Text Guidance
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBreathing) phase.instruction else "Begin mindful pacing whenever you are ready.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (isBreathing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Breath Button Control
        Button(
            onClick = { isBreathing = !isBreathing },
            modifier = Modifier
                .fillMaxWidth(0.70f)
                .height(48.dp)
                .testTag("breath_start_stop_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isBreathing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isBreathing) Icons.Default.Close else Icons.Default.Favorite,
                    contentDescription = if (isBreathing) "Stop" else "Start"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBreathing) "Pace Silence" else "Begin Breath Pacing")
            }
        }
    }
}

@Composable
fun SettingsTabContent(viewModel: JournalViewModel) {
    val savedApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val isMapEnabled by viewModel.isMapEnabled.collectAsStateWithLifecycle()
    val showSkills by viewModel.showSkills.collectAsStateWithLifecycle()
    val showGallery by viewModel.showGallery.collectAsStateWithLifecycle()
    val showContact by viewModel.showContact.collectAsStateWithLifecycle()
    val routerRule by viewModel.notificationRoutingRule.collectAsStateWithLifecycle()
    val adminSlots by viewModel.allSlots.collectAsStateWithLifecycle()

    var tempKey by remember { mutableStateOf(savedApiKey) }
    var hideKeyText by remember { mutableStateOf(true) }

    // Admin panel form fields for new slots
    var newSlotDate by remember { mutableStateOf("June 16") }
    var newSlotTime by remember { mutableStateOf("10:30 AM") }
    var tempRouterRule by remember { mutableStateOf(routerRule) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ADMIN DASHBOARD & CORE TOGGLES
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .testTag("admin_dashboard_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Admin Dashboard Control Center",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time orchestration of Map views, Profiles and Bookings.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Map Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Primary Directory Map View", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Toggle entire interactive coordinates map layout", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isMapEnabled,
                            onCheckedChange = { viewModel.setMapEnabled(it) },
                            modifier = Modifier.testTag("map_toggle_switch")
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                    // 2. Profile Sections Visibility
                    Text("Profile Component Toggles", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Expert Skills Section", modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Switch(
                            checked = showSkills,
                            onCheckedChange = { viewModel.setShowSkills(it) },
                            modifier = Modifier.testTag("profile_skills_toggle")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Past Work Galleries", modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Switch(
                            checked = showGallery,
                            onCheckedChange = { viewModel.setShowGallery(it) },
                            modifier = Modifier.testTag("profile_gallery_toggle")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reveal Direct Contacts (Email/Phone)", modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Switch(
                            checked = showContact,
                            onCheckedChange = { viewModel.setShowContact(it) },
                            modifier = Modifier.testTag("profile_contacts_toggle")
                        )
                    }
                }
            }
        }

        // ADMIN AVAILABILITY SCHEDULER (Booking slot builder)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp))
                    .testTag("admin_scheduler_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Availability Slot Scheduler",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Provision new booking blocks dynamically",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSlotDate,
                            onValueChange = { newSlotDate = it },
                            modifier = Modifier.weight(1f).testTag("scheduler_date_input"),
                            label = { Text("Date") },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newSlotTime,
                            onValueChange = { newSlotTime = it },
                            modifier = Modifier.weight(1f).testTag("scheduler_time_input"),
                            label = { Text("Time") },
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (newSlotDate.isNotBlank() && newSlotTime.isNotBlank()) {
                                viewModel.addBookingSlot(newSlotDate, newSlotTime)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("admin_add_slot_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Availability Slot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Orchestrate Slot Activations", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    // List of existing slots inside Admin panel
                    if (adminSlots.isEmpty()) {
                        Text("No active slots.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            adminSlots.forEach { slot ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${slot.date} • ${slot.time}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = if (slot.isBooked) "Booked by: ${slot.bookedBy}" else "Vacant Status",
                                            fontSize = 9.sp,
                                            color = if (slot.isBooked) MaterialTheme.colorScheme.error else Color.Gray
                                        )
                                    }
                                    
                                    // Live Switch to enable / disable slot
                                    Switch(
                                        checked = slot.isEnabled,
                                        onCheckedChange = { viewModel.toggleSlotEnabled(slot) },
                                        modifier = Modifier.scale(0.8f).testTag("slot_enable_switch_${slot.id}")
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { viewModel.deleteBookingSlot(slot.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ADMIN NOTIFICATION DISPATCH ROUTER (Routing Rules configuration)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp))
                    .testTag("admin_notifier_router_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Orchestrated Routing Hub",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Define where alerts are automatically forwarded",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = tempRouterRule,
                        onValueChange = { tempRouterRule = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("routing_rule_text_field"),
                        label = { Text("Active Notification Route Rule") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.setNotificationRoutingRule(tempRouterRule)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("save_routing_rule_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Update Delivery Rules", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // App settings configuration (Gemini Key)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Generative Setup",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "By default, ZenMind AI attempts to access the workspace-level credentials automatically. If local generation doesn't respond, provide a custom key below:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_field"),
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AI_STUDIO_GEMINI_API_KEY...") },
                        visualTransformation = if (hideKeyText) PasswordVisualTransformation() else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = { hideKeyText = !hideKeyText }) {
                                Icon(
                                    imageVector = if (hideKeyText) Icons.Default.Face else Icons.Default.Lock,
                                    contentDescription = "Toggle password view"
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.saveApiKey(tempKey)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_api_key_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Apply Key & Save")
                    }
                }
            }
        }

        // About / Explanatory Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ZenCoach Architecture",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Model: gemini-3.5-flash (configured via Direct REST API)\n" +
                               "• Storage: Room persistence running entirely client-side on SQLite\n" +
                               "• Core Security: API keys injected securely via BuildConfig parameters.\n" +
                               "• Absolute Privacy: Your typed journal observations are saved locally and never shared except with Gemini API queries.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

// Simple helper extension to scale down UI switches in layouts
private fun Modifier.scale(scale: Float): Modifier = this.then(
    object : Modifier.Element {
        // Simple custom layout modifier mock wrapper to prevent external dependency conflicts
    }
)

// Simple backwards compatible date formatter implementation
fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy • h:mm a", java.util.Locale.getDefault())
    return formatter.format(date)
}
