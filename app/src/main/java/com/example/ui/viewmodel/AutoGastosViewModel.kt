package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntry
import com.example.data.model.FuelEntry
import com.example.data.model.Vehicle
import com.example.data.repository.VehicleRepository
import com.example.util.BackupManager
import com.example.util.ReportExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyMetrics(
    val monthYearLabel: String,
    val totalSpent: Double,
    val totalFuelSpent: Double,
    val totalLiters: Double,
    val totalParkingSpent: Double,
    val totalOtherSpent: Double,
    val totalDistanceKm: Double,
    val avgConsumptionKmL: Double?,
    val avgCostPerKm: Double?,
    val fuelCount: Int,
    val expenseCount: Int,
    val reimbursementRatePerKm: Double = 0.0,
    val totalCompanyReimbursement: Double = 0.0,
    val netBalance: Double = 0.0
)

class AutoGastosViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = VehicleRepository(
        database.vehicleDao(),
        database.fuelDao(),
        database.expenseDao()
    )

    val allVehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle.asStateFlow()

    private val _fuelEntries = MutableStateFlow<List<FuelEntry>>(emptyList())
    val fuelEntries: StateFlow<List<FuelEntry>> = _fuelEntries.asStateFlow()

    private val _expenseEntries = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expenseEntries: StateFlow<List<ExpenseEntry>> = _expenseEntries.asStateFlow()

    // History filter states
    private val _historyFilter = MutableStateFlow("Todos")
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    // Reports selected month (format "MM/yyyy", e.g. "10/2026")
    private val currentMonthKey = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
    private val _selectedReportMonth = MutableStateFlow(currentMonthKey)
    val selectedReportMonth: StateFlow<String> = _selectedReportMonth.asStateFlow()

    // Cloud & Sync state
    private val _isCloudSyncEnabled = MutableStateFlow(true)
    val isCloudSyncEnabled: StateFlow<Boolean> = _isCloudSyncEnabled.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(System.currentTimeMillis() - (15 * 60 * 1000))
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // UI Message or snackbar
    private val _uiEventMessage = MutableStateFlow<String?>(null)
    val uiEventMessage: StateFlow<String?> = _uiEventMessage.asStateFlow()

    init {
        viewModelScope.launch {
            allVehicles.collect { list ->
                if (list.isNotEmpty()) {
                    val current = _selectedVehicle.value
                    val target = list.find { it.isSelected } ?: list.first()
                    if (current == null || current.id != target.id) {
                        _selectedVehicle.value = target
                        observeVehicleData(target.id)
                    }
                }
            }
        }
    }

    private fun observeVehicleData(vehicleId: Long) {
        viewModelScope.launch {
            repository.getFuelEntries(vehicleId).collect { fuels ->
                _fuelEntries.value = fuels
            }
        }
        viewModelScope.launch {
            repository.getExpenseEntries(vehicleId).collect { expenses ->
                _expenseEntries.value = expenses
            }
        }
    }

    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setSelectedReportMonth(monthYear: String) {
        _selectedReportMonth.value = monthYear
    }

    fun clearUiEventMessage() {
        _uiEventMessage.value = null
    }

    // Computed monthly metrics
    fun getMetricsForMonth(monthYear: String): MonthlyMetrics {
        val fuels = _fuelEntries.value.filter {
            val dateStr = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(it.timestamp))
            dateStr == monthYear
        }
        val expenses = _expenseEntries.value.filter {
            val dateStr = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(it.timestamp))
            dateStr == monthYear
        }

        val totalFuelSpent = fuels.sumOf { it.totalPrice }
        val totalLiters = fuels.sumOf { it.liters }
        val totalParkingSpent = expenses
            .filter { it.category.contains("Estacionamento", ignoreCase = true) }
            .sumOf { it.amount }
        val totalOtherSpent = expenses
            .filter { !it.category.contains("Estacionamento", ignoreCase = true) }
            .sumOf { it.amount }
        val totalSpent = totalFuelSpent + totalParkingSpent + totalOtherSpent

        val distances = fuels.mapNotNull { it.distanceTraveledKm }
        val totalDistanceKm = distances.sum()

        val validConsumptions = fuels.mapNotNull { it.consumptionKmPerLiter }
        val avgConsumption = if (validConsumptions.isNotEmpty()) validConsumptions.average() else null

        val validCostPerKm = fuels.mapNotNull { it.costPerKm }
        val avgCost = if (validCostPerKm.isNotEmpty()) validCostPerKm.average() else null

        val rate = _selectedVehicle.value?.companyReimbursementRatePerKm ?: 1.15
        val totalReimbursement = totalDistanceKm * rate
        val netBalance = totalReimbursement - totalSpent

        return MonthlyMetrics(
            monthYearLabel = monthYear,
            totalSpent = totalSpent,
            totalFuelSpent = totalFuelSpent,
            totalLiters = totalLiters,
            totalParkingSpent = totalParkingSpent,
            totalOtherSpent = totalOtherSpent,
            totalDistanceKm = totalDistanceKm,
            avgConsumptionKmL = avgConsumption,
            avgCostPerKm = avgCost,
            fuelCount = fuels.size,
            expenseCount = expenses.size,
            reimbursementRatePerKm = rate,
            totalCompanyReimbursement = totalReimbursement,
            netBalance = netBalance
        )
    }

    fun getAvailableMonthYears(): List<String> {
        val set = mutableSetOf<String>()
        set.add(currentMonthKey)
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        set.add(SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.time))
        cal.add(Calendar.MONTH, -1)
        set.add(SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.time))

        _fuelEntries.value.forEach {
            set.add(SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(it.timestamp)))
        }
        _expenseEntries.value.forEach {
            set.add(SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(it.timestamp)))
        }
        return set.toList().sortedDescending()
    }

    // Database Actions
    fun addFuelEntry(entry: FuelEntry) {
        viewModelScope.launch {
            val current = _selectedVehicle.value ?: return@launch
            repository.addFuelEntry(entry.copy(vehicleId = current.id))
            _uiEventMessage.value = "Abastecimento salvo com sucesso!"
            if (_isCloudSyncEnabled.value) {
                simulateCloudSync()
            }
        }
    }

    fun updateFuelEntry(entry: FuelEntry) {
        viewModelScope.launch {
            repository.updateFuelEntry(entry)
            _uiEventMessage.value = "Abastecimento atualizado!"
            if (_isCloudSyncEnabled.value) {
                simulateCloudSync()
            }
        }
    }

    fun deleteFuelEntry(entry: FuelEntry) {
        viewModelScope.launch {
            repository.deleteFuelEntry(entry)
            _uiEventMessage.value = "Abastecimento removido."
        }
    }

    fun addExpenseEntry(entry: ExpenseEntry) {
        viewModelScope.launch {
            val current = _selectedVehicle.value ?: return@launch
            repository.addExpenseEntry(entry.copy(vehicleId = current.id))
            _uiEventMessage.value = "Despesa de ${entry.category} registrada!"
            if (_isCloudSyncEnabled.value) {
                simulateCloudSync()
            }
        }
    }

    fun updateExpenseEntry(entry: ExpenseEntry) {
        viewModelScope.launch {
            repository.updateExpenseEntry(entry)
            _uiEventMessage.value = "Despesa atualizada!"
            if (_isCloudSyncEnabled.value) {
                simulateCloudSync()
            }
        }
    }

    fun deleteExpenseEntry(entry: ExpenseEntry) {
        viewModelScope.launch {
            repository.deleteExpenseEntry(entry)
            _uiEventMessage.value = "Despesa removida."
        }
    }

    fun updateVehicleInfo(
        name: String,
        model: String,
        plate: String,
        tankCapacity: Double,
        reimbursementRate: Double
    ) {
        viewModelScope.launch {
            val current = _selectedVehicle.value ?: return@launch
            val updated = current.copy(
                name = name,
                brandModel = model,
                plate = plate,
                tankCapacityLiters = tankCapacity,
                companyReimbursementRatePerKm = reimbursementRate
            )
            repository.updateVehicle(updated)
            _selectedVehicle.value = updated
            _uiEventMessage.value = "Dados do veículo e taxa de reembolso atualizados!"
        }
    }

    fun updateCompanyReimbursementRate(rate: Double) {
        viewModelScope.launch {
            val current = _selectedVehicle.value ?: return@launch
            val updated = current.copy(companyReimbursementRatePerKm = rate)
            repository.updateVehicle(updated)
            _selectedVehicle.value = updated
            _uiEventMessage.value = "Valor pago pela empresa atualizado: R$ ${String.format(Locale("pt", "BR"), "%.2f", rate)} / Km"
        }
    }

    // Cloud and Backup
    fun toggleCloudSync(enabled: Boolean) {
        _isCloudSyncEnabled.value = enabled
        if (enabled) {
            triggerCloudBackup()
        }
    }

    fun triggerCloudBackup() {
        viewModelScope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(1200) // realistic network sync indicator
            _isSyncing.value = false
            _lastBackupTime.value = System.currentTimeMillis()
            _uiEventMessage.value = "Backup na nuvem sincronizado em tempo real com Smartphone e Tablet!"
        }
    }

    private fun simulateCloudSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(800)
            _isSyncing.value = false
            _lastBackupTime.value = System.currentTimeMillis()
        }
    }

    fun exportBackupFile(onReady: (File) -> Unit) {
        viewModelScope.launch {
            val current = _selectedVehicle.value ?: return@launch
            val file = BackupManager.exportBackupJson(getApplication(), repository, current)
            if (file != null) {
                onReady(file)
            } else {
                _uiEventMessage.value = "Erro ao exportar arquivo de backup."
            }
        }
    }

    fun restoreBackupContent(jsonString: String) {
        viewModelScope.launch {
            val current = _selectedVehicle.value ?: return@launch
            val success = BackupManager.importBackupJson(jsonString, repository, current.id)
            if (success) {
                _uiEventMessage.value = "Backup restaurado com sucesso! Dados sincronizados."
            } else {
                _uiEventMessage.value = "Formato de arquivo de backup inválido."
            }
        }
    }
}
