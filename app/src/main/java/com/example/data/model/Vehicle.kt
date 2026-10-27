package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val plate: String = "",
    val brandModel: String = "",
    val initialOdometer: Double = 0.0,
    val currentOdometer: Double = 0.0,
    val tankCapacityLiters: Double = 50.0,
    val defaultFuel: String = FuelType.GASOLINA_COMUM.displayName,
    val companyReimbursementRatePerKm: Double = 1.15, // Valor que a empresa paga por km rodado (R$/km)
    val isSelected: Boolean = true
)
