package com.example.ui.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.QuickTapeNoteEntity
import com.example.data.repository.BazariRepository
import com.example.utils.BengaliNumberUtils
import com.example.utils.MathExpressionEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TapeEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String,
    val expression: String = "", // e.g. "45 × 5"
    val op: String = "+",
    val amount: Double,
    val runningTotal: Double
)

class TapeCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BazariRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BazariRepository(db, application)
    }

    val savedNotes: StateFlow<List<QuickTapeNoteEntity>> = repository.getAllTapeNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _tapeEntries = MutableStateFlow<List<TapeEntry>>(emptyList())
    val tapeEntries: StateFlow<List<TapeEntry>> = _tapeEntries.asStateFlow()

    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    private val _currentLabel = MutableStateFlow("")
    val currentLabel: StateFlow<String> = _currentLabel.asStateFlow()

    private val _currentOp = MutableStateFlow("+")
    val currentOp: StateFlow<String> = _currentOp.asStateFlow()

    private val _grandTotal = MutableStateFlow(0.0)
    val grandTotal: StateFlow<Double> = _grandTotal.asStateFlow()

    fun onDigit(d: String) {
        val cur = _currentInput.value
        if (d == ".") {
            // Check if current active number segment already contains a dot
            val lastSegment = cur.split('+', '-', '×', '*', '÷', '/', ' ').lastOrNull() ?: ""
            if (lastSegment.contains(".")) return
        }
        _currentInput.value = cur + d
    }

    fun onLabelChange(label: String) {
        _currentLabel.value = label
    }

    fun onOpChange(op: String) {
        val cur = _currentInput.value.trim()
        if (cur.isEmpty()) {
            if (op == "+" || op == "-") {
                _currentOp.value = op
            }
            return
        }

        val normalizedOp = when (op) {
            "*" -> "×"
            "/" -> "÷"
            else -> op
        }

        // If the last character is already an operator, replace it
        val trailingChar = cur.last()
        if (trailingChar in charArrayOf('+', '-', '×', '÷', '*', '/')) {
            _currentInput.value = cur.dropLast(1).trimEnd() + " $normalizedOp "
        } else {
            _currentInput.value = "$cur $normalizedOp "
        }
    }

    fun onEquals() {
        val cur = _currentInput.value.trim()
        if (cur.isBlank()) return
        val evaluated = MathExpressionEvaluator.evaluate(cur)
        if (evaluated != null) {
            val isWhole = (evaluated % 1.0 == 0.0)
            _currentInput.value = if (isWhole) evaluated.toLong().toString() else evaluated.toString()
        }
    }

    fun onBackspace() {
        val cur = _currentInput.value
        if (cur.isNotEmpty()) {
            _currentInput.value = cur.trimEnd().dropLast(1).trimEnd()
        }
    }

    fun onClear() {
        _currentInput.value = ""
        _currentLabel.value = ""
        _tapeEntries.value = emptyList()
        _grandTotal.value = 0.0
        _currentOp.value = "+"
    }

    fun onClearCurrentInput() {
        _currentInput.value = ""
        _currentLabel.value = ""
    }

    fun pushCurrentEntry() {
        val rawInput = _currentInput.value.trim()
        if (rawInput.isBlank() && _currentLabel.value.isBlank()) return

        val evaluatedAmount = MathExpressionEvaluator.evaluate(rawInput) ?: 0.0
        if (evaluatedAmount == 0.0 && _currentLabel.value.isBlank()) return

        val previousTotal = _grandTotal.value
        val isDeduction = _currentOp.value == "-"
        val newTotal = if (isDeduction) previousTotal - evaluatedAmount else previousTotal + evaluatedAmount

        val hasFormula = MathExpressionEvaluator.hasOperator(rawInput)
        val formattedExpr = if (hasFormula) MathExpressionEvaluator.formatExpression(rawInput, useBn = false) else ""

        val defaultLabel = "আইটেম ${_tapeEntries.value.size + 1}"
        val labelText = _currentLabel.value.trim().ifBlank { defaultLabel }

        val entry = TapeEntry(
            label = labelText,
            expression = formattedExpr,
            op = _currentOp.value,
            amount = evaluatedAmount,
            runningTotal = newTotal
        )

        _tapeEntries.value = _tapeEntries.value + entry
        _grandTotal.value = newTotal
        _currentInput.value = ""
        _currentLabel.value = ""
        _currentOp.value = "+"
    }

    fun removeEntry(index: Int) {
        val currentList = _tapeEntries.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            // Recalculate running totals from start to finish
            var running = 0.0
            val recalculated = currentList.map { entry ->
                running = if (entry.op == "-") running - entry.amount else running + entry.amount
                entry.copy(runningTotal = running)
            }
            _tapeEntries.value = recalculated
            _grandTotal.value = running
        }
    }

    fun saveCurrentTapeAsNote(
        title: String,
        noteType: String = "CALC_TAPE",
        personName: String = "",
        onSaved: () -> Unit
    ) {
        val entries = _tapeEntries.value
        if (entries.isEmpty() && _grandTotal.value == 0.0) return

        val sb = StringBuilder()
        entries.forEachIndexed { idx, it ->
            val numBn = BengaliNumberUtils.toBengaliDigits(idx + 1)
            val exprPart = if (it.expression.isNotBlank()) " (${BengaliNumberUtils.toBengaliDigits(it.expression)})" else ""
            val opSign = if (it.op == "-") " -" else " +"
            val priceBn = BengaliNumberUtils.formatPriceTaka(it.amount, useBn = true)
            sb.appendLine("$numBn. ${it.label}$exprPart: $opSign$priceBn")
        }
        sb.appendLine("------------------------")
        sb.appendLine("সর্বমোট: ${BengaliNumberUtils.formatPriceTaka(_grandTotal.value, useBn = true)}")
        val rawText = sb.toString().trim()

        viewModelScope.launch {
            repository.saveTapeNote(
                title = title.ifBlank { if (noteType == "DUES_NOTE") "বাকি হিসাব ($personName)" else "হিসাব-নোট" },
                rawEntriesText = rawText,
                totalSum = _grandTotal.value,
                noteType = noteType,
                personName = personName
            )
            onSaved()
        }
    }

    fun convertTapeToShoppingList(onConverted: (String) -> Unit) {
        val entries = _tapeEntries.value
        if (entries.isEmpty()) return

        val sb = StringBuilder()
        entries.forEach {
            if (it.expression.isNotBlank()) {
                // If expression has e.g. "45 × 5", parse quantity and item
                sb.appendLine("${it.label} ${it.expression} = ${it.amount}")
            } else {
                sb.appendLine("${it.label} ${it.amount}")
            }
        }
        val rawText = sb.toString().trim()

        viewModelScope.launch {
            val listId = repository.convertTapeToShoppingList("ক্যালকুলেটরের বাজার হিসাব", rawText)
            onConverted(listId)
        }
    }

    fun toggleNoteSettled(note: QuickTapeNoteEntity) {
        viewModelScope.launch {
            repository.setNoteSettled(note.id, !note.isSettled)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteTapeNote(noteId)
        }
    }
}
