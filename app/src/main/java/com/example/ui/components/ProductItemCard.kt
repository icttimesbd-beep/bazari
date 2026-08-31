package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.domain.model.AppLanguage
import com.example.utils.BengaliNumberUtils
import com.example.utils.L10n
import com.example.utils.LanguageManager

@Composable
fun BudgetSummaryCard(
    totalEstimatedCost: Double,
    boughtCost: Double,
    budget: Double,
    totalItems: Int,
    boughtItems: Int,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val progress = if (totalItems > 0) boughtItems.toFloat() / totalItems.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(300), label = "item_progress")
    val unboughtCost = (totalEstimatedCost - boughtCost).coerceAtLeast(0.0)
    val unboughtItems = (totalItems - boughtItems).coerceAtLeast(0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top Row: Progress header & Budget Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (boughtItems > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = L10n.progressLabel(boughtItems, totalItems, currentLang),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    )
                    if (totalItems > 0) {
                        val pct = (progress * 100).toInt()
                        Text(
                            text = " (${L10n.digits(pct, currentLang)}%)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Surface(
                    onClick = onEditBudgetClick,
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.testTag("edit_budget_button")
                ) {
                    Text(
                        text = if (budget > 0) {
                            if (currentLang == AppLanguage.BN) "বাজেট: ${L10n.price(budget, currentLang)}"
                            else "Budget: ${L10n.price(budget, currentLang)}"
                        } else {
                            if (currentLang == AppLanguage.BN) "+ বাজেট দিন" else "+ Set Budget"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SEPARATE TOTALS: Distinct cards for Checked/Bought vs Remaining vs Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Ticked / Checked Total (টিক মার্ক করা মোট)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (boughtItems > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        1.dp,
                        if (boughtItems > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("checked_total_stat_card")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (boughtItems > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = L10n.checkedTotalTitle(currentLang),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (boughtItems > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = L10n.price(boughtCost, currentLang),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (boughtItems > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = L10n.itemsCount(boughtItems, currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                // 2. Remaining Unchecked Total (বাকি কেনার মোট)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("remaining_total_stat_card")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = L10n.remainingTotalTitle(currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = L10n.price(unboughtCost, currentLang),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = L10n.itemsCount(unboughtItems, currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom summary footer: Overall list total & budget comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${L10n.listTotalTitle(currentLang)}: ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = L10n.price(totalEstimatedCost, currentLang),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    )
                }

                if (budget > 0) {
                    val remaining = budget - totalEstimatedCost
                    val isOver = remaining < 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentLang == AppLanguage.BN) {
                                if (!isOver) "বাকি বাজেট: " else "অতিরিক্ত: "
                            } else {
                                if (!isOver) "Left: " else "Over: "
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (!isOver) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = L10n.price(Math.abs(remaining), currentLang),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!isOver) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListItemCard(
    item: ShoppingItemEntity,
    onToggleBought: () -> Unit,
    onEditItem: () -> Unit,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val qtyNum = BengaliNumberUtils.toEnglishDigits(item.quantity).toDoubleOrNull() ?: 1.0
    val itemTotalPrice = qtyNum * item.unitPrice

    val cardBg by animateColorAsState(
        targetValue = if (item.isBought) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        else MaterialTheme.colorScheme.surface,
        label = "item_card_bg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .testTag("shopping_item_${item.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            1.dp,
            if (item.isBought) MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isBought,
                onCheckedChange = { onToggleBought() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .size(36.dp)
                    .testTag("checkbox_${item.id}")
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEditItem() }
            ) {
                Text(
                    text = item.nameBn,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (item.isBought) FontWeight.Normal else FontWeight.SemiBold,
                        textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 14.sp,
                        color = if (item.isBought) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = L10n.quantityWithUnit(item.quantity, item.unit, currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (item.isBought) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    if (item.unitPrice > 0) {
                        Text(
                            text = L10n.price(itemTotalPrice, currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (item.isBought) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.secondary
                            )
                        )
                    }

                    if (item.brand.isNotBlank()) {
                        Text(
                            text = "• ${item.brand}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onEditItem,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("edit_item_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (currentLang == AppLanguage.BN) "সম্পাদনা" else "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDeleteItem,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("delete_item_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = if (currentLang == AppLanguage.BN) "মুছুন" else "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    onAddProduct: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val displayName = L10n.productName(product.nameBn, product.nameEn, currentLang)
    val secondaryName = L10n.productSecondaryName(product.nameBn, product.nameEn, currentLang)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("fav_btn_${product.id}")
            ) {
                Icon(
                    imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (currentLang == AppLanguage.BN) "প্রিয় পণ্য" else "Favorite",
                    tint = if (product.isFavorite) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (secondaryName.isNotBlank()) {
                    Text(
                        text = secondaryName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "${L10n.digits(1, currentLang)} ${L10n.translateUnit(product.defaultUnit, currentLang)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                    if (product.defaultPrice > 0) {
                        Text(
                            text = "• ${L10n.price(product.defaultPrice, currentLang)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            IconButton(
                onClick = onAddProduct,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("add_product_${product.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (currentLang == AppLanguage.BN) "ফর্দে যোগ করুন" else "Add to list",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

