package com.example.ui.lists

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ShoppingHistoryEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppMode
import com.example.ui.components.BazariTopBar
import com.example.ui.components.BudgetSummaryCard
import com.example.ui.components.CelebrationDialog
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.EditItemDialog
import com.example.ui.components.ExportDialog
import com.example.ui.components.QuickAddChips
import com.example.ui.components.ShoppingListItemCard
import com.example.ui.components.SmartInputBar
import com.example.utils.AdMobManager
import com.example.utils.BengaliNumberUtils
import com.example.utils.L10n
import com.example.utils.LanguageManager

@Composable
fun ListDetailScreen(
    listId: String,
    viewModel: ListViewModel,
    onBackClick: () -> Unit,
    onNavigateToCatalog: (String) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    LaunchedEffect(listId) {
        viewModel.setListId(listId)
    }

    val currentList by viewModel.currentList.collectAsState()
    val items by viewModel.items.collectAsState()
    val commonProducts by viewModel.commonProducts.collectAsState()

    var isShoppingMode by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingItemEntity?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var celebrationHistory by remember { mutableStateOf<ShoppingHistoryEntity?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.smartQuickEntry(spokenText)
            }
        }
    }

    val unboughtItems = items.filter { !it.isBought }
    val boughtItems = items.filter { it.isBought }

    var totalCost = 0.0
    var boughtCost = 0.0

    items.forEach { item ->
        val qtyNum = BengaliNumberUtils.toEnglishDigits(item.quantity).toDoubleOrNull() ?: 1.0
        val lineCost = qtyNum * item.unitPrice
        totalCost += lineCost
        if (item.isBought) {
            boughtCost += lineCost
        }
    }

    // Dialogs
    editingItem?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                viewModel.updateItem(updated)
                editingItem = null
            },
            onDelete = {
                viewModel.deleteItem(item.id)
                editingItem = null
            }
        )
    }

    if (showBudgetDialog) {
        EditBudgetDialog(
            currentBudget = currentList?.budget ?: 0.0,
            onDismiss = { showBudgetDialog = false },
            onSave = { newBudget ->
                showBudgetDialog = false
                viewModel.updateBudget(newBudget)
            }
        )
    }

    if (showExportDialog && currentList != null) {
        ExportDialog(
            context = context,
            list = currentList!!,
            items = items,
            currentLang = currentLang,
            onDismiss = { showExportDialog = false }
        )
    }

    if (showSaveTemplateDialog) {
        var tplTitle by remember { mutableStateOf(currentList?.title ?: "") }
        var tplDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            title = {
                Text(
                    L10n.saveAsTemplate(currentLang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.BN)
                            "এই ফর্দের সব পণ্য দিয়ে ভবিষ্যতে ১-ট্যাপে ব্যবহারের জন্য একটি কাস্টম টেমপ্লেট তৈরি করুন।"
                        else
                            "Create a reusable template from this list's items for 1-tap list creation in the future.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    OutlinedTextField(
                        value = tplTitle,
                        onValueChange = { tplTitle = it },
                        label = { Text(if (currentLang == AppLanguage.BN) "টেমপ্লেটের নাম" else "Template Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tplDesc,
                        onValueChange = { tplDesc = it },
                        label = { Text(if (currentLang == AppLanguage.BN) "বিবরণ (ঐচ্ছিক)" else "Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveTemplateDialog = false
                        viewModel.saveAsTemplate(tplTitle, tplDesc) {}
                    }
                ) {
                    Text(L10n.saveButton(currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTemplateDialog = false }) {
                    Text(L10n.cancelButton(currentLang))
                }
            }
        )
    }

    celebrationHistory?.let { hist ->
        CelebrationDialog(
            history = hist,
            onDismiss = {
                celebrationHistory = null
                (context as? Activity)?.let { activity ->
                    AdMobManager.showInterstitialAd(activity) {
                        onBackClick()
                    }
                } ?: onBackClick()
            },
            onViewHistory = {
                celebrationHistory = null
                (context as? Activity)?.let { activity ->
                    AdMobManager.showInterstitialAd(activity) {
                        onNavigateToHistory()
                    }
                } ?: onNavigateToHistory()
            }
        )
    }

    // IF IN SHOPPING MODE (Fast, Clean, Large Touch Checklist)
    if (isShoppingMode) {
        ShoppingModeView(
            listTitle = currentList?.title ?: L10n.appName(currentLang),
            items = items,
            unboughtItems = unboughtItems,
            boughtItems = boughtItems,
            totalCost = totalCost,
            boughtCost = boughtCost,
            currentLang = currentLang,
            onToggleBought = { viewModel.toggleItemBought(it) },
            onExitShoppingMode = { isShoppingMode = false },
            onCompleteShopping = {
                viewModel.completeShoppingTrip { hist ->
                    celebrationHistory = hist
                }
            }
        )
        return
    }

    // NORMAL DETAIL VIEW
    Scaffold(
        topBar = {
            BazariTopBar(
                title = currentList?.title ?: (if (currentLang == AppLanguage.BN) "ফর্দ বিবরণ" else "List Details"),
                mode = if (currentList?.mode == "STORE") AppMode.STORE else AppMode.PERSONAL,
                showBackButton = true,
                onBackClick = onBackClick,
                showLanguageToggle = true,
                actions = {
                    // Shopping Mode toggle button
                    IconButton(
                        onClick = { isShoppingMode = true },
                        modifier = Modifier.size(34.dp).testTag("enter_shopping_mode_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = L10n.shoppingMode(currentLang),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Export & Share
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.size(34.dp).testTag("export_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = if (currentLang == AppLanguage.BN) "এক্সপোর্ট ও শেয়ার" else "Export & Share",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(34.dp).testTag("list_detail_more_btn")
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = if (currentLang == AppLanguage.BN) "আরও অপশন" else "More options",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(L10n.shoppingMode(currentLang)) },
                                onClick = {
                                    menuExpanded = false
                                    isShoppingMode = true
                                },
                                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (currentLang == AppLanguage.BN) "বাজেট পরিবর্তন করুন" else "Change Budget") },
                                onClick = {
                                    menuExpanded = false
                                    showBudgetDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text(L10n.saveAsTemplate(currentLang)) },
                                onClick = {
                                    menuExpanded = false
                                    showSaveTemplateDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                            if (boughtItems.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == AppLanguage.BN) "কেনা আইটেমগুলো ক্লিয়ার করুন" else "Clear Bought Items") },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.clearBoughtItems()
                                    },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { onNavigateToCatalog(listId) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp).testTag("open_catalog_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = if (currentLang == AppLanguage.BN) "ক্যাটালগ খুলুন" else "Open Catalog",
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (items.isNotEmpty() && currentList?.isCompleted == false) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.completeShoppingTrip { hist ->
                                celebrationHistory = hist
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("complete_shopping_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = L10n.completeShoppingBtn(currentLang),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Budget Summary Card
            item {
                BudgetSummaryCard(
                    totalEstimatedCost = totalCost,
                    boughtCost = boughtCost,
                    budget = currentList?.budget ?: 0.0,
                    totalItems = items.size,
                    boughtItems = boughtItems.size,
                    onEditBudgetClick = { showBudgetDialog = true }
                )
            }

            // Smart Quick Input Bar with Voice Support
            item {
                SmartInputBar(
                    onAddItem = { text -> viewModel.smartQuickEntry(text) },
                    onProductSelected = { product -> viewModel.quickAddProduct(product) },
                    onSearchSuggestions = { query -> viewModel.searchSuggestions(query) },
                    onOpenCatalog = { onNavigateToCatalog(listId) },
                    onVoiceInput = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (currentLang == AppLanguage.BN) "bn-BD" else "en-US")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, L10n.voiceListening(currentLang))
                            }
                            voiceLauncher.launch(intent)
                        } catch (e: Exception) {
                            // Handled silently
                        }
                    }
                )
            }

            // Quick Add Chips
            if (commonProducts.isNotEmpty()) {
                item {
                    QuickAddChips(
                        commonProducts = commonProducts,
                        onProductClick = { product -> viewModel.quickAddProduct(product) },
                        onSeeMoreClick = { onNavigateToCatalog(listId) }
                    )
                }
            }

            // Unbought Items Header
            if (unboughtItems.isNotEmpty()) {
                val unboughtCost = (totalCost - boughtCost).coerceAtLeast(0.0)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${L10n.remainingToBuy(currentLang)} (${L10n.itemsCount(unboughtItems.size, currentLang)})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = L10n.price(unboughtCost, currentLang),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                items(unboughtItems, key = { it.id }) { item ->
                    ShoppingListItemCard(
                        item = item,
                        onToggleBought = { viewModel.toggleItemBought(item) },
                        onEditItem = { editingItem = item },
                        onDeleteItem = { viewModel.deleteItem(item.id) }
                    )
                }
            }

            // Bought Items Header
            if (boughtItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${L10n.checkedTotalTitle(currentLang)} (${L10n.itemsCount(boughtItems.size, currentLang)})",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = L10n.price(boughtCost, currentLang),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = L10n.clearBought(currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier
                                .clickable { viewModel.clearBoughtItems() }
                                .padding(4.dp)
                        )
                    }
                }

                items(boughtItems, key = { it.id }) { item ->
                    ShoppingListItemCard(
                        item = item,
                        onToggleBought = { viewModel.toggleItemBought(item) },
                        onEditItem = { editingItem = item },
                        onDeleteItem = { viewModel.deleteItem(item.id) }
                    )
                }
            }

            // Empty state
            if (items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = L10n.emptyListTitle(currentLang),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = L10n.emptyListDesc(currentLang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                            )
                            Surface(
                                onClick = { onNavigateToCatalog(listId) },
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = L10n.pickFromCatalog(currentLang),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SHOPPING MODE (Active in-store streamlined UI) ---

@Composable
fun ShoppingModeView(
    listTitle: String,
    items: List<ShoppingItemEntity>,
    unboughtItems: List<ShoppingItemEntity>,
    boughtItems: List<ShoppingItemEntity>,
    totalCost: Double = 0.0,
    boughtCost: Double = 0.0,
    currentLang: AppLanguage,
    onToggleBought: (ShoppingItemEntity) -> Unit,
    onExitShoppingMode: () -> Unit,
    onCompleteShopping: () -> Unit
) {
    val totalCount = items.size
    val boughtCount = boughtItems.size
    val progress = if (totalCount > 0) boughtCount.toFloat() / totalCount.toFloat() else 0f
    val unboughtCost = (totalCost - boughtCost).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onExitShoppingMode,
                                modifier = Modifier.size(36.dp).testTag("exit_shopping_mode_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = L10n.exitShoppingMode(currentLang),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = listTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = L10n.shoppingModeActive(currentLang),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Progress Pill & Checked Total
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.BN) {
                                        "${L10n.digits(boughtCount, currentLang)} / ${L10n.digits(totalCount, currentLang)} কেনা"
                                    } else {
                                        "$boughtCount / $totalCount Done"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (totalCost > 0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${L10n.price(boughtCost, currentLang)} / ${L10n.price(totalCost, currentLang)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.onPrimary,
                        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onExitShoppingMode,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = L10n.exitShoppingMode(currentLang),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Button(
                        onClick = onCompleteShopping,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("shopping_mode_complete_btn")
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = L10n.completeShoppingBtn(currentLang),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (unboughtItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${L10n.remainingToBuy(currentLang)} (${L10n.itemsCount(unboughtItems.size, currentLang)})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = L10n.price(unboughtCost, currentLang),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                items(unboughtItems, key = { it.id }) { item ->
                    ShoppingModeItemRow(
                        item = item,
                        currentLang = currentLang,
                        onClick = { onToggleBought(item) }
                    )
                }
            }

            if (boughtItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${L10n.checkedTotalTitle(currentLang)} (${L10n.itemsCount(boughtItems.size, currentLang)})",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = L10n.price(boughtCost, currentLang),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                items(boughtItems, key = { it.id }) { item ->
                    ShoppingModeItemRow(
                        item = item,
                        currentLang = currentLang,
                        onClick = { onToggleBought(item) }
                    )
                }
            }

            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = L10n.emptyListTitle(currentLang),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingModeItemRow(
    item: ShoppingItemEntity,
    currentLang: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onClick() }
            .testTag("shopping_mode_row_${item.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isBought) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (item.isBought) MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isBought,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nameBn,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (item.isBought) FontWeight.Normal else FontWeight.SemiBold,
                        color = if (item.isBought) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.brand.isNotBlank()) {
                    Text(
                        text = item.brand,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (item.isBought) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Text(
                    text = L10n.quantityWithUnit(item.quantity, item.unit, currentLang),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (item.isBought) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
