package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_plans",
    indices = [
        Index(value = ["eventType"]),
        Index(value = ["isArchived"]),
        Index(value = ["updatedAt"])
    ]
)
data class EventPlanEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val eventType: String = "PICNIC", // "PICNIC", "WEDDING", "TOUR", "PARTY", "MESS", "OTHER"
    val targetBudget: Double = 0.0,
    val eventDate: Long = System.currentTimeMillis(),
    val organizerName: String = "",
    val location: String = "",
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "event_members",
    foreignKeys = [
        ForeignKey(
            entity = EventPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["isPaid"])
    ]
)
data class EventMemberEntity(
    @PrimaryKey
    val id: String,
    val eventId: String,
    val name: String,
    val phone: String = "",
    val targetAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val isPaid: Boolean = false,
    val paymentMethod: String = "নগদ", // নগদ, বিকাশ, নগদ, ব্যাংক
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "event_expenses",
    foreignKeys = [
        ForeignKey(
            entity = EventPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["category"]),
        Index(value = ["isBought"])
    ]
)
data class EventExpenseEntity(
    @PrimaryKey
    val id: String,
    val eventId: String,
    val title: String,
    val category: String = "BAZAAR", // "BAZAAR", "TRANSPORT", "COOKING", "VENUE", "SOUND", "MISC"
    val quantity: String = "১",
    val unit: String = "কেজি",
    val unitPrice: Double = 0.0,
    val amount: Double = 0.0,
    val paidBy: String = "কমন ফান্ড",
    val isBought: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quick_tape_notes",
    indices = [
        Index(value = ["noteType"]),
        Index(value = ["isSettled"]),
        Index(value = ["updatedAt"])
    ]
)
data class QuickTapeNoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val rawEntriesText: String, // Lines of items with amounts
    val totalSum: Double = 0.0,
    val noteType: String = "CALC_TAPE", // "CALC_TAPE", "DUES_NOTE", "EXPENSE_NOTE"
    val personName: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
