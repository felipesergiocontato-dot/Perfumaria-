package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.ExpenseEntry
import com.example.data.model.FuelEntry
import com.example.data.model.Vehicle
import com.example.data.repository.VehicleRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    suspend fun exportBackupJson(
        context: Context,
        repository: VehicleRepository,
        vehicle: Vehicle
    ): File? {
        return try {
            val fuels = repository.getAllFuelList(vehicle.id)
            val expenses = repository.getAllExpenseList(vehicle.id)

            val rootJson = JSONObject().apply {
                put("app", "AutoGastos")
                put("version", "1.0")
                put("exportTimestamp", System.currentTimeMillis())
                put("exportDateFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

                val vehicleJson = JSONObject().apply {
                    put("id", vehicle.id)
                    put("name", vehicle.name)
                    put("plate", vehicle.plate)
                    put("brandModel", vehicle.brandModel)
                    put("initialOdometer", vehicle.initialOdometer)
                    put("currentOdometer", vehicle.currentOdometer)
                    put("tankCapacityLiters", vehicle.tankCapacityLiters)
                    put("defaultFuel", vehicle.defaultFuel)
                    put("companyReimbursementRatePerKm", vehicle.companyReimbursementRatePerKm)
                }
                put("vehicle", vehicleJson)

                val fuelsArray = JSONArray()
                for (fuel in fuels) {
                    val fObj = JSONObject().apply {
                        put("timestamp", fuel.timestamp)
                        put("currentOdometerKm", fuel.currentOdometerKm)
                        put("distanceTraveledKm", fuel.distanceTraveledKm ?: 0.0)
                        put("fuelType", fuel.fuelType)
                        put("liters", fuel.liters)
                        put("pricePerLiter", fuel.pricePerLiter)
                        put("totalPrice", fuel.totalPrice)
                        put("isFullTank", fuel.isFullTank)
                        put("consumptionKmPerLiter", fuel.consumptionKmPerLiter ?: 0.0)
                        put("costPerKm", fuel.costPerKm ?: 0.0)
                        put("stationName", fuel.stationName ?: "")
                        put("notes", fuel.notes ?: "")
                    }
                    fuelsArray.put(fObj)
                }
                put("fuelEntries", fuelsArray)

                val expensesArray = JSONArray()
                for (exp in expenses) {
                    val eObj = JSONObject().apply {
                        put("timestamp", exp.timestamp)
                        put("category", exp.category)
                        put("title", exp.title)
                        put("amount", exp.amount)
                        put("odometerKm", exp.odometerKm ?: 0.0)
                        put("location", exp.location ?: "")
                        put("notes", exp.notes ?: "")
                    }
                    expensesArray.put(eObj)
                }
                put("expenseEntries", expensesArray)
            }

            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(reportsDir, "Backup_AutoGastos_$timeStamp.json")

            FileOutputStream(backupFile).use { out ->
                out.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            }
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importBackupJson(
        jsonString: String,
        repository: VehicleRepository,
        currentVehicleId: Long
    ): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("fuelEntries") && !root.has("expenseEntries")) return false

            val fuels = root.optJSONArray("fuelEntries")
            if (fuels != null) {
                for (i in 0 until fuels.length()) {
                    val fObj = fuels.getJSONObject(i)
                    val entry = FuelEntry(
                        vehicleId = currentVehicleId,
                        timestamp = fObj.optLong("timestamp", System.currentTimeMillis()),
                        currentOdometerKm = fObj.optDouble("currentOdometerKm", 0.0),
                        distanceTraveledKm = if (fObj.has("distanceTraveledKm") && fObj.getDouble("distanceTraveledKm") > 0) fObj.getDouble("distanceTraveledKm") else null,
                        fuelType = fObj.optString("fuelType", "Gasolina Comum"),
                        liters = fObj.optDouble("liters", 0.0),
                        pricePerLiter = fObj.optDouble("pricePerLiter", 0.0),
                        totalPrice = fObj.optDouble("totalPrice", 0.0),
                        isFullTank = fObj.optBoolean("isFullTank", true),
                        consumptionKmPerLiter = if (fObj.has("consumptionKmPerLiter") && fObj.getDouble("consumptionKmPerLiter") > 0) fObj.getDouble("consumptionKmPerLiter") else null,
                        costPerKm = if (fObj.has("costPerKm") && fObj.getDouble("costPerKm") > 0) fObj.getDouble("costPerKm") else null,
                        stationName = fObj.optString("stationName").ifEmpty { null },
                        notes = fObj.optString("notes").ifEmpty { null }
                    )
                    repository.addFuelEntry(entry)
                }
            }

            val expenses = root.optJSONArray("expenseEntries")
            if (expenses != null) {
                for (i in 0 until expenses.length()) {
                    val eObj = expenses.getJSONObject(i)
                    val entry = ExpenseEntry(
                        vehicleId = currentVehicleId,
                        timestamp = eObj.optLong("timestamp", System.currentTimeMillis()),
                        category = eObj.optString("category", "Estacionamento"),
                        title = eObj.optString("title", "Despesa Importada"),
                        amount = eObj.optDouble("amount", 0.0),
                        odometerKm = if (eObj.has("odometerKm") && eObj.getDouble("odometerKm") > 0) eObj.getDouble("odometerKm") else null,
                        location = eObj.optString("location").ifEmpty { null },
                        notes = eObj.optString("notes").ifEmpty { null }
                    )
                    repository.addExpenseEntry(entry)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
