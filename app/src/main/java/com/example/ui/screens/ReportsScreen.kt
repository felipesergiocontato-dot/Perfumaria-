package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.viewmodel.MonthlyMetrics
import com.example.util.ReportExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    vehicle: Vehicle?,
    availableMonths: List<String>,
    selectedMonth: String,
    onSelectMonth: (String) -> Unit,
    metrics: MonthlyMetrics,
    fuelEntries: List<FuelEntry>,
    expenseEntries: List<ExpenseEntry>,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    // Filter data for selected month
    val monthFuels = remember(fuelEntries, selectedMonth) {
        fuelEntries.filter {
            SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(it.timestamp)) == selectedMonth
        }
    }
    val monthExpenses = remember(expenseEntries, selectedMonth) {
        expenseEntries.filter {
            SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(it.timestamp)) == selectedMonth
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Relatórios Mensais & Exportação",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gere relatórios completos com métricas, km rodados e recibos em PDF, Excel e CSV.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Month selector
        item {
            Text(
                text = "Selecione o Mês de Referência:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
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
                        label = { Text(m, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("report_month_chip_$m")
                    )
                }
            }
        }

        // Monthly Summary Banner
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_report_summary_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumo Executivo • $selectedMonth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Geral Gasto", style = MaterialTheme.typography.labelSmall)
                            Text(
                                String.format(Locale("pt", "BR"), "R$ %,.2f", metrics.totalSpent),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Consumo Médio", style = MaterialTheme.typography.labelSmall)
                            Text(
                                if (metrics.avgConsumptionKmL != null) String.format(Locale("pt", "BR"), "%.2f Km/L", metrics.avgConsumptionKmL) else "N/D",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Combustível: R$ ${String.format(Locale("pt", "BR"), "%,.2f", metrics.totalFuelSpent)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Estacionamento: R$ ${String.format(Locale("pt", "BR"), "%,.2f", metrics.totalParkingSpent)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseParking
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Distância: ${String.format(Locale("pt", "BR"), "%,.0f km", metrics.totalDistanceKm)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Custo/Km: ${if (metrics.avgCostPerKm != null) String.format(Locale("pt", "BR"), "R$ %.2f / km", metrics.avgCostPerKm) else "N/D"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reembolso da Empresa (${String.format(Locale("pt", "BR"), "R$ %.2f", metrics.reimbursementRatePerKm)}/km):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", metrics.totalCompanyReimbursement),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Saldo Líquido do Motorista:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        val isPos = metrics.netBalance >= 0
                        Text(
                            text = if (isPos) String.format(Locale("pt", "BR"), "+ R$ %,.2f", metrics.netBalance)
                            else String.format(Locale("pt", "BR"), "- R$ %,.2f", kotlin.math.abs(metrics.netBalance)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPos) Color(0xFF047857) else Color(0xFFB91C1C)
                        )
                    }
                }
            }
        }

        // Export Actions Section
        item {
            Text(
                text = "Formatos de Exportação Disponíveis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // 1. PDF Export
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_pdf_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Relatório Mensal PDF", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Documento formatado pronto para envio, impressão ou arquivamento.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            if (vehicle == null) {
                                onShowMessage("Nenhum veículo selecionado.")
                                return@Button
                            }
                            val file = ReportExporter.generatePdfReport(
                                context = context,
                                vehicle = vehicle,
                                monthYear = selectedMonth,
                                fuelList = monthFuels,
                                expenseList = monthExpenses
                            )
                            if (file != null) {
                                ReportExporter.shareFile(context, file, "application/pdf", "Relatório PDF $selectedMonth")
                                onShowMessage("Relatório PDF gerado com sucesso!")
                            } else {
                                onShowMessage("Erro ao gerar PDF.")
                            }
                        },
                        modifier = Modifier.testTag("btn_export_pdf"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Excel Export
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_excel_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TableChart,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Planilha Excel (.xls)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Múltiplas abas com resumo, combustíveis e gastos operacionais.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            if (vehicle == null) {
                                onShowMessage("Nenhum veículo selecionado.")
                                return@Button
                            }
                            val file = ReportExporter.generateExcelReport(
                                context = context,
                                vehicle = vehicle,
                                monthYear = selectedMonth,
                                fuelList = monthFuels,
                                expenseList = monthExpenses
                            )
                            if (file != null) {
                                ReportExporter.shareFile(context, file, "application/vnd.ms-excel", "Planilha Excel $selectedMonth")
                                onShowMessage("Planilha Excel gerada!")
                            } else {
                                onShowMessage("Erro ao gerar Excel.")
                            }
                        },
                        modifier = Modifier.testTag("btn_export_excel"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. CSV Export
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_csv_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Arquivo CSV (.csv)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Formato aberto com separador ponto e vírgula e suporte a acentos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            if (vehicle == null) {
                                onShowMessage("Nenhum veículo selecionado.")
                                return@Button
                            }
                            val file = ReportExporter.generateCsvReport(
                                context = context,
                                vehicle = vehicle,
                                monthYear = selectedMonth,
                                fuelList = monthFuels,
                                expenseList = monthExpenses
                            )
                            if (file != null) {
                                ReportExporter.shareFile(context, file, "text/csv", "Dados CSV $selectedMonth")
                                onShowMessage("Arquivo CSV gerado!")
                            } else {
                                onShowMessage("Erro ao gerar CSV.")
                            }
                        },
                        modifier = Modifier.testTag("btn_export_csv"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Audit preview list
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prévia dos Registros do Mês (${monthFuels.size + monthExpenses.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (monthFuels.isEmpty() && monthExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Não há lançamentos no mês $selectedMonth.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(monthFuels) { fuel ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FuelTypeBadge(fuelType = fuel.fuelType)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fuel.stationName ?: "Abastecimento", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("${dateFormatter.format(Date(fuel.timestamp))} • ${String.format(Locale("pt", "BR"), "%.1f L", fuel.liters)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            String.format(Locale("pt", "BR"), "R$ %,.2f", fuel.totalPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            items(monthExpenses) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpenseCategoryBadge(category = exp.category)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exp.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(dateFormatter.format(Date(exp.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            String.format(Locale("pt", "BR"), "R$ %,.2f", exp.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
