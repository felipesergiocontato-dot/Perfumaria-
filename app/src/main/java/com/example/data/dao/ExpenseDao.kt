package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenseEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense_entries WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getExpensesByVehicle(vehicleId: Long): Flow<List<ExpenseEntry>>

    @Query("SELECT * FROM expense_entries ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntry>>

    @Query("SELECT * FROM expense_entries WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    suspend fun getExpensesList(vehicleId: Long): List<ExpenseEntry>

    @Query("SELECT * FROM expense_entries WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ExpenseEntry): Long

    @Update
    suspend fun update(entry: ExpenseEntry)

    @Delete
    suspend fun delete(entry: ExpenseEntry)

    @Query("DELETE FROM expense_entries WHERE vehicleId = :vehicleId")
    suspend fun clearByVehicle(vehicleId: Long)
}
