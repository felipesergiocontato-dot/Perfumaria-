package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {
    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY currentOdometerKm DESC, timestamp DESC")
    fun getFuelEntriesByVehicle(vehicleId: Long): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries ORDER BY currentOdometerKm DESC, timestamp DESC")
    fun getAllFuelEntries(): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY currentOdometerKm DESC, timestamp DESC")
    suspend fun getFuelEntriesList(vehicleId: Long): List<FuelEntry>

    @Query("SELECT * FROM fuel_entries WHERE id = :id LIMIT 1")
    suspend fun getFuelEntryById(id: Long): FuelEntry?

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId AND currentOdometerKm < :currentOdometer ORDER BY currentOdometerKm DESC LIMIT 1")
    suspend fun getPreviousFuelEntry(vehicleId: Long, currentOdometer: Double): FuelEntry?

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY currentOdometerKm DESC LIMIT 1")
    suspend fun getLatestFuelEntry(vehicleId: Long): FuelEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FuelEntry): Long

    @Update
    suspend fun update(entry: FuelEntry)

    @Delete
    suspend fun delete(entry: FuelEntry)

    @Query("DELETE FROM fuel_entries WHERE vehicleId = :vehicleId")
    suspend fun clearByVehicle(vehicleId: Long)
}
