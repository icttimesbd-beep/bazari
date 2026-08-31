package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ShoppingHistoryEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppMode
import com.example.utils.BengaliNumberUtils
import com.example.utils.L10n
import com.example.utils.LanguageManager

val popularUnitsBn = listOf("কেজি", "গ্রাম", "লিটার", "পিস", "ডজন", "হালি", "প্যাকেট", "বস্তা", "কার্টন", "বোতল", "আঁটি")

@Composable
fun EditItemDialog(
    item: ShoppingItemEntity,
    onDismiss: () -> Unit,
    onSave: (ShoppingItemEntity) -> Unit,
    onDelete: () -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    var quantity by remember {
        mutableStateOf(
            if (currentLang == AppLanguage.EN) BengaliNumberUtils.toEnglishDigits(item.quantity)
            else BengaliNumberUtils.toBengaliDigits(item.quantity)
        )
    }
    var selectedUnit by remember { mutableStateOf(item.unit) }
    var unitPriceStr by remember {
        mutableStateOf(
            if (item.unitPrice > 0) {
                if (currentLang == AppLanguage.EN) item.unitPrice.toInt().toString()
                else BengaliNumberUtils.toBengaliDigits(item.unitPrice.toInt().toString())
            } else ""
        )
    }
    var brand by remember { mutableStateOf(item.brand) }
    var notes by remember { mutableStateOf(item.notes) }

    fun adjustQuantity(delta: Double) {
        val curVal = BengaliNumberUtils.toEnglishDigits(quantity).toDoubleOrNull() ?: 1.0
        val newVal = (curVal + delta).coerceAtLeast(0.25)
        quantity = if (currentLang == AppLanguage.EN) {
            if (newVal % 1.0 == 0.0) newVal.toInt().toString() else newVal.toString()
        } else {
            BengaliNumberUtils.toBengaliDigits(newVal)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = item.nameBn,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quantity Stepper
                Text(
                    text = L10n.quantityLabel(currentLang),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { adjustQuantity(-1.0) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("qty_minus_btn")
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = if (currentLang == AppLanguage.BN) "কমান" else "Decrease",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("item_qty_input"),
                        singleLine = true
                    )

                    IconButton(
                        onClick = { adjustQuantity(1.0) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("qty_plus_btn")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = if (currentLang == AppLanguage.BN) "বাড়ান" else "Increase",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Unit selection chips
                Text(
                    text = L10n.unitLabel(currentLang),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularUnitsBn) { unitBn ->
                        val isSelected = selectedUnit == unitBn
                        val unitDisplay = L10n.translateUnit(unitBn, currentLang)
                        Surface(
                            onClick = { selectedUnit = unitBn },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.testTag("unit_chip_$unitBn")
                        ) {
                            Text(
                                text = unitDisplay,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Unit price
                OutlinedTextField(
                    value = unitPriceStr,
                    onValueChange = { unitPriceStr = it },
                    label = { Text(L10n.unitPriceLabel(currentLang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_price_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Brand / Notes
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(L10n.brandNotesLabel(currentLang)) },
                    placeholder = {
                        Text(if (currentLang == AppLanguage.BN) "যেমন: প্রাণ, তীর, এসিআই" else "e.g. Fresh, Teer, ACI")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceNum = BengaliNumberUtils.toEnglishDigits(unitPriceStr).toDoubleOrNull() ?: 0.0
                    onSave(
                        item.copy(
                            quantity = quantity.ifBlank { if (currentLang == AppLanguage.BN) "১" else "1" },
                            unit = selectedUnit,
                            unitPrice = priceNum,
                            brand = brand.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("save_item_btn")
            ) {
                Text(L10n.saveButton(currentLang))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("delete_item_in_dialog_btn")
            ) {
                Text(L10n.deleteButton(currentLang))
            }
        }
    )
}

@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    var budgetStr by remember {
        mutableStateOf(
            if (currentBudget > 0) {
                if (currentLang == AppLanguage.EN) currentBudget.toInt().toString()
                else BengaliNumberUtils.toBengaliDigits(currentBudget.toInt().toString())
            } else ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = L10n.setBudgetTitle(currentLang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = L10n.setBudgetHint(currentLang),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    placeholder = {
                        Text(if (currentLang == AppLanguage.BN) "যেমন: ৫০০০" else "e.g. 5000")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_input_field"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Quick preset chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val presets = listOf(1000, 2000, 5000, 10000, 15000, 25000)
                    items(presets) { preset ->
                        val presetStr = if (currentLang == AppLanguage.BN) {
                            BengaliNumberUtils.toBengaliDigits(preset)
                        } else {
                            preset.toString()
                        }
                        Surface(
                            onClick = { budgetStr = presetStr },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = L10n.price(preset.toDouble(), currentLang),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bVal = BengaliNumberUtils.toEnglishDigits(budgetStr).toDoubleOrNull() ?: 0.0
                    onSave(bVal)
                },
                modifier = Modifier.testTag("save_budget_btn")
            ) {
                Text(L10n.saveButton(currentLang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(L10n.cancelButton(currentLang))
            }
        }
    )
}

@Composable
fun CreateListDialog(
    initialMode: AppMode,
    onDismiss: () -> Unit,
    onCreate: (title: String, mode: AppMode, budget: Double) -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    var title by remember {
        mutableStateOf(
            if (initialMode == AppMode.PERSONAL) L10n.defaultPersonalListName(currentLang)
            else L10n.defaultStoreListName(currentLang)
        )
    }
    var selectedMode by remember { mutableStateOf(initialMode) }
    var budgetStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = L10n.createListTitle(currentLang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mode selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = {
                            selectedMode = AppMode.PERSONAL
                            if (title == L10n.defaultStoreListName(currentLang)) {
                                title = L10n.defaultPersonalListName(currentLang)
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (selectedMode == AppMode.PERSONAL) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f).testTag("select_mode_personal")
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.BN) "ব্যক্তিগত / পরিবার" else "Personal / Family",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (selectedMode == AppMode.PERSONAL) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                        )
                    }

                    Surface(
                        onClick = {
                            selectedMode = AppMode.STORE
                            if (title == L10n.defaultPersonalListName(currentLang)) {
                                title = L10n.defaultStoreListName(currentLang)
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (selectedMode == AppMode.STORE) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f).testTag("select_mode_store")
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.BN) "দোকান / পাইকারি" else "Store / Wholesale",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (selectedMode == AppMode.STORE) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(L10n.listNameLabel(currentLang)) },
                    placeholder = {
                        Text(if (currentLang == AppLanguage.BN) "যেমন: সাপ্তাহিক বাজার, ইফতার ফর্দ..." else "e.g. Weekly Grocery, Monthly Restock...")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_list_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = {
                        Text(if (currentLang == AppLanguage.BN) "আনুমানিক বাজেট (ঐচ্ছিক)" else "Estimated Budget (Optional)")
                    },
                    placeholder = {
                        Text(if (currentLang == AppLanguage.BN) "যেমন: ৩০০০" else "e.g. 3000")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_list_budget_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = BengaliNumberUtils.toEnglishDigits(budgetStr).toDoubleOrNull() ?: 0.0
                    onCreate(title.trim(), selectedMode, budget)
                },
                modifier = Modifier.testTag("confirm_create_list_btn")
            ) {
                Text(L10n.createButton(currentLang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(L10n.cancelButton(currentLang))
            }
        }
    )
}

@Composable
fun CelebrationDialog(
    history: ShoppingHistoryEntity,
    onDismiss: () -> Unit,
    onViewHistory: () -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = L10n.celebrationTitle(currentLang),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = L10n.celebrationDesc(currentLang),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.BN) "মোট পণ্য:" else "Total Items:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = L10n.itemsCount(history.boughtItemCount, currentLang),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (history.totalSpent > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.BN) "মোট খরচ:" else "Total Spend:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = L10n.price(history.totalSpent, currentLang),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onViewHistory,
                modifier = Modifier.testTag("celebration_view_history_btn")
            ) {
                Text(L10n.viewInHistory(currentLang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(L10n.okButton(currentLang))
            }
        }
    )
}

@Composable
fun CustomProductDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (nameBn: String, nameEn: String, categoryId: String, unit: String, price: Double) -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    var nameBn by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "rice_grains") }
    var selectedUnit by remember { mutableStateOf("কেজি") }
    var defaultPriceStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = L10n.addCustomProduct(currentLang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = {
                        Text(if (currentLang == AppLanguage.BN) "পণ্যের বাংলা নাম *" else "Bengali Name *")
                    },
                    placeholder = {
                        Text(if (currentLang == AppLanguage.BN) "যেমন: কাজুবাদাম, কাতিলা গাম" else "e.g. কাজুবাদাম")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_product_name_bn"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = {
                        Text(if (currentLang == AppLanguage.BN) "ইংরেজি নাম (ঐচ্ছিক)" else "English Name (Optional)")
                    },
                    placeholder = { Text("e.g. Cashew Nut") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_product_name_en"),
                    singleLine = true
                )

                Text(
                    text = if (currentLang == AppLanguage.BN) "ক্যাটেগরি:" else "Category:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        val catName = L10n.categoryName(cat.nameBn, cat.nameEn, currentLang)
                        Surface(
                            onClick = { selectedCategoryId = cat.id },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = catName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Text(
                    text = L10n.unitLabel(currentLang),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularUnitsBn) { unitBn ->
                        val isSelected = selectedUnit == unitBn
                        val unitDisplay = L10n.translateUnit(unitBn, currentLang)
                        Surface(
                            onClick = { selectedUnit = unitBn },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = unitDisplay,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = defaultPriceStr,
                    onValueChange = { defaultPriceStr = it },
                    label = {
                        Text(if (currentLang == AppLanguage.BN) "আনুমানিক মূল্য ৳ (ঐচ্ছিক)" else "Est. Price ৳ (Optional)")
                    },
                    placeholder = {
                        Text(if (currentLang == AppLanguage.BN) "যেমন: ১২০" else "e.g. 120")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val primaryName = if (nameBn.isNotBlank()) nameBn else nameEn
                    if (primaryName.isNotBlank()) {
                        val price = BengaliNumberUtils.toEnglishDigits(defaultPriceStr).toDoubleOrNull() ?: 0.0
                        onSave(primaryName.trim(), nameEn.trim(), selectedCategoryId, selectedUnit, price)
                    }
                },
                enabled = nameBn.isNotBlank() || nameEn.isNotBlank(),
                modifier = Modifier.testTag("save_custom_product_btn")
            ) {
                Text(L10n.saveButton(currentLang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(L10n.cancelButton(currentLang))
            }
        }
    )
}
