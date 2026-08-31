package com.example.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.EventPlanEntity
import com.example.domain.model.AppLanguage
import com.example.ui.components.BazariBottomBar
import com.example.ui.components.LanguageToggleBadge
import com.example.utils.BengaliNumberUtils
import com.example.utils.L10n
import com.example.utils.LanguageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onNavigateToEventDetail: (String) -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: EventsViewModel = viewModel()
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val activeEvents by viewModel.activeEvents.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.BN) "পিকনিক ও ইভেন্ট হিসাব" else "Events & Group Planner",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (currentLang == AppLanguage.BN) "চাঁদা কালেকশন, বাজার খরচ ও ইনভয়েস" else "Collection, expenses & memo generator",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                actions = {
                    LanguageToggleBadge(
                        currentLang = currentLang,
                        onToggle = { LanguageManager.toggleLanguage() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BazariBottomBar(
                currentRoute = "events",
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_create_event")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন ইভেন্ট যোগ করুন")
            }
        }
    ) { paddingValues ->
        if (activeEvents.isEmpty()) {
            EmptyEventsView(
                paddingValues = paddingValues,
                currentLang = currentLang,
                onCreateClick = { showCreateDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Banner
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (currentLang == AppLanguage.BN) "পিকনিক ও গ্রুপ বাজেট" else "Event & Group Budgeting",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (currentLang == AppLanguage.BN) "সবার কাছ থেকে টাকা তোলা, বাজার করা ও শেষে মেমো প্রিন্ট করুন।" else "Track member contributions, expenses and print professional invoices.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }

                items(activeEvents, key = { it.id }) { event ->
                    EventItemCard(
                        event = event,
                        currentLang = currentLang,
                        onClick = { onNavigateToEventDetail(event.id) },
                        onDelete = { viewModel.deleteEvent(event.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            currentLang = currentLang,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, type, budget, organizer, location, notes ->
                viewModel.createEvent(
                    title = title,
                    eventType = type,
                    targetBudget = budget,
                    organizerName = organizer,
                    location = location,
                    notes = notes
                ) { newId ->
                    showCreateDialog = false
                    onNavigateToEventDetail(newId)
                }
            }
        )
    }
}

@Composable
private fun EventItemCard(
    event: EventPlanEntity,
    currentLang: AppLanguage,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeLabel, typeIcon, typeColor) = getEventTypeDetails(event.eventType)
    val sdf = SimpleDateFormat("dd MMM yyyy", if (currentLang == AppLanguage.BN) Locale("bn", "BD") else Locale.US)
    val dateStr = sdf.format(Date(event.eventDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = typeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = typeColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                if (event.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            if (event.targetBudget > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বাজেট লক্ষ্যমাত্রা:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = L10n.price(event.targetBudget, currentLang),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyEventsView(
    paddingValues: PaddingValues,
    currentLang: AppLanguage,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (currentLang == AppLanguage.BN) "কোন ইভেন্ট বা পিকনিক হিসাব নেই" else "No Events Created Yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (currentLang == AppLanguage.BN) "পিকনিক, বিয়ে বা ট্যুরের জন্য সবার চাঁদা কালেকশন ও বাজার খরচ ট্র্যাক করতে নতুন ইভেন্ট শুরু করুন।"
                else "Create your first event to manage collections, expenses and download receipts.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onCreateClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (currentLang == AppLanguage.BN) "নতুন ইভেন্ট খুলুন" else "Create New Event")
            }
        }
    }
}

@Composable
private fun CreateEventDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onCreate: (title: String, type: String, budget: Double, organizer: String, location: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PICNIC") }
    var budgetStr by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val eventTypes = listOf(
        "PICNIC" to "পিকনিক",
        "WEDDING" to "বিবাহ/গায়ে হলুদ",
        "TOUR" to "ট্যুর/ভ্রমণ",
        "MESS" to "মেস/ব্যাচেলর",
        "OTHER" to "অন্যান্য অনুষ্ঠান"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentLang == AppLanguage.BN) "নতুন ইভেন্ট / পিকনিক হিসাব" else "New Event Planner",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type selector chips
                Text(
                    text = "ইভেন্টের ধরন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    eventTypes.take(3).forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                if (title.isBlank()) {
                                    title = when (type) {
                                        "PICNIC" -> "বার্ষিক পিকনিক"
                                        "WEDDING" -> "বিবাহ ও গায়ে হলুদ"
                                        "TOUR" -> "সাজেক ট্যুর"
                                        else -> ""
                                    }
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    eventTypes.drop(3).forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ইভেন্টের নাম (যেমন: কক্সবাজার ট্যুর বা পারিবারিক পিকনিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("বাজেট লক্ষ্যমাত্রা (টাকা, ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = organizer,
                    onValueChange = { organizer = it },
                    label = { Text("আয়োজক / ক্যাশিয়ার নাম (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("স্থান / ভেন্যু (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = BengaliNumberUtils.toEnglishDigits(budgetStr.trim()).toDoubleOrNull() ?: 0.0
                    onCreate(
                        title.ifBlank { "পিকনিক ও ইভেন্ট হিসাব" },
                        selectedType,
                        budget,
                        organizer,
                        location,
                        ""
                    )
                }
            ) {
                Text("শুরু করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

fun getEventTypeDetails(type: String): Triple<String, ImageVector, Color> {
    return when (type) {
        "PICNIC" -> Triple("পিকনিক", Icons.Default.Celebration, Color(0xFF2E7D32))
        "WEDDING" -> Triple("বিবাহ অনুষ্ঠান", Icons.Default.VolunteerActivism, Color(0xFFC2185B))
        "TOUR" -> Triple("ট্যুর ও ভ্রমণ", Icons.Default.FlightTakeoff, Color(0xFF0288D1))
        "MESS" -> Triple("মেস / ডাইনিং", Icons.Default.Restaurant, Color(0xFFF57C00))
        else -> Triple("গ্রুপ ইভেন্ট", Icons.Default.Groups, Color(0xFF512DA8))
    }
}
