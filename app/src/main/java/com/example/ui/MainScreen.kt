package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntry
import com.example.data.model.FuelEntry
import com.example.ui.screens.AddExpenseDialog
import com.example.ui.screens.AddFuelDialog
import com.example.ui.screens.CloudBackupScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditReimbursementRateDialog
import com.example.ui.screens.EditVehicleDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PhotoViewerDialog
import com.example.ui.screens.ReportsScreen
import com.example.ui.viewmodel.AutoGastosViewModel

@Composable
fun MainScreen(viewModel: AutoGastosViewModel) {
    val vehicle by viewModel.selectedVehicle.collectAsState()
    val fuelEntries by viewModel.fuelEntries.collectAsState()
    val expenseEntries by viewModel.expenseEntries.collectAsState()
    val historyFilter by viewModel.historyFilter.collectAsState()
    val historySearchQuery by viewModel.historySearchQuery.collectAsState()
    val selectedReportMonth by viewModel.selectedReportMonth.collectAsState()
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val uiEventMessage by viewModel.uiEventMessage.collectAsState()

    val availableMonths = remember(fuelEntries, expenseEntries) {
        viewModel.getAvailableMonthYears()
    }
    val metrics = remember(selectedReportMonth, fuelEntries, expenseEntries, vehicle) {
        viewModel.getMetricsForMonth(selectedReportMonth)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showAddFuelDialog by remember { mutableStateOf(false) }
    var editingFuelEntry by remember { mutableStateOf<FuelEntry?>(null) }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingExpenseEntry by remember { mutableStateOf<ExpenseEntry?>(null) }

    var showEditVehicleDialog by remember { mutableStateOf(false) }
    var showEditReimbursementRateDialog by remember { mutableStateOf(false) }
    var viewingPhoto by remember { mutableStateOf<Pair<String, String>?>(null) } // title, path

    LaunchedEffect(uiEventMessage) {
        uiEventMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiEventMessage()
        }
    }

    Scaffold(
        modifier = Modifier.testTag("main_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Filled.LocalGasStation, contentDescription = "Início") },
                    label = { Text("Início", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Filled.History, contentDescription = "Histórico") },
                    label = { Text("Histórico", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_history")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(Icons.Filled.Assessment, contentDescription = "Relatórios") },
                    label = { Text("Relatórios", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_reports")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    icon = { Icon(Icons.Filled.CloudSync, contentDescription = "Nuvem") },
                    label = { Text("Nuvem", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_cloud")
                )
            }
        },
        floatingActionButton = {
            if (selectedTabIndex == 0 || selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = {
                        editingFuelEntry = null
                        showAddFuelDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_fuel")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Novo Abastecimento", modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> DashboardScreen(
                    vehicle = vehicle,
                    metrics = metrics,
                    availableMonths = availableMonths,
                    selectedMonth = selectedReportMonth,
                    onSelectMonth = { viewModel.setSelectedReportMonth(it) },
                    fuelEntries = fuelEntries,
                    expenseEntries = expenseEntries,
                    isCloudSyncEnabled = isCloudSyncEnabled,
                    isSyncing = isSyncing,
                    onAddFuelClick = {
                        editingFuelEntry = null
                        showAddFuelDialog = true
                    },
                    onAddExpenseClick = {
                        editingExpenseEntry = null
                        showAddExpenseDialog = true
                    },
                    onFuelClick = { fuel ->
                        editingFuelEntry = fuel
                        showAddFuelDialog = true
                    },
                    onDeleteFuel = { viewModel.deleteFuelEntry(it) },
                    onExpenseClick = { exp ->
                        editingExpenseEntry = exp
                        showAddExpenseDialog = true
                    },
                    onDeleteExpense = { viewModel.deleteExpenseEntry(it) },
                    onEditVehicleClick = { showEditVehicleDialog = true },
                    onEditReimbursementRateClick = { showEditReimbursementRateDialog = true }
                )
                1 -> HistoryScreen(
                    fuelEntries = fuelEntries,
                    expenseEntries = expenseEntries,
                    selectedFilter = historyFilter,
                    onSelectFilter = { viewModel.setHistoryFilter(it) },
                    searchQuery = historySearchQuery,
                    onSearchQueryChange = { viewModel.setHistorySearchQuery(it) },
                    onEditFuel = { fuel ->
                        editingFuelEntry = fuel
                        showAddFuelDialog = true
                    },
                    onDeleteFuel = { viewModel.deleteFuelEntry(it) },
                    onEditExpense = { exp ->
                        editingExpenseEntry = exp
                        showAddExpenseDialog = true
                    },
                    onDeleteExpense = { viewModel.deleteExpenseEntry(it) },
                    onViewPhoto = { title, path ->
                        viewingPhoto = Pair(title, path)
                    }
                )
                2 -> ReportsScreen(
                    vehicle = vehicle,
                    availableMonths = availableMonths,
                    selectedMonth = selectedReportMonth,
                    onSelectMonth = { viewModel.setSelectedReportMonth(it) },
                    metrics = metrics,
                    fuelEntries = fuelEntries,
                    expenseEntries = expenseEntries,
                    onShowMessage = { viewModel.clearUiEventMessage() }
                )
                3 -> CloudBackupScreen(
                    vehicle = vehicle,
                    isCloudSyncEnabled = isCloudSyncEnabled,
                    onToggleCloudSync = { viewModel.toggleCloudSync(it) },
                    lastBackupTime = lastBackupTime,
                    isSyncing = isSyncing,
                    onTriggerSync = { viewModel.triggerCloudBackup() },
                    onExportBackup = { callback -> viewModel.exportBackupFile(callback) },
                    onRestoreBackup = { json -> viewModel.restoreBackupContent(json) },
                    onEditVehicleClick = { showEditVehicleDialog = true },
                    onShowMessage = { viewModel.clearUiEventMessage() }
                )
            }
        }
    }

    // Add / Edit Fuel Dialog
    if (showAddFuelDialog) {
        val latestOdo = fuelEntries.firstOrNull()?.currentOdometerKm ?: (vehicle?.currentOdometer ?: 0.0)
        AddFuelDialog(
            initialEntry = editingFuelEntry,
            latestOdometer = latestOdo,
            onDismiss = {
                showAddFuelDialog = false
                editingFuelEntry = null
            },
            onSave = { entry ->
                if (editingFuelEntry != null) {
                    viewModel.updateFuelEntry(entry)
                } else {
                    viewModel.addFuelEntry(entry)
                }
                showAddFuelDialog = false
                editingFuelEntry = null
            },
            onDelete = if (editingFuelEntry != null) {
                {
                    editingFuelEntry?.let { viewModel.deleteFuelEntry(it) }
                    showAddFuelDialog = false
                    editingFuelEntry = null
                }
            } else null,
            onViewPhoto = { title, path ->
                viewingPhoto = Pair(title, path)
            }
        )
    }

    // Add / Edit Expense Dialog
    if (showAddExpenseDialog) {
        val currentOdo = vehicle?.currentOdometer ?: 0.0
        AddExpenseDialog(
            initialEntry = editingExpenseEntry,
            currentOdometer = currentOdo,
            onDismiss = {
                showAddExpenseDialog = false
                editingExpenseEntry = null
            },
            onSave = { entry ->
                if (editingExpenseEntry != null) {
                    viewModel.updateExpenseEntry(entry)
                } else {
                    viewModel.addExpenseEntry(entry)
                }
                showAddExpenseDialog = false
                editingExpenseEntry = null
            },
            onDelete = if (editingExpenseEntry != null) {
                {
                    editingExpenseEntry?.let { viewModel.deleteExpenseEntry(it) }
                    showAddExpenseDialog = false
                    editingExpenseEntry = null
                }
            } else null,
            onViewPhoto = { title, path ->
                viewingPhoto = Pair(title, path)
            }
        )
    }

    // Edit Vehicle Dialog
    if (showEditVehicleDialog && vehicle != null) {
        EditVehicleDialog(
            vehicle = vehicle!!,
            onDismiss = { showEditVehicleDialog = false },
            onSave = { name, model, plate, tankCapacity, reimbursementRate ->
                viewModel.updateVehicleInfo(name, model, plate, tankCapacity, reimbursementRate)
                showEditVehicleDialog = false
            }
        )
    }

    // Quick Edit Reimbursement Rate Dialog
    if (showEditReimbursementRateDialog && vehicle != null) {
        EditReimbursementRateDialog(
            currentRate = vehicle!!.companyReimbursementRatePerKm,
            onDismiss = { showEditReimbursementRateDialog = false },
            onSaveRate = { rate ->
                viewModel.updateCompanyReimbursementRate(rate)
                showEditReimbursementRateDialog = false
            }
        )
    }

    // Full-screen Photo Viewer Dialog
    viewingPhoto?.let { (title, path) ->
        PhotoViewerDialog(
            title = title,
            photoPath = path,
            onDismiss = { viewingPhoto = null }
        )
    }
}
