package com.example.ui.events

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EventExpenseEntity
import com.example.data.local.entity.EventMemberEntity
import com.example.data.local.entity.EventPlanEntity
import com.example.data.repository.BazariRepository
import com.example.data.repository.EventInvoiceSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BazariRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BazariRepository(db, application)
    }

    val activeEvents: StateFlow<List<EventPlanEntity>> = repository.getAllActiveEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createEvent(
        title: String,
        eventType: String = "PICNIC",
        targetBudget: Double = 0.0,
        organizerName: String = "",
        location: String = "",
        notes: String = "",
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val eventId = repository.createEvent(
                title = title,
                eventType = eventType,
                targetBudget = targetBudget,
                organizerName = organizerName,
                location = location,
                notes = notes
            )
            onCreated(eventId)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }
}

class EventDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BazariRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BazariRepository(db, application)
    }

    private val _currentEventId = MutableStateFlow("")
    val currentEventId: StateFlow<String> = _currentEventId.asStateFlow()

    private val _event = MutableStateFlow<EventPlanEntity?>(null)
    val event: StateFlow<EventPlanEntity?> = _event.asStateFlow()

    private val _members = MutableStateFlow<List<EventMemberEntity>>(emptyList())
    val members: StateFlow<List<EventMemberEntity>> = _members.asStateFlow()

    private val _expenses = MutableStateFlow<List<EventExpenseEntity>>(emptyList())
    val expenses: StateFlow<List<EventExpenseEntity>> = _expenses.asStateFlow()

    private val _invoiceSummary = MutableStateFlow<EventInvoiceSummary?>(null)
    val invoiceSummary: StateFlow<EventInvoiceSummary?> = _invoiceSummary.asStateFlow()

    fun loadEvent(eventId: String) {
        _currentEventId.value = eventId
        viewModelScope.launch {
            repository.getEventById(eventId).collect { ev ->
                _event.value = ev
                refreshSummary()
            }
        }
        viewModelScope.launch {
            repository.getMembersForEvent(eventId).collect { mList ->
                _members.value = mList
                refreshSummary()
            }
        }
        viewModelScope.launch {
            repository.getExpensesForEvent(eventId).collect { eList ->
                _expenses.value = eList
                refreshSummary()
            }
        }
    }

    private fun refreshSummary() {
        viewModelScope.launch {
            val summary = repository.getEventInvoiceSummary(_currentEventId.value)
            _invoiceSummary.value = summary
        }
    }

    fun addMember(
        name: String,
        phone: String = "",
        targetAmount: Double = 0.0,
        paidAmount: Double = 0.0,
        isPaid: Boolean = false,
        paymentMethod: String = "নগদ"
    ) {
        viewModelScope.launch {
            repository.addMemberToEvent(
                eventId = _currentEventId.value,
                name = name,
                phone = phone,
                targetAmount = targetAmount,
                paidAmount = paidAmount,
                isPaid = isPaid,
                paymentMethod = paymentMethod
            )
            refreshSummary()
        }
    }

    fun addMembersBatch(rawNames: String, defaultTarget: Double = 0.0) {
        viewModelScope.launch {
            repository.addMembersBatch(_currentEventId.value, rawNames, defaultTarget)
            refreshSummary()
        }
    }

    fun updateMemberPayment(memberId: String, isPaid: Boolean, paidAmount: Double) {
        viewModelScope.launch {
            repository.setMemberPayment(memberId, isPaid, paidAmount)
            refreshSummary()
        }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            repository.deleteMember(memberId)
            refreshSummary()
        }
    }

    fun addExpense(
        title: String,
        category: String = "BAZAAR",
        quantity: String = "১",
        unit: String = "কেজি",
        unitPrice: Double = 0.0,
        amount: Double = 0.0,
        paidBy: String = "কমন ফান্ড",
        isBought: Boolean = false,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addExpenseToEvent(
                eventId = _currentEventId.value,
                title = title,
                category = category,
                quantity = quantity,
                unit = unit,
                unitPrice = unitPrice,
                amount = amount,
                paidBy = paidBy,
                isBought = isBought,
                notes = notes
            )
            refreshSummary()
        }
    }

    fun setExpenseBought(expenseId: String, isBought: Boolean) {
        viewModelScope.launch {
            repository.setExpenseBought(expenseId, isBought)
            refreshSummary()
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
            refreshSummary()
        }
    }

    fun convertToShoppingList(onConverted: (String) -> Unit) {
        viewModelScope.launch {
            val listId = repository.convertEventBazaarToShoppingList(_currentEventId.value)
            onConverted(listId)
        }
    }
}
