package com.example.ui.home

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.data.local.entity.TemplateEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppMode
import com.example.ui.calculator.SmartFloatingCalculator
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.utils.TemplateImageProvider
import com.example.ui.components.AdMobBanner
import com.example.ui.components.BazariBottomBar
import com.example.ui.components.BazariTopBar
import com.example.ui.components.CreateListDialog
import com.example.ui.components.SmartInputBar
import com.example.utils.L10n
import com.example.utils.LanguageManager

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToList: (String) -> Unit,
    onNavigateToCatalog: (String) -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToTemplateDetail: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val appMode by viewModel.appMode.collectAsState()
    val activeLists by viewModel.activeLists.collectAsState()
    val primarySummary by viewModel.primaryListSummary.collectAsState()
    val commonProducts by viewModel.commonProducts.collectAsState()
    val popularTemplates by viewModel.popularTemplates.collectAsState()
    val smartSuggestion by viewModel.smartSuggestion.collectAsState()
    val dismissedSuggestion by viewModel.dismissedSuggestion.collectAsState()
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // Voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.smartQuickEntryToLatestList(spokenText) { listId ->
                    onNavigateToList(listId)
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            initialMode = appMode,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, mode, budget ->
                showCreateDialog = false
                viewModel.createList(title, mode, budget) { listId ->
                    onNavigateToList(listId)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            BazariTopBar(
                title = L10n.appName(currentLang),
                mode = appMode,
                onToggleMode = { viewModel.toggleAppMode() },
                showLanguageToggle = true
            )
        },
        bottomBar = {
            BazariBottomBar(
                currentRoute = "home",
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmartFloatingCalculator(
                    onNavigateToList = onNavigateToList
                )

                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_new_list_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (appMode == AppMode.STORE) L10n.newStoreList(currentLang) else L10n.newList(currentLang),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
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
            contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp)
        ) {
            // 1. SMART QUICK INPUT BAR
            item {
                SmartInputBar(
                    onAddItem = { text ->
                        viewModel.smartQuickEntryToLatestList(text) { listId ->
                            onNavigateToList(listId)
                        }
                    },
                    onProductSelected = { product ->
                        viewModel.quickAddProductToLatestList(product) { listId ->
                            onNavigateToList(listId)
                        }
                    },
                    onSearchSuggestions = { query ->
                        viewModel.searchSuggestions(query)
                    },
                    onOpenCatalog = {
                        val latestId = activeLists.firstOrNull { !it.isCompleted }?.id ?: "new"
                        if (latestId == "new") {
                            val defaultTitle = if (appMode == AppMode.STORE) L10n.defaultStoreListName(currentLang) else L10n.defaultPersonalListName(currentLang)
                            viewModel.createList(defaultTitle, appMode, 0.0) { id ->
                                onNavigateToCatalog(id)
                            }
                        } else {
                            onNavigateToCatalog(latestId)
                        }
                    },
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

            // 2. TODAY'S LIST (আজকের ফর্দ) COMPACT SUMMARY
            item {
                val primaryList = primarySummary.list
                if (primaryList != null) {
                    CompactTodayListCard(
                        list = primaryList,
                        totalItems = primarySummary.totalItems,
                        boughtItems = primarySummary.boughtItems,
                        pendingItems = primarySummary.pendingItems,
                        estimatedCost = primarySummary.estimatedCost,
                        currentLang = currentLang,
                        onOpenList = { onNavigateToList(primaryList.id) },
                        onAddProduct = { onNavigateToCatalog(primaryList.id) }
                    )
                } else {
                    EmptyTodayListCard(
                        mode = appMode,
                        currentLang = currentLang,
                        onCreateList = { showCreateDialog = true }
                    )
                }
            }

            // 3. SMART REPEAT SUGGESTION BANNER (Subtle & dismissable)
            if (smartSuggestion != null && !dismissedSuggestion) {
                item {
                    SmartSuggestionBanner(
                        suggestion = smartSuggestion!!,
                        currentLang = currentLang,
                        onAdd = {
                            viewModel.addSuggestionToActiveList(smartSuggestion!!) { listId ->
                                onNavigateToList(listId)
                            }
                        },
                        onDismiss = { viewModel.dismissSmartSuggestion() }
                    )
                }
            }

            // 4. QUICK ADD CHIPS (দ্রুত যোগ)
            if (commonProducts.isNotEmpty()) {
                item {
                    CompactQuickAddRow(
                        products = commonProducts,
                        currentLang = currentLang,
                        onProductClick = { product ->
                            viewModel.quickAddProductToLatestList(product)
                        },
                        onOpenCatalog = {
                            val latestId = activeLists.firstOrNull { !it.isCompleted }?.id ?: "new"
                            if (latestId == "new") {
                                val defaultTitle = if (appMode == AppMode.STORE) L10n.defaultStoreListName(currentLang) else L10n.defaultPersonalListName(currentLang)
                                viewModel.createList(defaultTitle, appMode, 0.0) { id ->
                                    onNavigateToCatalog(id)
                                }
                            } else {
                                onNavigateToCatalog(latestId)
                            }
                        }
                    )
                }
            }

            // 5. MY LISTS (আমার ফর্দ)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appMode == AppMode.STORE) L10n.storeListsHeader(currentLang) else L10n.yourListsHeader(currentLang),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    )
                    Text(
                        text = L10n.listsCount(activeLists.size, currentLang),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            if (activeLists.isEmpty()) {
                item {
                    EmptyListsCard(
                        mode = appMode,
                        currentLang = currentLang,
                        onCreateClick = { showCreateDialog = true }
                    )
                }
            } else {
                items(activeLists, key = { it.id }) { list ->
                    CompactListCard(
                        list = list,
                        currentLang = currentLang,
                        onClick = { onNavigateToList(list.id) },
                        onDuplicate = { viewModel.duplicateList(list.id) { onNavigateToList(it) } },
                        onDelete = { viewModel.deleteList(list.id) }
                    )
                }
            }

            // 6. READY-MADE TEMPLATES (প্রস্তুত ফর্দ)
            if (popularTemplates.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = L10n.readymadeTemplatesHeader(currentLang),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                        Text(
                            text = L10n.seeAllTemplates(currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier
                                .clickable { onNavigateToTemplates() }
                                .padding(4.dp)
                                .testTag("see_all_templates_btn")
                        )
                    }
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                    ) {
                        items(popularTemplates, key = { it.id }) { template ->
                            CompactTemplateCard(
                                template = template,
                                currentLang = currentLang,
                                onClick = { onNavigateToTemplateDetail(template.id) },
                                onQuickCreate = {
                                    viewModel.createListFromTemplate(template.id) { newListId ->
                                        onNavigateToList(newListId)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 7. ADMOB BANNER AD
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AdMobBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// --- COMPACT COMPONENTS ---

@Composable
fun CompactTodayListCard(
    list: ShoppingListEntity,
    totalItems: Int,
    boughtItems: Int,
    pendingItems: Int,
    estimatedCost: Double,
    currentLang: AppLanguage,
    onOpenList: () -> Unit,
    onAddProduct: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onOpenList() }
            .testTag("today_list_summary_card"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = list.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val statusText = if (totalItems > 0) {
                            if (currentLang == AppLanguage.BN) {
                                "${L10n.digits(pendingItems, currentLang)}টি বাকি · ${L10n.digits(boughtItems, currentLang)}টি কেনা"
                            } else {
                                "$pendingItems remaining · $boughtItems bought"
                            }
                        } else {
                            if (currentLang == AppLanguage.BN) "ফর্দটি খালি" else "List is empty"
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    onClick = onAddProduct,
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("today_list_add_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (currentLang == AppLanguage.BN) "পণ্য যোগ" else "Add Item",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            if (totalItems > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = if (totalItems > 0) boughtItems.toFloat() / totalItems.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun EmptyTodayListCard(
    mode: AppMode,
    currentLang: AppLanguage,
    onCreateList: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (currentLang == AppLanguage.BN) "আজকের ফর্দ নেই" else "No Active List",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                )
                Text(
                    text = if (currentLang == AppLanguage.BN) "কেনাকাটা শুরু করতে ফর্দ খুলুন" else "Start a new list to track purchases",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }

            Surface(
                onClick = onCreateList,
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.testTag("open_today_list_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLang == AppLanguage.BN) "নতুন ফর্দ" else "New List",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SmartSuggestionBanner(
    suggestion: SmartRepeatSuggestion,
    currentLang: AppLanguage,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val displayName = L10n.productName(suggestion.productNameBn, suggestion.productNameEn, currentLang)
            val msg = L10n.smartSuggestionMsg(displayName, suggestion.intervalDays, currentLang)
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                onClick = onAdd,
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.testTag("suggestion_add_btn")
            ) {
                Text(
                    text = if (currentLang == AppLanguage.BN) "যোগ করুন" else "Add",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun CompactQuickAddRow(
    products: List<ProductEntity>,
    currentLang: AppLanguage,
    onProductClick: (ProductEntity) -> Unit,
    onOpenCatalog: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentLang == AppLanguage.BN) "দ্রুত যোগ" else "Quick Add",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
            Text(
                text = L10n.seeAllCatalog(currentLang),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                modifier = Modifier
                    .clickable { onOpenCatalog() }
                    .padding(2.dp)
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
        ) {
            items(products, key = { it.id }) { product ->
                Surface(
                    onClick = { onProductClick(product) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("quick_chip_${product.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = L10n.productName(product.nameBn, product.nameEn, currentLang),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactListCard(
    list: ShoppingListEntity,
    currentLang: AppLanguage = AppLanguage.BN,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.5.dp)
            .clickable { onClick() }
            .testTag("list_card_${list.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (list.isCompleted) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (list.isCompleted) Icons.Default.CheckCircle else Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = if (list.isCompleted) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.padding(top = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (list.budget > 0) {
                        Text(
                            text = "${if (currentLang == AppLanguage.BN) "বাজেট:" else "Budget:"} ${L10n.price(list.budget, currentLang)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    val modeLabel = if (list.isCompleted) {
                        if (currentLang == AppLanguage.BN) "সম্পন্ন" else "Completed"
                    } else if (list.mode == "STORE") {
                        if (currentLang == AppLanguage.BN) "দোকান" else "Store"
                    } else {
                        if (currentLang == AppLanguage.BN) "ব্যক্তিগত" else "Personal"
                    }

                    Text(
                        text = modeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (list.isCompleted) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = if (currentLang == AppLanguage.BN) "অপশন" else "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (currentLang == AppLanguage.BN) "আবার তৈরি করুন" else "Create Again") },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text(L10n.deleteList(currentLang), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CompactTemplateCard(
    template: TemplateEntity,
    currentLang: AppLanguage,
    onClick: () -> Unit,
    onQuickCreate: () -> Unit
) {
    val title = L10n.templateTitle(template.titleBn, template.titleEn, currentLang)
    val desc = L10n.templateDesc(template.descriptionBn, template.descriptionEn, currentLang)
    val imageRes = TemplateImageProvider.getImageResForTemplate(template)

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .testTag("template_card_${template.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            // Image Header with Quick Add Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                Surface(
                    onClick = onQuickCreate,
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .testTag("template_quick_add_${template.id}")
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.BN) "+ ফর্দ" else "+ List",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyListsCard(
    mode: AppMode,
    currentLang: AppLanguage = AppLanguage.BN,
    onCreateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = L10n.noListsTitle(currentLang),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
            Text(
                text = L10n.tapToCreateList(currentLang),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCreateClick,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("empty_state_create_list_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = L10n.createButton(currentLang),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
