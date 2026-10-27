package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_entries")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = ExpenseCategory.ESTACIONAMENTO.displayName,
    val title: String,
    val amount: Double,
    val odometerKm: Double? = null,
    val location: String? = null,
    val receiptPhotoUri: String? = null,
    val notes: String? = null
)
