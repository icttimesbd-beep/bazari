package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.EventExpenseEntity
import com.example.data.local.entity.EventMemberEntity
import com.example.data.local.entity.EventPlanEntity
import com.example.data.local.entity.QuickTapeNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventPlanDao {

    @Query("SELECT * FROM event_plans WHERE isArchived = 0 ORDER BY eventDate DESC, updatedAt DESC")
    fun getAllActiveEvents(): Flow<List<EventPlanEntity>>

    @Query("SELECT * FROM event_plans WHERE id = :id LIMIT 1")
    fun getEventById(id: String): Flow<EventPlanEntity?>

    @Query("SELECT * FROM event_plans WHERE id = :id LIMIT 1")
    suspend fun getEventByIdSync(id: String): EventPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventPlanEntity)

    @Update
    suspend fun updateEvent(event: EventPlanEntity)

    @Query("DELETE FROM event_plans WHERE id = :id")
    suspend fun deleteEvent(id: String)

    // Members
    @Query("SELECT * FROM event_members WHERE eventId = :eventId ORDER BY isPaid ASC, createdAt ASC")
    fun getMembersForEvent(eventId: String): Flow<List<EventMemberEntity>>

    @Query("SELECT * FROM event_members WHERE eventId = :eventId")
    suspend fun getMembersForEventSync(eventId: String): List<EventMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: EventMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<EventMemberEntity>)

    @Update
    suspend fun updateMember(member: EventMemberEntity)

    @Query("UPDATE event_members SET isPaid = :isPaid, paidAmount = :paidAmount WHERE id = :id")
    suspend fun setMemberPayment(id: String, isPaid: Boolean, paidAmount: Double)

    @Query("DELETE FROM event_members WHERE id = :id")
    suspend fun deleteMember(id: String)

    // Expenses & Bazaar items
    @Query("SELECT * FROM event_expenses WHERE eventId = :eventId ORDER BY category ASC, isBought ASC, createdAt DESC")
    fun getExpensesForEvent(eventId: String): Flow<List<EventExpenseEntity>>

    @Query("SELECT * FROM event_expenses WHERE eventId = :eventId")
    suspend fun getExpensesForEventSync(eventId: String): List<EventExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: EventExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<EventExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: EventExpenseEntity)

    @Query("UPDATE event_expenses SET isBought = :isBought WHERE id = :id")
    suspend fun setExpenseBought(id: String, isBought: Boolean)

    @Query("DELETE FROM event_expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)
}

@Dao
interface QuickTapeNoteDao {

    @Query("SELECT * FROM quick_tape_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<QuickTapeNoteEntity>>

    @Query("SELECT * FROM quick_tape_notes WHERE noteType = :type ORDER BY updatedAt DESC")
    fun getNotesByType(type: String): Flow<List<QuickTapeNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuickTapeNoteEntity)

    @Update
    suspend fun updateNote(note: QuickTapeNoteEntity)

    @Query("UPDATE quick_tape_notes SET isSettled = :isSettled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setNoteSettled(id: String, isSettled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM quick_tape_notes WHERE id = :id")
    suspend fun deleteNote(id: String)
}
