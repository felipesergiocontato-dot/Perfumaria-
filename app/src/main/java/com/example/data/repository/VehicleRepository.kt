package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.dao.FuelDao
import com.example.data.dao.VehicleDao
import com.example.data.model.ExpenseEntry
import com.example.data.model.FuelEntry
import com.example.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(
    private val vehicleDao: VehicleDao,
    private val fuelDao: FuelDao,
    private val expenseDao: ExpenseDao
) {
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    fun getFuelEntries(vehicleId: Long): Flow<List<FuelEntry>> =
        fuelDao.getFuelEntriesByVehicle(vehicleId)

    fun getExpenseEntries(vehicleId: Long): Flow<List<ExpenseEntry>> =
        expenseDao.getExpensesByVehicle(vehicleId)

    suspend fun getSelectedVehicle(): Vehicle? = vehicleDao.getSelectedVehicle()

    suspend fun selectVehicle(id: Long) = vehicleDao.setSelectedVehicle(id)

    suspend fun insertVehicle(vehicle: Vehicle): Long = vehicleDao.insert(vehicle)

    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.update(vehicle)

    suspend fun addFuelEntry(entry: FuelEntry): Long {
        // Calculate distance and consumption relative to previous entry
        val previous = fuelDao.getPreviousFuelEntry(entry.vehicleId, entry.currentOdometerKm)
        val distance = if (previous != null && entry.currentOdometerKm > previous.currentOdometerKm) {
            entry.currentOdometerKm - previous.currentOdometerKm
        } else {
            entry.distanceTraveledKm
        }

        val consumption = if (distance != null && distance > 0 && entry.liters > 0 && entry.isFullTank && (previous?.isFullTank != false)) {
            distance / entry.liters
        } else {
            null
        }

        val costPerKm = if (distance != null && distance > 0 && entry.totalPrice > 0) {
            entry.totalPrice / distance
        } else {
            null
        }

        val enriched = entry.copy(
            distanceTraveledKm = distance,
            consumptionKmPerLiter = consumption,
            costPerKm = costPerKm
        )

        val id = fuelDao.insert(enriched)

        // Update vehicle current odometer if higher
        val vehicle = vehicleDao.getVehicleById(entry.vehicleId)
        if (vehicle != null && entry.currentOdometerKm > vehicle.currentOdometer) {
            vehicleDao.update(vehicle.copy(currentOdometer = entry.currentOdometerKm))
        }

        return id
    }

    suspend fun updateFuelEntry(entry: FuelEntry) {
        val previous = fuelDao.getPreviousFuelEntry(entry.vehicleId, entry.currentOdometerKm)
        val distance = if (previous != null && entry.currentOdometerKm > previous.currentOdometerKm) {
            entry.currentOdometerKm - previous.currentOdometerKm
        } else {
            entry.distanceTraveledKm
        }

        val consumption = if (distance != null && distance > 0 && entry.liters > 0 && entry.isFullTank && (previous?.isFullTank != false)) {
            distance / entry.liters
        } else {
            null
        }

        val costPerKm = if (distance != null && distance > 0 && entry.totalPrice > 0) {
            entry.totalPrice / distance
        } else {
            null
        }

        fuelDao.update(
            entry.copy(
                distanceTraveledKm = distance,
                consumptionKmPerLiter = consumption,
                costPerKm = costPerKm
            )
        )
    }

    suspend fun deleteFuelEntry(entry: FuelEntry) = fuelDao.delete(entry)

    suspend fun addExpenseEntry(entry: ExpenseEntry): Long {
        val id = expenseDao.insert(entry)
        if (entry.odometerKm != null) {
            val vehicle = vehicleDao.getVehicleById(entry.vehicleId)
            if (vehicle != null && entry.odometerKm > vehicle.currentOdometer) {
                vehicleDao.update(vehicle.copy(currentOdometer = entry.odometerKm))
            }
        }
        return id
    }

    suspend fun updateExpenseEntry(entry: ExpenseEntry) = expenseDao.update(entry)

    suspend fun deleteExpenseEntry(entry: ExpenseEntry) = expenseDao.delete(entry)

    suspend fun getAllFuelList(vehicleId: Long) = fuelDao.getFuelEntriesList(vehicleId)

    suspend fun getAllExpenseList(vehicleId: Long) = expenseDao.getExpensesList(vehicleId)
}
