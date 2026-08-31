package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.domain.model.AppLanguage
import com.example.utils.ExportManager
import com.example.utils.L10n

@Composable
fun ExportDialog(
    context: Context,
    list: ShoppingListEntity,
    items: List<ShoppingItemEntity>,
    currentLang: AppLanguage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = L10n.exportOptionsTitle(currentLang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExportOptionItem(
                    icon = Icons.Default.Message,
                    title = L10n.shareTextOption(currentLang),
                    subtitle = if (currentLang == AppLanguage.BN) "হোয়াটসঅ্যাপ, এসএমএস বা যেকোনো মেসেঞ্জারে পাঠান" else "Share via WhatsApp, SMS, or Messenger",
                    onClick = {
                        onDismiss()
                        val text = ExportManager.generatePlainText(list, items, currentLang)
                        ExportManager.shareText(context, list.title, text)
                    },
                    testTag = "export_text_btn"
                )

                ExportOptionItem(
                    icon = Icons.Default.Image,
                    title = L10n.exportAsImage(currentLang),
                    subtitle = if (currentLang == AppLanguage.BN) "রসিদ আকারে স্পষ্ট ছবি হিসেবে শেয়ার করুন" else "Share as high quality receipt image",
                    onClick = {
                        onDismiss()
                        val file = ExportManager.exportImageReceipt(context, list, items, currentLang)
                        if (file != null) {
                            ExportManager.shareFile(context, file, "image/png", list.title)
                        }
                    },
                    testTag = "export_image_btn"
                )

                ExportOptionItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = L10n.exportAsPdf(currentLang),
                    subtitle = if (currentLang == AppLanguage.BN) "প্রিন্টযোগ্য অফিসিয়াল পিডিএফ তৈরি করুন" else "Generate printable PDF document",
                    onClick = {
                        onDismiss()
                        val file = ExportManager.exportPdfDocument(context, list, items, currentLang)
                        if (file != null) {
                            ExportManager.shareFile(context, file, "application/pdf", list.title)
                        }
                    },
                    testTag = "export_pdf_btn"
                )

                ExportOptionItem(
                    icon = Icons.Default.Description,
                    title = L10n.exportAsDoc(currentLang),
                    subtitle = if (currentLang == AppLanguage.BN) "ওয়ার্ড বা নোটপ্যাডে খোলার মতো ডকুমেন্ট" else "Document file for Word or Notepad",
                    onClick = {
                        onDismiss()
                        val file = ExportManager.exportDocument(context, list, items, currentLang)
                        if (file != null) {
                            ExportManager.shareFile(context, file, "application/msword", list.title)
                        }
                    },
                    testTag = "export_doc_btn"
                )

                ExportOptionItem(
                    icon = Icons.Default.Print,
                    title = L10n.printListOption(currentLang),
                    subtitle = if (currentLang == AppLanguage.BN) "সরাসরি প্রিন্টারে পাঠান" else "Send directly to printer",
                    onClick = {
                        onDismiss()
                        val file = ExportManager.exportPdfDocument(context, list, items, currentLang)
                        if (file != null) {
                            ExportManager.printPdf(context, file, list.title)
                        }
                    },
                    testTag = "export_print_btn"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(L10n.cancelButton(currentLang))
            }
        }
    )
}

@Composable
private fun ExportOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
