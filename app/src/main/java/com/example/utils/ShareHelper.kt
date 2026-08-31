package com.example.utils

import android.content.Context
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.domain.model.AppLanguage

object ShareHelper {

    fun generateFormattedListText(
        list: ShoppingListEntity,
        items: List<ShoppingItemEntity>,
        includePrices: Boolean = true
    ): String {
        return ExportManager.generatePlainText(list, items, AppLanguage.BN, includePrices)
    }

    fun shareText(context: Context, title: String, text: String) {
        ExportManager.shareText(context, title, text)
    }
}

