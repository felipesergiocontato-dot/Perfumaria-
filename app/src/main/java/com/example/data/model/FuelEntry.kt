package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_entries")
data class FuelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val currentOdometerKm: Double,
    val distanceTraveledKm: Double? = null,
    val fuelType: String = FuelType.GASOLINA_COMUM.displayName,
    val liters: Double,
    val pricePerLiter: Double,
    val totalPrice: Double,
    val isFullTank: Boolean = true,
    val consumptionKmPerLiter: Double? = null,
    val costPerKm: Double? = null,
    val stationName: String? = null,
    val odometerPhotoUri: String? = null,
    val receiptPhotoUri: String? = null,
    val notes: String? = null
)
