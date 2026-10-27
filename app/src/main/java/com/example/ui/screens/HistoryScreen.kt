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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.components.ExpenseCategoryBadge
import com.example.ui.components.FuelTypeBadge
import com.example.ui.theme.ExpenseParking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class HistoryItem(val timestamp: Long) {
    data class Fuel(val entry: FuelEntry) : HistoryItem(entry.timestamp)
    data class Expense(val entry: ExpenseEntry) : HistoryItem(entry.timestamp)
}

@Composable
fun HistoryScreen(
    fuelEntries: List<FuelEntry>,
    expenseEntries: List<ExpenseEntry>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditFuel: (FuelEntry) -> Unit,
    onDeleteFuel: (FuelEntry) -> Unit,
    onEditExpense: (ExpenseEntry) -> Unit,
    onDeleteExpense: (ExpenseEntry) -> Unit,
    onViewPhoto: (title: String, path: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    val filters = listOf("Todos", "Combustível", "Estacionamento", "Pedágio", "Manutenção", "Outros")

    // Active item details modal
    var activeDetailItem by remember { mutableStateOf<HistoryItem?>(null) }
    var itemToDelete by remember { mutableStateOf<HistoryItem?>(null) }

    // Combine & filter
    val combinedItems = remember(fuelEntries, expenseEntries, selectedFilter, searchQuery) {
        val list = mutableListOf<HistoryItem>()

        if (selectedFilter == "Todos" || selectedFilter == "Combustível") {
            fuelEntries.forEach { f ->
                val matchesSearch = searchQuery.isBlank() ||
                        (f.stationName?.contains(searchQuery, ignoreCase = true) == true) ||
                        f.fuelType.contains(searchQuery, ignoreCase = true) ||
                        (f.notes?.contains(searchQuery, ignoreCase = true) == true)
                if (matchesSearch) list.add(HistoryItem.Fuel(f))
            }
        }

        if (selectedFilter != "Combustível") {
            expenseEntries.forEach { e ->
                val matchesCategory = when (selectedFilter) {
                    "Todos" -> true
                    "Estacionamento" -> e.category.contains("Estacionamento", ignoreCase = true)
                    "Pedágio" -> e.category.contains("Pedágio", ignoreCase = true)
                    "Manutenção" -> e.category.contains("Manutenção", ignoreCase = true) || e.category.contains("Óleo", ignoreCase = true)
                    "Outros" -> !e.category.contains("Estacionamento", ignoreCase = true) && !e.category.contains("Pedágio", ignoreCase = true) && !e.category.contains("Manutenção", ignoreCase = true)
                    else -> e.category.equals(selectedFilter, ignoreCase = true)
                }

                val matchesSearch = searchQuery.isBlank() ||
                        e.title.contains(searchQuery, ignoreCase = true) ||
                        e.category.contains(searchQuery, ignoreCase = true) ||
                        (e.location?.contains(searchQuery, ignoreCase = true) == true) ||
                        (e.notes?.contains(searchQuery, ignoreCase = true) == true)

                if (matchesCategory && matchesSearch) {
                    list.add(HistoryItem.Expense(e))
                }
            }
        }

        list.sortedByDescending { it.timestamp }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Buscar posto, estacionamento, local...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Limpar busca")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_history_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { f ->
                FilterChip(
                    selected = f == selectedFilter,
                    onClick = { onSelectFilter(f) },
                    label = { Text(f, fontSize = 13.sp) },
                    modifier = Modifier.testTag("filter_chip_$f")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results count
        Text(
            text = "${combinedItems.size} registros encontrados",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (combinedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum registro encontrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(combinedItems) { item ->
                    when (item) {
                        is HistoryItem.Fuel -> {
                            val fuel = item.entry
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeDetailItem = item }
                                    .testTag("history_fuel_item_${fuel.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FuelTypeBadge(fuelType = fuel.fuelType)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = dateFormatter.format(Date(fuel.timestamp)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", fuel.totalPrice),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = fuel.stationName ?: "Posto de Combustível",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Odômetro: ${String.format(Locale("pt", "BR"), "%,.0f km", fuel.currentOdometerKm)}${if (fuel.distanceTraveledKm != null) " (+${String.format(Locale("pt", "BR"), "%,.0f km", fuel.distanceTraveledKm)})" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = String.format(Locale("pt", "BR"), "%.1f L (R$ %.2f/L)", fuel.liters, fuel.pricePerLiter),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (fuel.consumptionKmPerLiter != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    Text(
                                                        text = String.format(Locale("pt", "BR"), "%.2f Km/L", fuel.consumptionKmPerLiter),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Photos indicator badge
                                    if (!fuel.odometerPhotoUri.isNullOrEmpty() || !fuel.receiptPhotoUri.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.CameraAlt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (!fuel.odometerPhotoUri.isNullOrEmpty() && !fuel.receiptPhotoUri.isNullOrEmpty()) "Odômetro e Comprovante anexados"
                                                else if (!fuel.odometerPhotoUri.isNullOrEmpty()) "Foto do odômetro anexada"
                                                else "Comprovante fiscal anexado",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { onEditFuel(fuel) },
                                            modifier = Modifier.testTag("btn_history_edit_fuel_${fuel.id}")
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Editar", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        TextButton(
                                            onClick = { itemToDelete = HistoryItem.Fuel(fuel) },
                                            modifier = Modifier.testTag("btn_history_delete_fuel_${fuel.id}")
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Excluir", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        is HistoryItem.Expense -> {
                            val exp = item.entry
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeDetailItem = item }
                                    .testTag("history_expense_item_${exp.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ExpenseCategoryBadge(category = exp.category)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = dateFormatter.format(Date(exp.timestamp)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", exp.amount),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = exp.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (!exp.location.isNullOrEmpty()) {
                                                Text(
                                                    text = "Local: ${exp.location}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (exp.odometerKm != null) {
                                            Text(
                                                text = String.format(Locale("pt", "BR"), "%,.0f km", exp.odometerKm),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (!exp.receiptPhotoUri.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Receipt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Comprovante anexado",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { onEditExpense(exp) },
                                            modifier = Modifier.testTag("btn_history_edit_expense_${exp.id}")
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Editar", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        TextButton(
                                            onClick = { itemToDelete = HistoryItem.Expense(exp) },
                                            modifier = Modifier.testTag("btn_history_delete_expense_${exp.id}")
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Excluir", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                        }
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
        }
    }

    // Detail & Actions Dialog
    activeDetailItem?.let { item ->
        AlertDialog(
            onDismissRequest = { activeDetailItem = null },
            title = {
                Text(
                    text = when (item) {
                        is HistoryItem.Fuel -> "Detalhes do Abastecimento"
                        is HistoryItem.Expense -> "Detalhes da Despesa"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (item) {
                        is HistoryItem.Fuel -> {
                            val f = item.entry
                            Text("Data: ${dateFormatter.format(Date(f.timestamp))}")
                            Text("Posto: ${f.stationName ?: "Não informado"}")
                            Text("Combustível: ${f.fuelType}")
                            Text("Odômetro: ${String.format(Locale("pt", "BR"), "%,.0f km", f.currentOdometerKm)}")
                            if (f.distanceTraveledKm != null) {
                                Text("Km Rodados: ${String.format(Locale("pt", "BR"), "%,.0f km", f.distanceTraveledKm)}")
                            }
                            Text("Volume: ${String.format(Locale("pt", "BR"), "%.2f Litros", f.liters)}")
                            Text("Preço/Litro: R$ ${String.format(Locale("pt", "BR"), "%.2f", f.pricePerLiter)}")
                            Text("Total Pago: R$ ${String.format(Locale("pt", "BR"), "%.2f", f.totalPrice)}", fontWeight = FontWeight.Bold)
                            if (f.consumptionKmPerLiter != null) {
                                Text("Consumo Médio: ${String.format(Locale("pt", "BR"), "%.2f Km/L", f.consumptionKmPerLiter)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            if (f.costPerKm != null) {
                                Text("Custo por Km: R$ ${String.format(Locale("pt", "BR"), "%.2f / km", f.costPerKm)}")
                            }
                            if (!f.notes.isNullOrEmpty()) {
                                Text("Notas: ${f.notes}")
                            }

                            // Photo preview buttons
                            if (!f.odometerPhotoUri.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { onViewPhoto("Foto do Odômetro", f.odometerPhotoUri) },
                                    modifier = Modifier.fillMaxWidth().testTag("view_odometer_photo_btn")
                                ) {
                                    Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ver Foto do Odômetro")
                                }
                            }

                            if (!f.receiptPhotoUri.isNullOrEmpty()) {
                                OutlinedButton(
                                    onClick = { onViewPhoto("Comprovante Fiscal", f.receiptPhotoUri) },
                                    modifier = Modifier.fillMaxWidth().testTag("view_receipt_photo_btn")
                                ) {
                                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ver Comprovante Fiscal")
                                }
                            }
                        }
                        is HistoryItem.Expense -> {
                            val e = item.entry
                            Text("Data: ${dateFormatter.format(Date(e.timestamp))}")
                            Text("Categoria: ${e.category}")
                            Text("Descrição: ${e.title}")
                            Text("Valor: R$ ${String.format(Locale("pt", "BR"), "%.2f", e.amount)}", fontWeight = FontWeight.Bold)
                            if (!e.location.isNullOrEmpty()) Text("Local: ${e.location}")
                            if (e.odometerKm != null) Text("Odômetro: ${String.format(Locale("pt", "BR"), "%,.0f km", e.odometerKm)}")
                            if (!e.notes.isNullOrEmpty()) Text("Observações: ${e.notes}")

                            if (!e.receiptPhotoUri.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { onViewPhoto("Comprovante - ${e.title}", e.receiptPhotoUri) },
                                    modifier = Modifier.fillMaxWidth().testTag("view_expense_receipt_btn")
                                ) {
                                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ver Foto do Comprovante")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toEdit = activeDetailItem
                        activeDetailItem = null
                        when (toEdit) {
                            is HistoryItem.Fuel -> onEditFuel(toEdit.entry)
                            is HistoryItem.Expense -> onEditExpense(toEdit.entry)
                            null -> {}
                        }
                    },
                    modifier = Modifier.testTag("modal_edit_btn")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        itemToDelete = activeDetailItem
                        activeDetailItem = null
                    },
                    modifier = Modifier.testTag("modal_delete_btn")
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // Delete confirmation dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Excluir Registro?") },
            text = { Text("Esta ação não pode ser desfeita. Deseja realmente remover este registro?") },
            confirmButton = {
                Button(
                    onClick = {
                        when (item) {
                            is HistoryItem.Fuel -> onDeleteFuel(item.entry)
                            is HistoryItem.Expense -> onDeleteExpense(item.entry)
                        }
                        itemToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { itemToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_btn")
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
