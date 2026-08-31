package com.example.ui.events

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.EventExpenseEntity
import com.example.data.local.entity.EventMemberEntity
import com.example.data.repository.EventInvoiceSummary
import com.example.domain.model.AppLanguage
import com.example.utils.BengaliNumberUtils
import com.example.utils.L10n
import com.example.utils.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onNavigateToList: (String) -> Unit,
    viewModel: EventDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    val event by viewModel.event.collectAsState()
    val members by viewModel.members.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val invoiceSummary by viewModel.invoiceSummary.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showBatchMemberDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("লোড হচ্ছে...")
        }
        return
    }

    val ev = event!!

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = ev.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (ev.organizerName.isNotBlank()) "আয়োজক: ${ev.organizerName}" else "পিকনিক ও গ্রুপ হিসাব",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showInvoiceDialog = true },
                        modifier = Modifier.testTag("btn_show_event_invoice")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "ইনভয়েস মেমো",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = { showAddMemberDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_event_member")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "সদস্য যোগ করুন")
                }
            } else if (selectedTabIndex == 2) {
                FloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_event_expense")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "খরচ/বাজার যোগ করুন")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("সার্বিক হিসাব", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("চাঁদা ও সদস্য (${members.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("বাজার ও খরচ (${expenses.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            when (selectedTabIndex) {
                0 -> EventOverviewTab(
                    summary = invoiceSummary,
                    currentLang = currentLang,
                    onOpenInvoice = { showInvoiceDialog = true },
                    onConvertToList = {
                        viewModel.convertToShoppingList { newListId ->
                            if (newListId.isNotBlank()) {
                                Toast.makeText(context, "বাজার ফর্দ তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                                onNavigateToList(newListId)
                            } else {
                                Toast.makeText(context, "বাজার ফর্দ তৈরির মতো কোন খরচ নেই", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                1 -> EventMembersTab(
                    members = members,
                    currentLang = currentLang,
                    onAddMemberClick = { showAddMemberDialog = true },
                    onBatchAddClick = { showBatchMemberDialog = true },
                    onPaymentUpdate = { id, isPaid, amount -> viewModel.updateMemberPayment(id, isPaid, amount) },
                    onDeleteMember = { viewModel.deleteMember(it) }
                )
                2 -> EventExpensesTab(
                    expenses = expenses,
                    currentLang = currentLang,
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onToggleBought = { id, isBought -> viewModel.setExpenseBought(id, isBought) },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
            }
        }
    }

    // Invoice Dialog
    if (showInvoiceDialog && invoiceSummary != null) {
        EventInvoiceDialog(
            summary = invoiceSummary!!,
            onDismiss = { showInvoiceDialog = false }
        )
    }

    // Add Single Member Dialog
    if (showAddMemberDialog) {
        AddEventMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAdd = { name, phone, target, paid, isPaid ->
                viewModel.addMember(name, phone, target, paid, isPaid)
                showAddMemberDialog = false
            }
        )
    }

    // Batch Add Members Dialog
    if (showBatchMemberDialog) {
        BatchAddMembersDialog(
            onDismiss = { showBatchMemberDialog = false },
            onAddBatch = { rawNames, defaultTarget ->
                viewModel.addMembersBatch(rawNames, defaultTarget)
                showBatchMemberDialog = false
            }
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddEventExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { title, cat, qty, unit, rate, amount, paidBy ->
                viewModel.addExpense(title, cat, qty, unit, rate, amount, paidBy)
                showAddExpenseDialog = false
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 0: OVERVIEW & ANALYTICS
// -------------------------------------------------------------
@Composable
private fun EventOverviewTab(
    summary: EventInvoiceSummary?,
    currentLang: AppLanguage,
    onOpenInvoice: () -> Unit,
    onConvertToList: () -> Unit
) {
    if (summary == null) return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KPI Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Collection Card
                KpiCard(
                    title = "মোট চাঁদা আদায়",
                    value = L10n.price(summary.totalPaidCollection, currentLang),
                    subtext = "বাকি: ${L10n.price(summary.totalPendingCollection, currentLang)}",
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Total Expenses Card
                KpiCard(
                    title = "মোট খরচ",
                    value = L10n.price(summary.totalExpenses, currentLang),
                    subtext = "${summary.expenses.size}টি খাতে খরচ",
                    icon = Icons.Default.TrendingDown,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Net Balance Banner
        item {
            val net = summary.netBalance
            val isPositive = net >= 0
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPositive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPositive) "উদ্বৃত্ত / হাতে জমা আছে" else "ঘাটতি / অতিরিক্ত খরচ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        )
                        Text(
                            text = if (isPositive) "কালেকশন থেকে খরচের পর অবশিষ্ট টাকা" else "কালেকশনের চেয়ে খরচ বেশি হয়েছে",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Text(
                        text = (if (isPositive) "(+) " else "(-) ") + L10n.price(Math.abs(net), currentLang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }

        // Fast Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenInvoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_overview_invoice"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ইনভয়েস মেমো দেখুন ও শেয়ার করুন (PDF/ছবি)", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onConvertToList,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_overview_convert_fard"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("বাজার খরচকে সরাসরি ফর্দে রূপান্তর করুন")
                }
            }
        }

        // Target Budget progress
        if (summary.event.targetBudget > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("বাজেট লক্ষ্যমাত্রা:", style = MaterialTheme.typography.bodySmall)
                            Text(L10n.price(summary.event.targetBudget, currentLang), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("কালেকশন অগ্রগতি:", style = MaterialTheme.typography.bodySmall)
                            val pct = if (summary.event.targetBudget > 0) (summary.totalPaidCollection / summary.event.targetBudget * 100).toInt() else 0
                            Text("${L10n.digits(pct, currentLang)}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color))
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

// -------------------------------------------------------------
// TAB 1: MEMBERS & COLLECTION
// -------------------------------------------------------------
@Composable
private fun EventMembersTab(
    members: List<EventMemberEntity>,
    currentLang: AppLanguage,
    onAddMemberClick: () -> Unit,
    onBatchAddClick: () -> Unit,
    onPaymentUpdate: (id: String, isPaid: Boolean, amount: Double) -> Unit,
    onDeleteMember: (String) -> Unit
) {
    val totalCollected = members.sumOf { it.paidAmount }
    val totalTarget = members.sumOf { it.targetAmount }
    val paidCount = members.count { it.isPaid }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Actions & Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddMemberClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("সদস্য যোগ", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onBatchAddClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("একসাথে একাধিক", fontSize = 12.sp)
                }
            }
        }

        // Stats strip
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "পরিশোধ করেছে: ${L10n.digits(paidCount, currentLang)}/${L10n.digits(members.size, currentLang)} জন",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "মোট জমা: ${L10n.price(totalCollected, currentLang)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        if (members.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("এখনো কোন সদস্য যুক্ত করা হয়নি", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
        } else {
            items(members, key = { it.id }) { member ->
                MemberItemCard(
                    member = member,
                    currentLang = currentLang,
                    onTogglePaid = {
                        val newPaid = !member.isPaid
                        val newAmount = if (newPaid) member.targetAmount else 0.0
                        onPaymentUpdate(member.id, newPaid, newAmount)
                    },
                    onDelete = { onDeleteMember(member.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun MemberItemCard(
    member: EventMemberEntity,
    currentLang: AppLanguage,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (member.isPaid) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                if (member.phone.isNotBlank()) {
                    Text(member.phone, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("চাঁদা: ${L10n.price(member.targetAmount, currentLang)}", style = MaterialTheme.typography.bodySmall)
                    Text("• জমা: ${L10n.price(member.paidAmount, currentLang)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onTogglePaid,
                    shape = RoundedCornerShape(8.dp),
                    color = if (member.isPaid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (member.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (member.isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (member.isPaid) "পরিশোধ" else "বাকি",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (member.isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: BAZAAR & EXPENSES
// -------------------------------------------------------------
@Composable
private fun EventExpensesTab(
    expenses: List<EventExpenseEntity>,
    currentLang: AppLanguage,
    onAddExpenseClick: () -> Unit,
    onToggleBought: (id: String, isBought: Boolean) -> Unit,
    onDeleteExpense: (String) -> Unit
) {
    val totalExpenses = expenses.sumOf { it.amount }
    val boughtCount = expenses.count { it.isBought }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Action & Summary
        item {
            Button(
                onClick = onAddExpenseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("নতুন বাজার / খরচ এন্ট্রি করুন", fontSize = 13.sp)
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কেনা হয়েছে: ${L10n.digits(boughtCount, currentLang)}/${L10n.digits(expenses.size, currentLang)}টি",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "মোট খরচ: ${L10n.price(totalExpenses, currentLang)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    )
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("এখনো কোন বাজার বা খরচের হিসাব নেই", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    currentLang = currentLang,
                    onToggleBought = { onToggleBought(expense.id, !expense.isBought) },
                    onDelete = { onDeleteExpense(expense.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun ExpenseItemCard(
    expense: EventExpenseEntity,
    currentLang: AppLanguage,
    onToggleBought: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = expense.isBought,
                onCheckedChange = { onToggleBought() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (expense.unitPrice > 0) {
                        Text(
                            text = "${expense.quantity} ${expense.unit} × ${L10n.price(expense.unitPrice, currentLang)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    if (expense.paidBy.isNotBlank() && expense.paidBy != "কমন ফান্ড") {
                        Text(
                            text = "[পরিশোধ: ${expense.paidBy}]",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                        )
                    }
                }
            }

            Text(
                text = L10n.price(expense.amount, currentLang),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOGS: ADD MEMBER, BATCH MEMBERS, ADD EXPENSE
// -------------------------------------------------------------
@Composable
private fun AddEventMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, target: Double, paid: Double, isPaid: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var isPaid by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন সদস্য যোগ করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("সদস্যের নাম (যেমন: তানভীর)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("চাঁদার পরিমাণ (টাকা, যেমন: ৫০০)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPaid = !isPaid },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isPaid, onCheckedChange = { isPaid = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("চাঁদা ইতিমধ্যেই পরিশোধ করেছে")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = BengaliNumberUtils.toEnglishDigits(targetStr.trim()).toDoubleOrNull() ?: 0.0
                    val paid = if (isPaid) target else 0.0
                    onAdd(name.ifBlank { "সদস্য" }, phone, target, paid, isPaid)
                }
            ) {
                Text("যুক্ত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
private fun BatchAddMembersDialog(
    onDismiss: () -> Unit,
    onAddBatch: (rawNames: String, defaultTarget: Double) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var defaultTargetStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("একসাথে একাধিক সদস্য যুক্ত করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "প্রতি লাইনে বা কমা দিয়ে নাম লিখুন। যেমন:\nরাকিব\nতানভীর ৫০০\nসাকিব",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = defaultTargetStr,
                    onValueChange = { defaultTargetStr = it },
                    label = { Text("সবার ডিফল্ট চাঁদা (টাকা)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("সদস্যদের নামের তালিকা") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val defTarget = BengaliNumberUtils.toEnglishDigits(defaultTargetStr.trim()).toDoubleOrNull() ?: 0.0
                    onAddBatch(rawText, defTarget)
                }
            ) {
                Text("যুক্ত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
private fun AddEventExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, qty: String, unit: String, rate: Double, amount: Double, paidBy: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("BAZAAR") }
    var qtyStr by remember { mutableStateOf("১") }
    var unitStr by remember { mutableStateOf("কেজি") }
    var rateStr by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("কমন ফান্ড") }

    val categories = listOf(
        "BAZAAR" to "বাজার",
        "FOOD" to "খাবার",
        "TRANSPORT" to "যাতায়াত",
        "VENUE" to "ভেন্যু/ডেকোরেশন",
        "OTHER" to "অন্যান্য"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন খরচ বা বাজার যুক্ত করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Category Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { (cat, label) ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.drop(3).forEach { (cat, label) ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("খরচ / আইটেমের নাম (যেমন: মুরগির মাংস)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("পরিমাণ") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitStr,
                        onValueChange = { unitStr = it },
                        label = { Text("একক") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it },
                        label = { Text("দর (প্রতি একক)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("মোট টাকা") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = paidBy,
                    onValueChange = { paidBy = it },
                    label = { Text("কে পরিশোধ করেছে? (যেমন: কমন ফান্ড / তানভীর)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = BengaliNumberUtils.toEnglishDigits(rateStr.trim()).toDoubleOrNull() ?: 0.0
                    val amount = BengaliNumberUtils.toEnglishDigits(amountStr.trim()).toDoubleOrNull() ?: (rate * (BengaliNumberUtils.toEnglishDigits(qtyStr.trim()).toDoubleOrNull() ?: 1.0))
                    onAdd(title.ifBlank { "বাজার খরচ" }, selectedCategory, qtyStr, unitStr, rate, amount, paidBy)
                }
            ) {
                Text("যুক্ত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
