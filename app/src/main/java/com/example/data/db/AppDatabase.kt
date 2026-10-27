package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ExpenseDao
import com.example.data.dao.FuelDao
import com.example.data.dao.VehicleDao
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntry
import com.example.data.model.FuelEntry
import com.example.data.model.FuelType
import com.example.data.model.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Vehicle::class, FuelEntry::class, ExpenseEntry::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelDao(): FuelDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autogastos_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(database: AppDatabase) {
                val vehicleDao = database.vehicleDao()
                val fuelDao = database.fuelDao()
                val expenseDao = database.expenseDao()

                // Default vehicle with company mileage reimbursement rate (e.g. R$ 1.15/km)
                val vehicleId = vehicleDao.insert(
                    Vehicle(
                        id = 1,
                        name = "Meu Carro",
                        brandModel = "Corolla 2.0 Flex",
                        plate = "ABC-1D23",
                        initialOdometer = 41200.0,
                        currentOdometer = 42531.0,
                        tankCapacityLiters = 50.0,
                        defaultFuel = FuelType.GASOLINA_COMUM.displayName,
                        companyReimbursementRatePerKm = 1.15,
                        isSelected = true
                    )
                )

                val now = System.currentTimeMillis()
                val dayMillis = 24L * 60 * 60 * 1000

                // 3 realistic fuel logs to show immediate consumption & cost/km
                // First fill-up (baseline): 41,500 km
                fuelDao.insert(
                    FuelEntry(
                        vehicleId = vehicleId,
                        timestamp = now - (20 * dayMillis),
                        currentOdometerKm = 41500.0,
                        distanceTraveledKm = 300.0,
                        fuelType = FuelType.GASOLINA_COMUM.displayName,
                        liters = 44.0,
                        pricePerLiter = 5.79,
                        totalPrice = 254.76,
                        isFullTank = true,
                        consumptionKmPerLiter = null, // baseline
                        costPerKm = null,
                        stationName = "Posto Shell - Av. Central",
                        notes = "Tanque cheio para início da medição"
                    )
                )

                // Second fill-up: 42,020 km (520 km traveled on 43.3L -> 12.01 Km/L, R$ 0.49/Km)
                fuelDao.insert(
                    FuelEntry(
                        vehicleId = vehicleId,
                        timestamp = now - (10 * dayMillis),
                        currentOdometerKm = 42020.0,
                        distanceTraveledKm = 520.0,
                        fuelType = FuelType.GASOLINA_COMUM.displayName,
                        liters = 43.3,
                        pricePerLiter = 5.85,
                        totalPrice = 253.30,
                        isFullTank = true,
                        consumptionKmPerLiter = 12.01,
                        costPerKm = 0.49,
                        stationName = "Posto Ipiranga",
                        notes = "Uso misto cidade e rodovia"
                    )
                )

                // Third fill-up: 42,531 km (511 km traveled on 41.5L -> 12.31 Km/L, R$ 0.48/Km)
                fuelDao.insert(
                    FuelEntry(
                        vehicleId = vehicleId,
                        timestamp = now - (2 * dayMillis),
                        currentOdometerKm = 42531.0,
                        distanceTraveledKm = 511.0,
                        fuelType = FuelType.GASOLINA_ADITIVADA.displayName,
                        liters = 41.5,
                        pricePerLiter = 5.92,
                        totalPrice = 245.68,
                        isFullTank = true,
                        consumptionKmPerLiter = 12.31,
                        costPerKm = 0.48,
                        stationName = "Posto Petrobras BR",
                        notes = "Gasolina aditivada para viagem"
                    )
                )

                // Realistic expenses: parking, toll, wash
                expenseDao.insert(
                    ExpenseEntry(
                        vehicleId = vehicleId,
                        timestamp = now - (14 * dayMillis),
                        category = ExpenseCategory.ESTACIONAMENTO.displayName,
                        title = "Estacionamento Shopping",
                        amount = 25.00,
                        odometerKm = 41850.0,
                        location = "Shopping Morumbi",
                        notes = "Período de 3 horas"
                    )
                )

                expenseDao.insert(
                    ExpenseEntry(
                        vehicleId = vehicleId,
                        timestamp = now - (8 * dayMillis),
                        category = ExpenseCategory.PEDAGIO.displayName,
                        title = "Pedágio Rodovia dos Imigrantes",
                        amount = 35.80,
                        odometerKm = 42100.0,
                        location = "Praça de Pedágio km 32",
                        notes = "Ida para o litoral"
                    )
                )

                expenseDao.insert(
                    ExpenseEntry(
                        vehicleId = vehicleId,
                        timestamp = now - (3 * dayMillis),
                        category = ExpenseCategory.ESTACIONAMENTO.displayName,
                        title = "Estacionamento Centro Médico",
                        amount = 30.00,
                        odometerKm = 42420.0,
                        location = "Estacionamento Valet",
                        notes = "Consulta de rotina"
                    )
                )

                expenseDao.insert(
                    ExpenseEntry(
                        vehicleId = vehicleId,
                        timestamp = now - (1 * dayMillis),
                        category = ExpenseCategory.LAVAGEM.displayName,
                        title = "Lavagem Completa com Cera",
                        amount = 70.00,
                        odometerKm = 42530.0,
                        location = "Lava-Rápido Estrela",
                        notes = "Higienização interna e cera protetora"
                    )
                )
            }
        }
    }
}
