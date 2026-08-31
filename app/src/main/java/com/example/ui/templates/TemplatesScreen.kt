package com.example.ui.templates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.TemplateItemEntity
import com.example.domain.model.AppLanguage
import com.example.ui.components.AdMobBanner
import com.example.ui.components.BazariBottomBar
import com.example.ui.components.BazariTopBar
import com.example.utils.L10n
import com.example.utils.LanguageManager
import com.example.utils.TemplateImageProvider

@Composable
fun TemplatesScreen(
    viewModel: TemplatesViewModel,
    onNavigateToTemplateDetail: (String) -> Unit,
    onNavigateToList: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val templates by viewModel.templates.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            BazariTopBar(
                title = L10n.templatesTitle(currentLang),
                showLanguageToggle = true
            )
        },
        bottomBar = {
            BazariBottomBar(
                currentRoute = "templates",
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
        ) {
            // 1. Swipable Visual Carousel (ডানে বামে টেনে দেখার চিত্রযুক্ত টেমপ্লেট গ্যালারি)
            if (templates.isNotEmpty()) {
                item {
                    TemplateVisualSwipeGallery(
                        templates = templates,
                        currentLang = currentLang,
                        onOpenDetail = { onNavigateToTemplateDetail(it.id) },
                        onQuickCreate = { template ->
                            viewModel.createListFromTemplate(template.id) { newListId ->
                                onNavigateToList(newListId)
                            }
                        }
                    )
                }
            }

            // 2. Filter Tabs (সব, পরিবার ও মেস, দোকান ও পাইকারি)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.BN) "সব ক্যাটাগরির টেমপ্লেট তালিকা" else "All Category Templates",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterOptions = listOf(
                            "ALL" to (if (currentLang == AppLanguage.BN) "সব টেমপ্লেট" else "All Templates"),
                            "PERSONAL" to (if (currentLang == AppLanguage.BN) "পরিবার ও মেস" else "Family & Mess"),
                            "STORE" to (if (currentLang == AppLanguage.BN) "দোকান ও পাইকারি" else "Store & Wholesale")
                        )

                        items(filterOptions) { (key, label) ->
                            val isSelected = selectedFilter == key
                            Surface(
                                onClick = { viewModel.setSelectedFilter(key) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.testTag("tpl_filter_$key")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Detailed Templates List Cards with corresponding visual image thumbnail
            items(templates, key = { it.id }) { template ->
                TemplateRowCard(
                    template = template,
                    currentLang = currentLang,
                    onClick = { onNavigateToTemplateDetail(template.id) },
                    onQuickCreate = {
                        viewModel.createListFromTemplate(template.id) { newListId ->
                            onNavigateToList(newListId)
                        }
                    },
                    onDelete = if (template.isCustom) {
                        { viewModel.deleteCustomTemplate(template.id) }
                    } else null
                )
            }

            // AdMob Banner
            item {
                Spacer(modifier = Modifier.height(10.dp))
                AdMobBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Top Swipable Gallery: ডানে-বামে টেনে দেখার চিত্রযুক্ত ইন্টারেক্টিভ ক্যারোসেল
 */
@Composable
fun TemplateVisualSwipeGallery(
    templates: List<TemplateEntity>,
    currentLang: AppLanguage,
    onOpenDetail: (TemplateEntity) -> Unit,
    onQuickCreate: (TemplateEntity) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { templates.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Top instruction header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentLang == AppLanguage.BN) "চিত্রসহ ফর্দ টেমপ্লেট" else "Visual Template Gallery",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.BN) "ডানে-বামে টানুন 👈👉" else "Swipe left-right 👈👉",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .testTag("templates_horizontal_pager")
        ) { page ->
            val template = templates[page]
            val imageRes = TemplateImageProvider.getImageResForTemplate(template)
            val title = L10n.templateTitle(template.titleBn, template.titleEn, currentLang)
            val desc = L10n.templateDesc(template.descriptionBn, template.descriptionEn, currentLang)

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onOpenDetail(template) }
                    .testTag("gallery_card_${template.id}"),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Category Image
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Black.copy(alpha = 0.92f)
                                    ),
                                    startY = 0f
                                )
                            )
                    )

                    // Top Badge: Topic & Category
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (template.category == "STORE") MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = if (template.category == "STORE") {
                                    if (currentLang == AppLanguage.BN) "দোকান ও হোলসেল" else "Store & Wholesale"
                                } else {
                                    if (currentLang == AppLanguage.BN) "পরিবার ও মেস" else "Family & Household"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${page + 1}/${templates.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Bottom Content: Title, Description, and Actions
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )

                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { onOpenDetail(template) },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.BN) "আইটেম দেখুন" else "View Items",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                onClick = { onQuickCreate(template) },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.BN) "ফর্দ তৈরি করুন" else "Create List",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pager Dots Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(templates.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(5.dp)
                        .width(if (isSelected) 18.dp else 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
fun TemplateRowCard(
    template: TemplateEntity,
    currentLang: AppLanguage = AppLanguage.BN,
    onClick: () -> Unit,
    onQuickCreate: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val title = L10n.templateTitle(template.titleBn, template.titleEn, currentLang)
    val desc = L10n.templateDesc(template.descriptionBn, template.descriptionEn, currentLang)
    val imageRes = TemplateImageProvider.getImageResForTemplate(template)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("tpl_card_${template.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Thumbnail
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.45f)
                                )
                            )
                        )
                )
                // Small Category Dot Badge
                Surface(
                    color = if (template.category == "STORE") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = if (template.category == "STORE") "দোকান" else "পরিবার",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details and actions
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = onClick,
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = L10n.viewTemplateItems(currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 6.dp)
                        )
                    }

                    Surface(
                        onClick = onQuickCreate,
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = L10n.createListBtn(currentLang),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateDetailScreen(
    templateId: String,
    viewModel: TemplatesViewModel,
    onBackClick: () -> Unit,
    onNavigateToList: (String) -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    LaunchedEffect(templateId) {
        viewModel.setSelectedTemplateId(templateId)
    }

    val templates by viewModel.templates.collectAsState()
    val template = templates.find { it.id == templateId }
    val items by viewModel.currentTemplateItems.collectAsState()

    // Selective checkbox state: default all selected
    val selectedItemIds = remember { mutableStateMapOf<Long, Boolean>() }

    LaunchedEffect(items) {
        items.forEach { item ->
            if (!selectedItemIds.containsKey(item.id)) {
                selectedItemIds[item.id] = true
            }
        }
    }

    val title = template?.let { L10n.templateTitle(it.titleBn, it.titleEn, currentLang) }
        ?: (if (currentLang == AppLanguage.BN) "টেমপ্লেট প্রিভিউ" else "Template Preview")
    val desc = template?.let { L10n.templateDesc(it.descriptionBn, it.descriptionEn, currentLang) } ?: ""
    val imageRes = template?.let { TemplateImageProvider.getImageResForTemplate(it) } ?: TemplateImageProvider.getImageRes(templateId)

    Scaffold(
        topBar = {
            BazariTopBar(
                title = title,
                showBackButton = true,
                onBackClick = onBackClick,
                showLanguageToggle = true
            )
        },
        floatingActionButton = {
            val count = items.count { selectedItemIds[it.id] == true }
            FloatingActionButton(
                onClick = {
                    val checkedIds = items.filter { selectedItemIds[it.id] == true }.map { it.id }
                    viewModel.createListFromTemplate(templateId, checkedIds) { newListId ->
                        onNavigateToList(newListId)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("create_list_from_template_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${L10n.createListBtn(currentLang)} (${L10n.itemsCount(count, currentLang)})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
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
            // Template Hero Header with corresponding photo
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
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
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.2f),
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = if (template?.category == "STORE") "দোকান ও হোলসেল" else "পরিবার ও গৃহস্থালী",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            )
                            if (desc.isNotBlank()) {
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Items Count and Select All Action Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${if (currentLang == AppLanguage.BN) "পণ্য তালিকা" else "Item List"} (${L10n.itemsCount(items.size, currentLang)})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    val allSelected = items.isNotEmpty() && items.all { selectedItemIds[it.id] == true }
                    Text(
                        text = if (allSelected) {
                            if (currentLang == AppLanguage.BN) "সব আনচেক করুন" else "Uncheck All"
                        } else {
                            if (currentLang == AppLanguage.BN) "সব সিলেক্ট করুন" else "Select All"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clickable {
                                val newState = !allSelected
                                items.forEach { selectedItemIds[it.id] = newState }
                            }
                            .padding(4.dp)
                    )
                }
            }

            // List of template items
            items(items, key = { it.id }) { item ->
                val isChecked = selectedItemIds[item.id] ?: true
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { selectedItemIds[item.id] = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.nameBn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                Text(
                                    text = L10n.quantityWithUnit(item.quantity, item.unit, currentLang),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    )
                                )
                                if (item.defaultPrice > 0) {
                                    Text(
                                        text = "• ${L10n.price(item.defaultPrice, currentLang)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
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
