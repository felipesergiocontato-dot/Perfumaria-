package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ExpenseCategory
import com.example.data.model.FuelType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AutoGastos", appName)
    }

    @Test
    fun `verify fuel type parsing`() {
        val fuel = FuelType.fromDisplayName("Gasolina Comum")
        assertEquals(FuelType.GASOLINA_COMUM, fuel)
        val ethanol = FuelType.fromDisplayName("Etanol")
        assertEquals(FuelType.ETANOL, ethanol)
    }

    @Test
    fun `verify expense category parsing`() {
        val parking = ExpenseCategory.fromDisplayName("Estacionamento")
        assertEquals(ExpenseCategory.ESTACIONAMENTO, parking)
    }

    @Test
    fun `verify fuel consumption calculation`() {
        val distanceKm = 520.0
        val liters = 43.3
        val totalPrice = 253.30

        val consumption = distanceKm / liters
        val costPerKm = totalPrice / distanceKm

        assertEquals(12.009, consumption, 0.01)
        assertEquals(0.487, costPerKm, 0.01)
    }

    @Test
    fun `verify company mileage reimbursement calculation`() {
        val totalDistanceKm = 1000.0
        val reimbursementRatePerKm = 1.20
        val totalSpent = 750.0

        val totalReimbursement = totalDistanceKm * reimbursementRatePerKm
        val netBalance = totalReimbursement - totalSpent

        assertEquals(1200.0, totalReimbursement, 0.001)
        assertEquals(450.0, netBalance, 0.001)
    }

    @Test
    fun `verify fuel entry edit copy update`() {
        val original = com.example.data.model.FuelEntry(
            id = 1L,
            vehicleId = 1L,
            timestamp = System.currentTimeMillis(),
            liters = 40.0,
            pricePerLiter = 5.50,
            totalPrice = 220.0,
            currentOdometerKm = 45000.0,
            fuelType = "Gasolina Comum",
            stationName = "Posto Shell"
        )

        // Editing price and station
        val edited = original.copy(
            pricePerLiter = 5.60,
            totalPrice = 224.0,
            stationName = "Posto Shell Atualizado"
        )

        assertEquals(1L, edited.id)
        assertEquals(5.60, edited.pricePerLiter, 0.001)
        assertEquals(224.0, edited.totalPrice, 0.001)
        assertEquals("Posto Shell Atualizado", edited.stationName)
    }

    @Test
    fun `verify expense entry edit copy update`() {
        val original = com.example.data.model.ExpenseEntry(
            id = 2L,
            vehicleId = 1L,
            timestamp = System.currentTimeMillis(),
            category = "Estacionamento",
            title = "Estacionamento Aeroporto",
            amount = 35.0
        )

        val edited = original.copy(
            amount = 40.0,
            title = "Estacionamento Aeroporto VIP"
        )

        assertEquals(2L, edited.id)
        assertEquals(40.0, edited.amount, 0.001)
        assertEquals("Estacionamento Aeroporto VIP", edited.title)
    }
}
