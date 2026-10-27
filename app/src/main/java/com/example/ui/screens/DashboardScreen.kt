package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntry
import com.example.data.model.FuelEntry
import com.example.data.model.Vehicle
import com.example.ui.components.ExpenseCategoryBadge
import com.example.ui.components.FuelTypeBadge
import com.example.ui.components.MetricCard
import com.example.ui.theme.ExpenseParking
import com.example.ui.theme.FuelGasoline
import com.example.ui.viewmodel.MonthlyMetrics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    vehicle: Vehicle?,
    metrics: MonthlyMetrics,
    availableMonths: List<String>,
    selectedMonth: String,
    onSelectMonth: (String) -> Unit,
    fuelEntries: List<FuelEntry>,
    expenseEntries: List<ExpenseEntry>,
    isCloudSyncEnabled: Boolean,
    isSyncing: Boolean,
    onAddFuelClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onFuelClick: (FuelEntry) -> Unit,
    onDeleteFuel: (FuelEntry) -> Unit,
    onExpenseClick: (ExpenseEntry) -> Unit,
    onDeleteExpense: (ExpenseEntry) -> Unit,
    onEditVehicleClick: () -> Unit,
    onEditReimbursementRateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    var fuelToDelete by remember { mutableStateOf<FuelEntry?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntry?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vehicle header card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vehicle_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vehicle?.name ?: "Meu Veículo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${vehicle?.brandModel ?: "Carro"} • ${vehicle?.plate ?: "Placa"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Odômetro: ${String.format(Locale("pt", "BR"), "%,.0f km", vehicle?.currentOdometer ?: 0.0)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Cloud sync status pill & vehicle edit
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCloudSyncEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.testTag("cloud_status_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSyncing) Icons.Filled.Sync else Icons.Filled.CloudDone,
                                    contentDescription = null,
                                    tint = if (isCloudSyncEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSyncing) "Sincronizando" else if (isCloudSyncEnabled) "Nuvem OK" else "Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCloudSyncEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = onEditVehicleClick,
                            modifier = Modifier.size(32.dp).testTag("btn_edit_vehicle_header")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar veículo",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Month Selector row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableMonths.forEach { m ->
                    FilterChip(
                        selected = m == selectedMonth,
                        onClick = { onSelectMonth(m) },
                        label = { Text(m, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("chip_month_$m")
                    )
                }
            }
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddFuelClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_quick_add_fuel"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.LocalGasStation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Abastecer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onAddExpenseClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_quick_add_expense"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.LocalParking, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Despesa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // 2x2 Metric Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Consumo Médio",
                    value = if (metrics.avgConsumptionKmL != null) String.format(Locale("pt", "BR"), "%.2f Km/L", metrics.avgConsumptionKmL) else "-- Km/L",
                    subtitle = "${metrics.fuelCount} abastecimentos",
                    icon = Icons.Filled.LocalGasStation,
                    accentColor = MaterialTheme.colorScheme.primary,
                    testTag = "metric_avg_consumption"
                )

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Custo por Km",
                    value = if (metrics.avgCostPerKm != null) String.format(Locale("pt", "BR"), "R$ %.2f", metrics.avgCostPerKm) else "R$ --",
                    subtitle = "por km rodado",
                    icon = Icons.Filled.Speed,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    testTag = "metric_cost_per_km"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Gasto no Mês",
                    value = String.format(Locale("pt", "BR"), "R$ %,.2f", metrics.totalSpent),
                    subtitle = "Combustível + Despesas",
                    icon = Icons.Filled.AttachMoney,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    testTag = "metric_total_spent"
                )

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Distância Percorrida",
                    value = String.format(Locale("pt", "BR"), "%,.0f km", metrics.totalDistanceKm),
                    subtitle = "${String.format(Locale("pt", "BR"), "%.1f", metrics.totalLiters)} L consumidos",
                    icon = Icons.Filled.DirectionsCar,
                    accentColor = MaterialTheme.colorScheme.primary,
                    testTag = "metric_total_distance"
                )
            }
        }

        // Reembolso por Km da Empresa Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reimbursement_company_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Payments,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Reembolso por Km (Empresa)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Taxa configurada: R$ ${String.format(Locale("pt", "BR"), "%.2f", metrics.reimbursementRatePerKm)} / km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        TextButton(
                            onClick = onEditReimbursementRateClick,
                            modifier = Modifier.testTag("btn_change_reimbursement_rate")
                        ) {
                            Text("Alterar Taxa", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "A Receber da Empresa:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", metrics.totalCompanyReimbursement),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${String.format(Locale("pt", "BR"), "%,.0f km", metrics.totalDistanceKm)} rodados x R$ ${String.format(Locale("pt", "BR"), "%.2f", metrics.reimbursementRatePerKm)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Saldo do Motorista:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            val isPositive = metrics.netBalance >= 0
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPositive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = if (isPositive) String.format(Locale("pt", "BR"), "+ R$ %,.2f", metrics.netBalance)
                                    else String.format(Locale("pt", "BR"), "- R$ %,.2f", kotlin.math.abs(metrics.netBalance)),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) Color(0xFF047857) else Color(0xFFB91C1C),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = if (isPositive) "Reembolso cobre gastos" else "Gasto maior que reembolso",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Expense Breakdown Card (Fuel vs Parking vs Other)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_breakdown_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distribuição de Gastos do Mês",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val total = if (metrics.totalSpent > 0) metrics.totalSpent else 1.0
                    val fuelPct = ((metrics.totalFuelSpent / total) * 100).toFloat()
                    val parkPct = ((metrics.totalParkingSpent / total) * 100).toFloat()
                    val otherPct = ((metrics.totalOtherSpent / total) * 100).toFloat()

                    // Visual Progress Bar
                    LinearProgressIndicator(
                        progress = { (metrics.totalFuelSpent / total).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Combustível", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                String.format(Locale("pt", "BR"), "R$ %,.2f (%.0f%%)", metrics.totalFuelSpent, fuelPct),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseParking))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Estacionamento", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                String.format(Locale("pt", "BR"), "R$ %,.2f (%.0f%%)", metrics.totalParkingSpent, parkPct),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Outros", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                String.format(Locale("pt", "BR"), "R$ %,.2f (%.0f%%)", metrics.totalOtherSpent, otherPct),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Recent Activity Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Atividades Recentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${fuelEntries.size + expenseEntries.size} registros",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Recent Fuel Entries (first 3)
        items(fuelEntries.take(3)) { fuel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFuelClick(fuel) }
                    .testTag("recent_fuel_card_${fuel.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalGasStation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FuelTypeBadge(fuelType = fuel.fuelType)
                            if (!fuel.odometerPhotoUri.isNullOrEmpty() || !fuel.receiptPhotoUri.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = "Tem fotos anexadas",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${fuel.stationName ?: "Abastecimento"} • ${String.format(Locale("pt", "BR"), "%,.0f km", fuel.currentOdometerKm)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${dateFormatter.format(Date(fuel.timestamp))} • ${String.format(Locale("pt", "BR"), "%.1f L a R$ %.2f", fuel.liters, fuel.pricePerLiter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", fuel.totalPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (fuel.consumptionKmPerLiter != null) {
                            Text(
                                text = String.format(Locale("pt", "BR"), "%.1f Km/L", fuel.consumptionKmPerLiter),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            IconButton(
                                onClick = { onFuelClick(fuel) },
                                modifier = Modifier.size(28.dp).testTag("btn_edit_recent_fuel_${fuel.id}")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar abastecimento", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { fuelToDelete = fuel },
                                modifier = Modifier.size(28.dp).testTag("btn_delete_recent_fuel_${fuel.id}")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Excluir abastecimento", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // Recent Expenses (first 2)
        items(expenseEntries.take(2)) { exp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpenseClick(exp) }
                    .testTag("recent_expense_card_${exp.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ExpenseParking.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            tint = ExpenseParking,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        ExpenseCategoryBadge(category = exp.category)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = exp.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${dateFormatter.format(Date(exp.timestamp))} • ${exp.location ?: "Registrado"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", exp.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            IconButton(
                                onClick = { onExpenseClick(exp) },
                                modifier = Modifier.size(28.dp).testTag("btn_edit_recent_expense_${exp.id}")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar despesa", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { expenseToDelete = exp },
                                modifier = Modifier.size(28.dp).testTag("btn_delete_recent_expense_${exp.id}")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Excluir despesa", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Delete Fuel Confirmation Dialog
    fuelToDelete?.let { fuel ->
        AlertDialog(
            onDismissRequest = { fuelToDelete = null },
            title = { Text("Excluir Abastecimento?") },
            text = { Text("Deseja realmente remover o abastecimento de ${String.format(Locale("pt", "BR"), "R$ %,.2f", fuel.totalPrice)} no ${fuel.stationName ?: "posto"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFuel(fuel)
                        fuelToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_fuel_btn")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { fuelToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_fuel_btn")
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Expense Confirmation Dialog
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Excluir Despesa / Ticket?") },
            text = { Text("Deseja realmente remover a despesa \"${exp.title}\" no valor de ${String.format(Locale("pt", "BR"), "R$ %,.2f", exp.amount)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(exp)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_expense_btn")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { expenseToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_expense_btn")
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
