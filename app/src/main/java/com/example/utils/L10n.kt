package com.example.utils

import com.example.domain.model.AppLanguage
import com.example.domain.model.AppMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object L10n {

    fun appName(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাজারি" else "Bazari"

    fun appTagline(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "বাজার সহজ, ফর্দ আরও সহজ।" else "Shopping made easy, lists made smarter."

    fun digits(number: Any, lang: AppLanguage): String {
        val str = number.toString()
        return if (lang == AppLanguage.BN) {
            BengaliNumberUtils.toBengaliDigits(str)
        } else {
            BengaliNumberUtils.toEnglishDigits(str)
        }
    }

    fun price(amount: Double, lang: AppLanguage): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##0")
        val formatted = formatter.format(amount)
        return if (lang == AppLanguage.BN) {
            "৳${BengaliNumberUtils.toBengaliDigits(formatted)}"
        } else {
            "৳$formatted"
        }
    }

    fun translateUnit(unit: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BN) {
            return SmartEntryParser.mapUnitToStandardBn(unit)
        }
        return when (unit.trim().lowercase()) {
            "কেজি", "কে.জি", "কেজি.", "kg", "kgs", "kilo" -> "kg"
            "গ্রাম", "gm", "g", "gram", "grams" -> "g"
            "লিটার", "লি.", "ltr", "l", "liter", "litres" -> "L"
            "মিলি", "মি.লি", "ml" -> "ml"
            "পিস", "টা", "টি", "pc", "pcs", "piece", "pieces" -> "pcs"
            "হালি" -> "hali (4)"
            "ডজন", "dozen", "doz" -> "dozen"
            "প্যাকেট", "প্যাক", "pack", "pkt", "packet" -> "pack"
            "বক্স", "box" -> "box"
            "কার্টন", "কার্টুন", "carton" -> "carton"
            "বস্তা", "sack", "bosta" -> "sack"
            "বোতল", "bottle", "btl" -> "bottle"
            "কৌটা", "can", "jar", "ক্যান" -> "can"
            "আঁটি", "bundle" -> "bundle"
            "জোড়া", "pair" -> "pair"
            "রিম", "ream" -> "ream"
            "রোল", "roll" -> "roll"
            else -> unit
        }
    }

    fun quantityWithUnit(qty: String, unit: String, lang: AppLanguage): String {
        val formattedQty = digits(qty, lang)
        val formattedUnit = translateUnit(unit, lang)
        return "$formattedQty $formattedUnit"
    }

    fun productName(nameBn: String, nameEn: String, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN && nameEn.isNotBlank()) nameEn else nameBn
    }

    fun productSecondaryName(nameBn: String, nameEn: String, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN) {
            if (nameBn.isNotBlank() && nameBn != nameEn) nameBn else ""
        } else {
            if (nameEn.isNotBlank() && nameEn != nameBn) nameEn else ""
        }
    }

    fun categoryName(nameBn: String, nameEn: String, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN && nameEn.isNotBlank()) nameEn else nameBn
    }

    fun templateTitle(titleBn: String, titleEn: String, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN && titleEn.isNotBlank()) titleEn else titleBn
    }

    fun templateDesc(descBn: String, descEn: String, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN && descEn.isNotBlank()) descEn else descBn
    }

    fun modeTitle(mode: AppMode, lang: AppLanguage): String {
        return if (lang == AppLanguage.BN) {
            if (mode == AppMode.STORE) "দোকান মোড" else "ব্যক্তিগত"
        } else {
            if (mode == AppMode.STORE) "Store Mode" else "Personal"
        }
    }

    // Navigation Labels
    fun navHome(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দ" else "Lists"
    fun navTemplates(lang: AppLanguage): String = if (lang == AppLanguage.BN) "রেডিমেড" else "Templates"
    fun navHistory(lang: AppLanguage): String = if (lang == AppLanguage.BN) "হিস্ট্রি" else "History"
    fun navSettings(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সেটিংস" else "Settings"

    // Home Screen
    fun activeListsHeader(lang: AppLanguage): String = if (lang == AppLanguage.BN) "চলতি ফর্দ" else "Active Lists"
    fun yourListsHeader(lang: AppLanguage): String = if (lang == AppLanguage.BN) "আপনার চলতি ফর্দসমূহ" else "Your Active Lists"
    fun storeListsHeader(lang: AppLanguage): String = if (lang == AppLanguage.BN) "দোকানের চলতি ফর্দসমূহ" else "Active Store Lists"
    fun completedListsHeader(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সম্পন্ন ফর্দ" else "Completed Lists"
    fun noListsTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "কোনো ফর্দ নেই" else "No Shopping Lists"
    fun noListsSubtitle(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "নতুন ফর্দ তৈরি করতে নিচের বাটনে চাপুন" else "Tap the + button below to create a new list"
    fun noActiveLists(lang: AppLanguage): String = if (lang == AppLanguage.BN) "কোনো চলতি ফর্দ নেই" else "No Active Lists"
    fun tapToCreateList(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "নতুন ফর্দ তৈরি করে কেনাকাটার হিসাব রাখুন।" else "Create a new shopping list to track purchases."
    fun openNewList(lang: AppLanguage): String = if (lang == AppLanguage.BN) "+ নতুন ফর্দ খুলুন" else "+ Open New List"
    fun newListFab(lang: AppLanguage): String = if (lang == AppLanguage.BN) "নতুন ফর্দ" else "New List"
    fun newList(lang: AppLanguage): String = if (lang == AppLanguage.BN) "নতুন ফর্দ" else "New List"
    fun newStoreList(lang: AppLanguage): String = if (lang == AppLanguage.BN) "নতুন দোকানের ফর্দ" else "New Store List"
    fun listsCount(count: Int, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "${digits(count, lang)} টি ফর্দ" else "$count lists"
    fun itemsCount(count: Int, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "${digits(count, lang)} টি পণ্য" else "$count items"
    fun itemsBought(bought: Int, total: Int, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "${digits(bought, lang)}/${digits(total, lang)} টি কেনা"
        else "$bought/$total bought"
    fun estimatedCost(amount: Double, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "আনুমানিক ${price(amount, lang)}" else "Est. ${price(amount, lang)}"
    fun budgetLabel(amount: Double, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "বাজেট ${price(amount, lang)}" else "Budget ${price(amount, lang)}"
    fun quickAddHeader(lang: AppLanguage): String = if (lang == AppLanguage.BN) "দ্রুত যোগ করুন" else "Quick Add"
    fun seeAllCatalog(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সব ক্যাটালগ →" else "Full Catalog →"
    fun readyMadeTemplatesHeader(lang: AppLanguage): String = if (lang == AppLanguage.BN) "রেডিমেড ফর্দ" else "Ready Templates"
    fun readymadeTemplatesHeader(lang: AppLanguage): String = if (lang == AppLanguage.BN) "রেডিমেড ফর্দসমূহ" else "Ready-made Templates"
    fun seeAllTemplates(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সব দেখুন →" else "See All →"

    // List Details Screen
    fun quickInputPlaceholder(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "যেমন: ৫ কেজি চাল, ১২টি ডিম..." else "e.g. 5kg rice, 12 eggs, 2L oil..."
    fun quickInputAdd(lang: AppLanguage): String = if (lang == AppLanguage.BN) "যোগ করুন" else "Add"
    fun pickFromCatalog(lang: AppLanguage): String = if (lang == AppLanguage.BN) "+ ক্যাটালগ থেকে বাছুন" else "+ Pick from Catalog"
    fun completeMarketTrip(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাজার সম্পন্ন করুন" else "Complete Trip"
    fun completeShoppingBtn(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাজার সম্পন্ন করুন" else "Complete Shopping"
    fun listCompletedBadge(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সম্পন্ন" else "Completed"
    fun progressLabel(bought: Int, total: Int, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "${digits(bought, lang)}/${digits(total, lang)} টি টিক করা"
        else "$bought/$total checked"
    fun checkedTotalTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "টিক মার্ক করা মোট" else "Checked Total"
    fun remainingTotalTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাকি কেনার মোট" else "Remaining Total"
    fun listTotalTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দের মোট" else "List Total"
    fun totalEstimate(amount: Double, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "মোট আনুমানিক: ${price(amount, lang)}" else "Total Est: ${price(amount, lang)}"
    fun remainingBudget(amount: Double, isOver: Boolean, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) {
            if (isOver) "অতিরিক্ত: ${price(amount, lang)}" else "বাকি: ${price(amount, lang)}"
        } else {
            if (isOver) "Over: ${price(amount, lang)}" else "Left: ${price(amount, lang)}"
        }
    fun filterAll(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সব" else "All"
    fun filterPending(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাকি আছে" else "Pending"
    fun remainingToBuy(lang: AppLanguage): String = if (lang == AppLanguage.BN) "কেনা বাকি" else "Remaining to Buy"
    fun boughtItemsHeader(lang: AppLanguage): String = if (lang == AppLanguage.BN) "কেনা হয়েছে" else "Bought Items"
    fun filterBought(lang: AppLanguage): String = if (lang == AppLanguage.BN) "কেনা হয়েছে" else "Bought"
    fun shareList(lang: AppLanguage): String = if (lang == AppLanguage.BN) "শেয়ার করুন" else "Share List"
    fun copyList(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দ কপি করুন" else "Duplicate List"
    fun renameList(lang: AppLanguage): String = if (lang == AppLanguage.BN) "নাম পরিবর্তন" else "Rename"
    fun deleteList(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দ মুছুন" else "Delete List"
    fun clearBought(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ক্লিয়ার" else "Clear"
    fun emptyListTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দটি এখনও খালি" else "This list is empty"
    fun emptyListDesc(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "উপরের বক্সে পণ্যের নাম লিখুন অথবা ক্যাটালগ থেকে পণ্য যোগ করুন।"
        else "Type product names above or pick directly from the catalog."
    fun emptyListItemsTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দে কোনো পণ্য নেই" else "No Items in this List"
    fun emptyListItemsSubtitle(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "উপরে লিখে বা ক্যাটালগ থেকে সহজে যোগ করুন" else "Type above or pick from catalog to add items"

    // Product Picker
    fun catalogTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "পণ্য নির্বাচন ও ক্যাটালগ" else "Product Catalog"
    fun searchProductsPlaceholder(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "পণ্য খুঁজুন (চাল, ডাল, তেল...)" else "Search products (rice, oil, eggs...)"
    fun allCategory(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সব পণ্য" else "All Products"
    fun allProductsTab(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সব পণ্য" else "All Products"
    fun favProductsTab(lang: AppLanguage): String = if (lang == AppLanguage.BN) "প্রিয় পণ্য" else "Favorites"
    fun popularTab(lang: AppLanguage): String = if (lang == AppLanguage.BN) "জনপ্রিয়" else "Popular"
    fun favoritesCategory(lang: AppLanguage): String = if (lang == AppLanguage.BN) "প্রিয় পণ্য" else "Favorites"
    fun addCustomProduct(lang: AppLanguage): String = if (lang == AppLanguage.BN) "কাস্টম পণ্য তৈরি" else "Custom Product"
    fun productsFound(count: Int, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "${digits(count, lang)} টি পণ্য পাওয়া গেছে" else "$count products found"

    // Templates Screen
    fun templatesTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দ টেমপ্লেট" else "Shopping Templates"
    fun templateFilterAll(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সব টেমপ্লেট" else "All"
    fun templateFilterPersonal(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ব্যক্তিগত ও মেস" else "Personal & Mess"
    fun templateFilterStore(lang: AppLanguage): String = if (lang == AppLanguage.BN) "দোকান ও পাইকারি" else "Store & Wholesale"
    fun useTemplateButton(lang: AppLanguage): String = if (lang == AppLanguage.BN) "এই ফর্দ তৈরি করুন" else "Use This Template"
    fun createListBtn(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দ তৈরি করুন" else "Create List"
    fun viewTemplateItems(lang: AppLanguage): String = if (lang == AppLanguage.BN) "আইটেম দেখুন" else "View Items"
    fun createCustomTemplate(lang: AppLanguage): String = if (lang == AppLanguage.BN) "কাস্টম টেমপ্লেট" else "Custom Template"

    // History Screen
    fun historyTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাজারের হিস্ট্রি ও হিসাব" else "Shopping History & Receipts"
    fun totalSpendAllTime(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সর্বমোট বাজার খরচ" else "Total Shopping Spend"
    fun totalSpentLabel(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সর্বমোট বাজার খরচ" else "Total Spent"
    fun completedTripsLabel(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সম্পন্ন ট্রিপ" else "Completed Trips"
    fun totalCompletedTrips(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সম্পন্ন বাজার" else "Completed Trips"
    fun reuseListButton(lang: AppLanguage): String = if (lang == AppLanguage.BN) "আবার তৈরি" else "Re-create List"
    fun reuseList(lang: AppLanguage): String = if (lang == AppLanguage.BN) "আবার তৈরি" else "Re-create"
    fun deleteHistoryButton(lang: AppLanguage): String = if (lang == AppLanguage.BN) "মুছুন" else "Delete"
    fun emptyHistoryTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "এখনও কোনো বাজার সম্পন্ন হয়নি" else "No Shopping History Yet"
    fun emptyHistorySubtitle(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "ফর্দের সব পণ্য কেনা শেষ হলে বাজার সম্পন্ন করুন" else "Complete shopping lists to save history receipts here"
    fun noHistory(lang: AppLanguage): String = if (lang == AppLanguage.BN) "এখনও কোনো বাজার সম্পন্ন হয়নি" else "No shopping history yet"
    fun noHistoryDesc(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "ফর্দের কেনাকাটা শেষ হলে 'বাজার সম্পন্ন করুন' চাপলে হিস্ট্রি এখানে সংরক্ষিত হবে।"
        else "When your market trip is done, tap 'Complete Shopping' to save receipts here."

    // Settings Screen
    fun settingsTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সেটিংস ও পছন্দ" else "Settings & Preferences"
    fun languageSectionTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ভাষা / Language" else "Language / ভাষা"
    fun languageSectionDesc(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "বাংলা বা ইংরেজি নির্বাচন করুন" else "Choose Bengali or English"
    fun storeModeTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "দোকান / পাইকারি মোড" else "Store / Wholesale Mode"
    fun storeModeDescOn(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "দোকানের বস্তা, কার্টন ও পাইকারি একক সক্রিয়" else "Wholesale units like sacks, cartons enabled"
    fun storeModeDescOff(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "ব্যক্তিগত ও পরিবারভিত্তিক মুদি বাজার মোড" else "Personal and family grocery mode"
    fun offlineCatalogTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "অফলাইন ক্যাটালগ" else "Offline Catalog"
    fun offlineCatalogPoints(lang: AppLanguage): String = if (lang == AppLanguage.BN) {
        "• ২৫,০০০+ পণ্যের বিশাল ডাটাবেজ ডিভাইসে সংরক্ষিত।\n• ইন্টারনেট বা লগইন ছাড়াই সম্পূর্ণ অফলাইনে কাজ করে।\n• বাংলা ও ইংরেজি উভয় ভাষায় তাৎক্ষণিক স্মার্ট সার্চ।"
    } else {
        "• 25,000+ grocery & store items saved on-device.\n• Works completely offline without internet or login.\n• Instant smart search in Bengali & English."
    }
    fun developerSectionTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ডেভেলপার পরিচিতি" else "Developer Information"
    fun developerName(lang: AppLanguage): String = if (lang == AppLanguage.BN) "রুহুল আমিন সৌরভ" else "Ruhul Amin Sourav"
    fun developerRole(lang: AppLanguage): String = if (lang == AppLanguage.BN) "অ্যাপ ক্রিয়েটর ও লিড ডেভেলপার" else "Lead Mobile Developer & Creator"
    fun developerEmail(lang: AppLanguage): String = "icttimesbd@gmail.com"
    fun jsonCatalogSectionTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "পণ্য ক্যাটালগ ব্যাকআপ ও ট্রান্সফার" else "Catalog JSON Backup & Transfer"
    fun jsonExportTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "পণ্য ক্যাটালগ ডাউনলোড (JSON Export)" else "Download Catalog (JSON Export)"
    fun jsonExportDesc(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সব পণ্যের ডাটাবেজ JSON ফরম্যাটে ডাউনলোড বা শেয়ার করুন" else "Export entire catalog to a JSON backup file"
    fun jsonImportTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "পণ্য ক্যাটালগ আপলোড (JSON Import)" else "Upload Catalog (JSON Import)"
    fun jsonImportDesc(lang: AppLanguage): String = if (lang == AppLanguage.BN) "কাস্টম বা ব্যাকআপ JSON ফাইল থেকে পণ্য যোগ করুন" else "Import or merge custom products from JSON"
    fun reloadCatalogTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ডিফল্ট ক্যাটালগ রিলোড (২৫,০০০+ পণ্য)" else "Reload Default Catalog (25,000+)"
    fun reloadCatalogDesc(lang: AppLanguage): String = if (lang == AppLanguage.BN) "মূল ২৫,০০০+ পণ্যের ক্যাটালগ পুনরায় রিফ্রেশ করুন" else "Reset catalog back to full 25,000+ default items"
    fun aboutTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাজারি (Bazari)" else "Bazari"
    fun aboutDescription(lang: AppLanguage): String = if (lang == AppLanguage.BN) {
        "স্মার্ট মুদি ও শপিং ফর্দ ম্যানেজার। অফলাইন ক্যাটালগ, ভয়েস ইনপুট, ইভেন্ট বাজেট এবং স্বয়ংক্রিয় হিসাব।"
    } else {
        "Smart grocery shopping list manager with full offline catalog, voice entry, event budgeting, and instant calculation."
    }

    // Shopping Mode
    fun shoppingMode(lang: AppLanguage): String = if (lang == AppLanguage.BN) "শপিং মোড" else "Shopping Mode"
    fun exitShoppingMode(lang: AppLanguage): String = if (lang == AppLanguage.BN) "শপিং মোড বন্ধ" else "Exit Shopping Mode"
    fun shoppingModeActive(lang: AppLanguage): String = if (lang == AppLanguage.BN) "শপিং মোড চালু" else "Shopping Mode Active"

    // Smart Suggestions & Repeat
    fun smartSuggestionTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "স্মার্ট পরামর্শ" else "Smart Suggestion"
    fun smartSuggestionMsg(item: String, days: Int, lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "আপনি সাধারণত প্রতি ${digits(days, lang)} দিনে $item কেনেন। ফর্দে যোগ করবেন?"
        else "You usually buy $item every $days days. Add to list?"
    fun smartSuggestionsSetting(lang: AppLanguage): String = if (lang == AppLanguage.BN) "স্মার্ট পুনরাবৃত্তি পরামর্শ" else "Smart Repeat Suggestions"
    fun smartSuggestionsDesc(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "নিয়মিত পণ্যের প্রয়োজনীয়তা স্বয়ংক্রিয়ভাবে স্মরণ করিয়ে দেয়"
        else "Automatically reminds you of regularly purchased products"

    // Export Strings
    fun exportOptionsTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দ এক্সপোর্ট ও শেয়ার" else "Export & Share List"
    fun exportAsImage(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ছবি (Image/Receipt)" else "Image (Receipt)"
    fun exportAsPdf(lang: AppLanguage): String = if (lang == AppLanguage.BN) "পিডিএফ (PDF Document)" else "PDF Document"
    fun exportAsDoc(lang: AppLanguage): String = if (lang == AppLanguage.BN) "টেক্সট ফাইল (Text/Doc)" else "Text File (Doc)"
    fun shareTextOption(lang: AppLanguage): String = if (lang == AppLanguage.BN) "মেসেজ হিসেবে পাঠান" else "Share as Message"
    fun printListOption(lang: AppLanguage): String = if (lang == AppLanguage.BN) "প্রিন্ট করুন" else "Print List"

    // Voice & Barcode
    fun voiceInput(lang: AppLanguage): String = if (lang == AppLanguage.BN) "মুখে বলে যোগ করুন" else "Voice Add"
    fun voiceListening(lang: AppLanguage): String = if (lang == AppLanguage.BN) "শুনছি... পণ্যের নাম বলুন" else "Listening... say items"
    fun barcodeScanner(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বারকোড স্ক্যানার" else "Barcode Scanner"
    fun barcodePlaceholder(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বারকোড লিখুন বা স্ক্যান করুন" else "Enter barcode or scan"

    // Dialogs
    fun saveButton(lang: AppLanguage): String = if (lang == AppLanguage.BN) "সংরক্ষণ" else "Save"
    fun cancelButton(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাতিল" else "Cancel"
    fun createButton(lang: AppLanguage): String = if (lang == AppLanguage.BN) "তৈরি করুন" else "Create"
    fun deleteButton(lang: AppLanguage): String = if (lang == AppLanguage.BN) "মুছুন" else "Delete"
    fun quantityLabel(lang: AppLanguage): String = if (lang == AppLanguage.BN) "পরিমাণ:" else "Quantity:"
    fun unitLabel(lang: AppLanguage): String = if (lang == AppLanguage.BN) "একক:" else "Unit:"
    fun unitPriceLabel(lang: AppLanguage): String = if (lang == AppLanguage.BN) "প্রতি এককের আনুমানিক দাম (৳)" else "Est. Unit Price (৳)"
    fun brandNotesLabel(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ব্র্যান্ড / নোট (ঐচ্ছিক)" else "Brand / Notes (Optional)"
    fun setBudgetTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাজেট নির্ধারণ" else "Set Budget"
    fun setBudgetHint(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দের জন্য আনুমানিক বাজেট টাকা লিখুন:" else "Enter estimated budget amount for this list:"
    fun createListTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "নতুন ফর্দ তৈরি" else "Create New List"
    fun listNameLabel(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ফর্দের নাম" else "List Name"
    fun defaultPersonalListName(lang: AppLanguage): String = if (lang == AppLanguage.BN) "আজকের বাজার" else "Today's Grocery"
    fun defaultStoreListName(lang: AppLanguage): String = if (lang == AppLanguage.BN) "দোকানের নতুন ফর্দ" else "Store Restock List"
    fun celebrationTitle(lang: AppLanguage): String = if (lang == AppLanguage.BN) "বাজার সম্পন্ন হয়েছে!" else "Market Trip Completed!"
    fun celebrationDesc(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) "ফর্দের সব কেনাকাটা সম্পন্ন হয়েছে এবং হিস্ট্রিতে সংরক্ষিত হয়েছে।"
        else "All shopping items have been completed and saved to history receipts."
    fun viewInHistory(lang: AppLanguage): String = if (lang == AppLanguage.BN) "হিস্ট্রিতে দেখুন" else "View in History"
    fun okButton(lang: AppLanguage): String = if (lang == AppLanguage.BN) "ঠিক আছে" else "OK"
    fun saveAsTemplate(lang: AppLanguage): String = if (lang == AppLanguage.BN) "টেমপ্লেট হিসেবে সংরক্ষণ" else "Save as Template"
    fun templateNameHint(lang: AppLanguage): String = if (lang == AppLanguage.BN) "টেমপ্লেটের নাম লিখুন" else "Enter template name"
}
