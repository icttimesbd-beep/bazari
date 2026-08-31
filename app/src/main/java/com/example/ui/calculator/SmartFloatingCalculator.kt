package com.example.ui.calculator

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.QuickTapeNoteEntity
import com.example.domain.model.AppLanguage
import com.example.utils.ExportManager
import com.example.utils.L10n
import com.example.utils.LanguageManager
import com.example.utils.MathExpressionEvaluator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartFloatingCalculator(
    onNavigateToList: (String) -> Unit = {},
    viewModel: TapeCalculatorViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tapeEntries by viewModel.tapeEntries.collectAsState()
    val currentInput by viewModel.currentInput.collectAsState()
    val currentLabel by viewModel.currentLabel.collectAsState()
    val currentOp by viewModel.currentOp.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()
    val savedNotes by viewModel.savedNotes.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNoteTitle by remember { mutableStateOf("") }
    var savePersonName by remember { mutableStateOf("") }
    var saveNoteType by remember { mutableStateOf("CALC_TAPE") }

    // Floating Button
    FloatingActionButton(
        onClick = { isSheetOpen = true },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .testTag("floating_tape_calculator_btn")
            .shadow(6.dp, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = "ক্যালকুলেটর ও হিসাব টেপ",
            modifier = Modifier.size(26.dp)
        )
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.testTag("tape_calculator_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header with Tab Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.BN) "বাজার হিসাব ও ডিজিটাল টেপ" else "Market Tape & Quick Calc",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = if (currentLang == AppLanguage.BN) "আইটেমের গুন-ভাগ, মোট যোগফল ও বাকি নোট" else "Item formulas (×/÷), total sum & dues",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = { isSheetOpen = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "লাইভ টেপ (${tapeEntries.size})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "বাকি ও নোট খাতা (${savedNotes.size})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // TAB 0: LIVE TAPE CALCULATOR
                    LiveTapeCalculatorContent(
                        tapeEntries = tapeEntries,
                        currentInput = currentInput,
                        currentLabel = currentLabel,
                        currentOp = currentOp,
                        grandTotal = grandTotal,
                        currentLang = currentLang,
                        onDigit = { viewModel.onDigit(it) },
                        onLabelChange = { viewModel.onLabelChange(it) },
                        onOpChange = { viewModel.onOpChange(it) },
                        onEquals = { viewModel.onEquals() },
                        onPush = { viewModel.pushCurrentEntry() },
                        onClear = { viewModel.onClear() },
                        onBackspace = { viewModel.onBackspace() },
                        onRemoveEntry = { viewModel.removeEntry(it) },
                        onOpenSaveDialog = {
                            saveNoteTitle = ""
                            savePersonName = ""
                            saveNoteType = "CALC_TAPE"
                            showSaveDialog = true
                        },
                        onConvertToShoppingList = {
                            viewModel.convertTapeToShoppingList { newListId ->
                                isSheetOpen = false
                                onNavigateToList(newListId)
                            }
                        }
                    )
                } else {
                    // TAB 1: SAVED DUES & NOTES LEDGER
                    SavedDuesLedgerContent(
                        savedNotes = savedNotes,
                        currentLang = currentLang,
                        onToggleSettled = { viewModel.toggleNoteSettled(it) },
                        onDelete = { viewModel.deleteNote(it.id) },
                        onConvertNoteToList = { note ->
                            viewModel.saveCurrentTapeAsNote(note.title, note.noteType, note.personName) {}
                            isSheetOpen = false
                        }
                    )
                }
            }
        }
    }

    // Save Tape Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text("হিসাব সংরক্ষণ করুন", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "মোট যোগফল: ${L10n.price(grandTotal, currentLang)} (${tapeEntries.size}টি এন্ট্রি)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { saveNoteType = "CALC_TAPE" },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (saveNoteType == "CALC_TAPE") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("সাধারণ হিসাব", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { saveNoteType = "DUES_NOTE" },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (saveNoteType == "DUES_NOTE") MaterialTheme.colorScheme.errorContainer else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাকি হিসাব", fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = saveNoteTitle,
                        onValueChange = { saveNoteTitle = it },
                        label = { Text("হিসাবের শিরোনাম (যেমন: মুদি বাজার)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (saveNoteType == "DUES_NOTE") {
                        OutlinedTextField(
                            value = savePersonName,
                            onValueChange = { savePersonName = it },
                            label = { Text("কার কাছে বাকি / নাম (যেমন: করিম ভাই)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentTapeAsNote(
                            title = saveNoteTitle,
                            noteType = saveNoteType,
                            personName = savePersonName
                        ) {
                            showSaveDialog = false
                            selectedTab = 1
                            Toast.makeText(context, "হিসাব সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("সেভ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun LiveTapeCalculatorContent(
    tapeEntries: List<TapeEntry>,
    currentInput: String,
    currentLabel: String,
    currentOp: String,
    grandTotal: Double,
    currentLang: AppLanguage,
    onDigit: (String) -> Unit,
    onLabelChange: (String) -> Unit,
    onOpChange: (String) -> Unit,
    onEquals: () -> Unit,
    onPush: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onRemoveEntry: (Int) -> Unit,
    onOpenSaveDialog: () -> Unit,
    onConvertToShoppingList: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(tapeEntries.size) {
        if (tapeEntries.isNotEmpty()) {
            listState.animateScrollToItem(tapeEntries.size - 1)
        }
    }

    val liveEvaluated = remember(currentInput) {
        if (currentInput.isNotBlank()) MathExpressionEvaluator.evaluate(currentInput) else null
    }
    val hasFormula = remember(currentInput) {
        MathExpressionEvaluator.hasOperator(currentInput)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Digital Paper Tape Display
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                // Tape list
                if (tapeEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোন হিসাব যোগ করা হয়নি।\nযেমন: 'আলু' লিখে ৪৫ × ৫ টাইপ করে 'যোগ (+)' চাপুন।\nপ্রতিটি আইটেমের হিসাব বের হয়ে মোট যোগ হবে।",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(tapeEntries) { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${L10n.digits(idx + 1, currentLang)}. ${item.label}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.expression.isNotBlank()) {
                                        Text(
                                            text = "(${L10n.digits(item.expression, currentLang)})",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val opSymbol = if (item.op == "-") "- " else "+ "
                                    Text(
                                        text = "$opSymbol${L10n.price(item.amount, currentLang)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.op == "-") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    Text(
                                        text = "= ${L10n.price(item.runningTotal, currentLang)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    IconButton(
                                        onClick = { onRemoveEntry(idx) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Grand Total Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সর্বমোট যোগফল:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = L10n.price(grandTotal, currentLang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input row: Item Name + Amount/Formula Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentLabel,
                onValueChange = onLabelChange,
                placeholder = { Text("আইটেমের নাম (যেমন: রুই মাছ)", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1.1f)
                    .height(52.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Current Input display box with live expression result
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Line sign badge (+ or -)
                    Surface(
                        color = if (currentOp == "-") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable {
                            onOpChange(if (currentOp == "+") "-" else "+")
                        }
                    ) {
                        Text(
                            text = currentOp,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (currentOp == "-") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentInput.isNotBlank()) L10n.digits(currentInput, currentLang) else "0",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = if (currentInput.length > 10) 12.sp else 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasFormula && liveEvaluated != null) {
                            Text(
                                text = "= ${L10n.price(liveEvaluated, currentLang)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Keypad Grid & Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: 7, 8, 9, ÷, C
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcButton("7", Modifier.weight(1f)) { onDigit("7") }
                CalcButton("8", Modifier.weight(1f)) { onDigit("8") }
                CalcButton("9", Modifier.weight(1f)) { onDigit("9") }
                CalcButton("÷", Modifier.weight(1f), isOp = true) { onOpChange("÷") }
                CalcButton("C", Modifier.weight(1f), isAction = true) { onClear() }
            }

            // Row 2: 4, 5, 6, ×, ⌫
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcButton("4", Modifier.weight(1f)) { onDigit("4") }
                CalcButton("5", Modifier.weight(1f)) { onDigit("5") }
                CalcButton("6", Modifier.weight(1f)) { onDigit("6") }
                CalcButton("×", Modifier.weight(1f), isOp = true) { onOpChange("×") }
                CalcIconButton(Icons.Default.Backspace, Modifier.weight(1f), isAction = true) { onBackspace() }
            }

            // Row 3: 1, 2, 3, -, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcButton("1", Modifier.weight(1f)) { onDigit("1") }
                CalcButton("2", Modifier.weight(1f)) { onDigit("2") }
                CalcButton("3", Modifier.weight(1f)) { onDigit("3") }
                CalcButton("-", Modifier.weight(1f), isOp = true) { onOpChange("-") }
                CalcButton("+", Modifier.weight(1f), isOp = true) { onOpChange("+") }
            }

            // Row 4: 0, 00, ., =, + যোগ করুন
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcButton("0", Modifier.weight(1f)) { onDigit("0") }
                CalcButton("00", Modifier.weight(1f)) { onDigit("00") }
                CalcButton(".", Modifier.weight(0.8f)) { onDigit(".") }
                CalcButton("=", Modifier.weight(1f), isOp = true) { onEquals() }
                Button(
                    onClick = onPush,
                    modifier = Modifier
                        .weight(1.8f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("+ যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Conversion & Save Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onConvertToShoppingList,
                enabled = tapeEntries.isNotEmpty(),
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("btn_convert_tape_to_list"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("বাজার ফর্দে রূপান্তর", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onOpenSaveDialog,
                enabled = tapeEntries.isNotEmpty() || grandTotal > 0,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_save_tape_note"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("নোট/বাকি সেভ", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SavedDuesLedgerContent(
    savedNotes: List<QuickTapeNoteEntity>,
    currentLang: AppLanguage,
    onToggleSettled: (QuickTapeNoteEntity) -> Unit,
    onDelete: (QuickTapeNoteEntity) -> Unit,
    onConvertNoteToList: (QuickTapeNoteEntity) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    if (savedNotes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "কোন সংরক্ষিত বাকি বা হিসাব নোট নেই।",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "ক্যালকুলেটরে হিসাব করে 'নোট/বাকি সেভ' চাপুন।",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(savedNotes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (note.isSettled) MaterialTheme.colorScheme.surfaceContainerLow
                        else if (note.noteType == "DUES_NOTE") MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (note.noteType == "DUES_NOTE") MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (note.noteType == "DUES_NOTE") "বাকি হিসাব" else "হিসাব নোট",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (note.noteType == "DUES_NOTE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    if (note.personName.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${note.personName}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Text(
                                text = L10n.price(note.totalSum, currentLang),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (note.isSettled) MaterialTheme.colorScheme.onSurfaceVariant
                                    else if (note.noteType == "DUES_NOTE") MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (note.rawEntriesText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = note.rawEntriesText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Settle toggle button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleSettled(note) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (note.isSettled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (note.isSettled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (note.isSettled) "পরিশোধিত" else "বাকি রয়েছে",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (note.isSettled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val text = "${note.title}\n${note.personName}\n${note.rawEntriesText}\nমোট: ${note.totalSum} ৳"
                                        clipboard.setText(AnnotatedString(text))
                                        Toast.makeText(context, "কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = { onDelete(note) },
                                    modifier = Modifier.size(32.dp)
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
}

@Composable
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    isOp: Boolean = false,
    isAction: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = when {
            isAction -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            isOp -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        modifier = modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isAction -> MaterialTheme.colorScheme.error
                        isOp -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            )
        }
    }
}

@Composable
private fun CalcIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isAction: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isAction) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAction) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
