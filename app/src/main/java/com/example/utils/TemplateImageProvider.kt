package com.example.utils

import androidx.annotation.DrawableRes
import com.example.R
import com.example.data.local.entity.TemplateEntity

object TemplateImageProvider {

    @DrawableRes
    fun getImageResForTemplate(template: TemplateEntity): Int {
        return getImageRes(template.id, template.titleBn, template.category)
    }

    @DrawableRes
    fun getImageRes(templateId: String, titleBn: String = "", category: String = "PERSONAL"): Int {
        val lowerId = templateId.lowercase()
        val lowerTitle = titleBn.lowercase()

        return when {
            lowerId.contains("mach") || lowerId.contains("fish") ||
                    lowerTitle.contains("মাছ") || lowerTitle.contains("ইলিশ") || lowerTitle.contains("রুই") -> {
                R.drawable.img_tpl_fish_market
            }
            lowerId.contains("sobji") || lowerId.contains("veg") ||
                    lowerTitle.contains("সবজি") || lowerTitle.contains("শাক") || lowerTitle.contains("তরকারি") -> {
                R.drawable.img_tpl_veg_market
            }
            lowerId.contains("mangsho") || lowerId.contains("meat") || lowerId.contains("chicken") ||
                    lowerTitle.contains("মাংস") || lowerTitle.contains("চিকেন") || lowerTitle.contains("গরু") || lowerTitle.contains("খাসি") -> {
                R.drawable.img_tpl_meat_poultry
            }
            lowerId.contains("eid") || lowerId.contains("romjan") || lowerId.contains("ramadan") ||
                    lowerTitle.contains("ঈদ") || lowerTitle.contains("রমজান") || lowerTitle.contains("পোলাও") || lowerTitle.contains("সেমাই") -> {
                R.drawable.img_tpl_eid_festive
            }
            else -> {
                R.drawable.img_tpl_grocery_weekly
            }
        }
    }
}
