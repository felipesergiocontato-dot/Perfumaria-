package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.ExpenseEntry
import com.example.data.model.FuelEntry
import com.example.data.model.Vehicle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val dateOnlyFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val monthYearFormatter = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))

    fun generatePdfReport(
        context: Context,
        vehicle: Vehicle,
        monthYear: String,
        fuelList: List<FuelEntry>,
        expenseList: List<ExpenseEntry>
    ): File? {
        return try {
            val pdfDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val cleanMonth = monthYear.replace("/", "_").replace(" ", "_")
            val pdfFile = File(pdfDir, "Relatorio_AutoGastos_$cleanMonth.pdf")

            val document = PdfDocument()
            val pageWidth = 595 // A4 standard width in points at 72 dpi
            val pageHeight = 842 // A4 standard height

            // Paint styles
            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // slate 900
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(100, 116, 139) // slate 500
                textSize = 11f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val sectionHeaderPaint = Paint().apply {
                color = Color.rgb(13, 148, 136) // teal
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val headerTextPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 9f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val cardBgPaint = Paint().apply {
                color = Color.rgb(241, 245, 249) // slate 100
                style = Paint.Style.FILL
            }

            val cardBorderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225) // slate 300
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val accentBgPaint = Paint().apply {
                color = Color.rgb(204, 251, 241) // teal container
                style = Paint.Style.FILL
            }

            // Calculations
            val totalFuelSpent = fuelList.sumOf { it.totalPrice }
            val totalLiters = fuelList.sumOf { it.liters }
            val totalParkingSpent = expenseList.filter { it.category.contains("Estacionamento", ignoreCase = true) }.sumOf { it.amount }
            val totalOtherExpenses = expenseList.filter { !it.category.contains("Estacionamento", ignoreCase = true) }.sumOf { it.amount }
            val totalSpent = totalFuelSpent + totalParkingSpent + totalOtherExpenses

            val fuelDistanceList = fuelList.mapNotNull { it.distanceTraveledKm }
            val totalDistanceKm = fuelDistanceList.sum()
            val validConsumptions = fuelList.mapNotNull { it.consumptionKmPerLiter }
            val avgConsumption = if (validConsumptions.isNotEmpty()) validConsumptions.average() else null
            val validCostPerKm = fuelList.mapNotNull { it.costPerKm }
            val avgCostPerKm = if (validCostPerKm.isNotEmpty()) validCostPerKm.average() else null

            // Start Page 1
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = 45f

            // App branding bar
            canvas.drawRect(RectF(40f, y, pageWidth - 40f, y + 4f), Paint().apply {
                color = Color.rgb(13, 148, 136)
                style = Paint.Style.FILL
            })
            y += 24f

            // Header Title
            canvas.drawText("AutoGastos - Relatório Mensal de Gastos", 40f, y, titlePaint)
            y += 18f
            canvas.drawText(
                "Período: $monthYear  •  Veículo: ${vehicle.name} (${vehicle.brandModel} - ${vehicle.plate})",
                40f,
                y,
                subtitlePaint
            )
            y += 14f
            val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date())
            canvas.drawText("Gerado em: $nowStr  •  Odômetro atual: ${String.format(Locale("pt", "BR"), "%,.0f km", vehicle.currentOdometer)}", 40f, y, subtitlePaint)
            y += 26f

            // Executive Summary Card (Grid)
            canvas.drawRoundRect(RectF(40f, y, pageWidth - 40f, y + 128f), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(40f, y, pageWidth - 40f, y + 128f), 8f, 8f, cardBorderPaint)

            // Highlight bar on top of card
            canvas.drawRoundRect(RectF(40f, y, pageWidth - 40f, y + 26f), 8f, 8f, accentBgPaint)
            canvas.drawText("RESUMO FINANCEIRO, TELEMETRIA & REEMBOLSO EMPRESA", 52f, y + 17f, sectionHeaderPaint)

            val col1X = 52f
            val col2X = 220f
            val col3X = 390f

            val row1Y = y + 46f
            val row2Y = y + 66f
            val row3Y = y + 86f
            val row4Y = y + 108f

            canvas.drawText("Total Geral Gasto: R$ ${String.format(Locale("pt", "BR"), "%,.2f", totalSpent)}", col1X, row1Y, headerTextPaint)
            canvas.drawText("Combustível: R$ ${String.format(Locale("pt", "BR"), "%,.2f", totalFuelSpent)}", col1X, row2Y, bodyPaint)
            canvas.drawText("Volume: ${String.format(Locale("pt", "BR"), "%.1f L", totalLiters)}", col1X, row3Y, bodyPaint)

            canvas.drawText("Estacionamento: R$ ${String.format(Locale("pt", "BR"), "%,.2f", totalParkingSpent)}", col2X, row1Y, headerTextPaint)
            canvas.drawText("Outras Despesas: R$ ${String.format(Locale("pt", "BR"), "%,.2f", totalOtherExpenses)}", col2X, row2Y, bodyPaint)
            canvas.drawText("Distância Rodada: ${String.format(Locale("pt", "BR"), "%,.0f km", totalDistanceKm)}", col2X, row3Y, bodyPaint)

            val avgConsText = if (avgConsumption != null) String.format(Locale("pt", "BR"), "%.2f Km/L", avgConsumption) else "N/D"
            val avgCostText = if (avgCostPerKm != null) String.format(Locale("pt", "BR"), "R$ %.2f/Km", avgCostPerKm) else "N/D"

            canvas.drawText("Consumo Médio: $avgConsText", col3X, row1Y, headerTextPaint)
            canvas.drawText("Custo por Km: $avgCostText", col3X, row2Y, bodyPaint)
            canvas.drawText("Qtd. Registros: ${fuelList.size + expenseList.size}", col3X, row3Y, bodyPaint)

            val totalReimb = totalDistanceKm * vehicle.companyReimbursementRatePerKm
            val netBal = totalReimb - totalSpent
            val reimbRateText = String.format(Locale("pt", "BR"), "R$ %.2f", vehicle.companyReimbursementRatePerKm)
            val reimbTotalText = String.format(Locale("pt", "BR"), "R$ %,.2f", totalReimb)
            val netBalText = if (netBal >= 0) String.format(Locale("pt", "BR"), "+ R$ %,.2f", netBal) else String.format(Locale("pt", "BR"), "- R$ %,.2f", kotlin.math.abs(netBal))

            canvas.drawText("Reembolso Empresa ($reimbRateText/km): $reimbTotalText", col1X, row4Y, headerTextPaint)
            canvas.drawText("Saldo Líquido do Motorista: $netBalText", col3X, row4Y, headerTextPaint)

            y += 148f

            // Section: Abastecimentos
            canvas.drawText("Abastecimentos de Combustível (${fuelList.size})", 40f, y, sectionHeaderPaint)
            y += 14f

            // Table Header
            val tableBgPaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                style = Paint.Style.FILL
            }
            canvas.drawRect(RectF(40f, y, pageWidth - 40f, y + 20f), tableBgPaint)

            val tColDate = 45f
            val tColKm = 115f
            val tColDist = 175f
            val tColFuel = 230f
            val tColLiters = 310f
            val tColTotal = 365f
            val tColKmL = 430f
            val tColCostKm = 495f

            val headerY = y + 14f
            canvas.drawText("Data", tColDate, headerY, headerTextPaint)
            canvas.drawText("Odômetro", tColKm, headerY, headerTextPaint)
            canvas.drawText("Km Rod.", tColDist, headerY, headerTextPaint)
            canvas.drawText("Combustível", tColFuel, headerY, headerTextPaint)
            canvas.drawText("Litros", tColLiters, headerY, headerTextPaint)
            canvas.drawText("Total R$", tColTotal, headerY, headerTextPaint)
            canvas.drawText("Km/L", tColKmL, headerY, headerTextPaint)
            canvas.drawText("R$/Km", tColCostKm, headerY, headerTextPaint)

            y += 20f

            val linePaint = Paint().apply {
                color = Color.rgb(241, 245, 249)
                strokeWidth = 1f
            }

            if (fuelList.isEmpty()) {
                y += 18f
                canvas.drawText("Nenhum abastecimento registrado neste período.", 45f, y, bodyPaint)
                y += 12f
            } else {
                for (fuel in fuelList.take(15)) {
                    y += 18f
                    val dateStr = dateOnlyFormatter.format(Date(fuel.timestamp))
                    val kmStr = String.format(Locale("pt", "BR"), "%,.0f", fuel.currentOdometerKm)
                    val distStr = if (fuel.distanceTraveledKm != null) String.format(Locale("pt", "BR"), "%,.0f", fuel.distanceTraveledKm) else "-"
                    val fuelName = fuel.fuelType.take(12)
                    val litersStr = String.format(Locale("pt", "BR"), "%.1f", fuel.liters)
                    val totalStr = String.format(Locale("pt", "BR"), "%.2f", fuel.totalPrice)
                    val kmLStr = if (fuel.consumptionKmPerLiter != null) String.format(Locale("pt", "BR"), "%.1f", fuel.consumptionKmPerLiter) else "-"
                    val costKmStr = if (fuel.costPerKm != null) String.format(Locale("pt", "BR"), "%.2f", fuel.costPerKm) else "-"

                    canvas.drawText(dateStr, tColDate, y, bodyPaint)
                    canvas.drawText(kmStr, tColKm, y, bodyPaint)
                    canvas.drawText(distStr, tColDist, y, bodyPaint)
                    canvas.drawText(fuelName, tColFuel, y, bodyPaint)
                    canvas.drawText(litersStr, tColLiters, y, bodyPaint)
                    canvas.drawText(totalStr, tColTotal, y, bodyPaint)
                    canvas.drawText(kmLStr, tColKmL, y, bodyPaint)
                    canvas.drawText(costKmStr, tColCostKm, y, bodyPaint)

                    canvas.drawLine(40f, y + 4f, pageWidth - 40f, y + 4f, linePaint)
                }
            }

            y += 24f

            // Section: Outras Despesas (Estacionamento, etc.)
            canvas.drawText("Despesas Operacionais & Estacionamento (${expenseList.size})", 40f, y, sectionHeaderPaint)
            y += 14f

            canvas.drawRect(RectF(40f, y, pageWidth - 40f, y + 20f), tableBgPaint)
            val eColDate = 45f
            val eColCat = 120f
            val eColTitle = 220f
            val eColLoc = 360f
            val eColAmount = 485f

            val eHeaderY = y + 14f
            canvas.drawText("Data", eColDate, eHeaderY, headerTextPaint)
            canvas.drawText("Categoria", eColCat, eHeaderY, headerTextPaint)
            canvas.drawText("Descrição / Item", eColTitle, eHeaderY, headerTextPaint)
            canvas.drawText("Local / Estabelecimento", eColLoc, eHeaderY, headerTextPaint)
            canvas.drawText("Valor R$", eColAmount, eHeaderY, headerTextPaint)

            y += 20f

            if (expenseList.isEmpty()) {
                y += 18f
                canvas.drawText("Nenhuma despesa operacional registrada neste período.", 45f, y, bodyPaint)
                y += 12f
            } else {
                for (exp in expenseList.take(12)) {
                    y += 18f
                    val dateStr = dateOnlyFormatter.format(Date(exp.timestamp))
                    val catStr = exp.category.take(15)
                    val titleStr = exp.title.take(24)
                    val locStr = (exp.location ?: "-").take(20)
                    val amtStr = String.format(Locale("pt", "BR"), "R$ %.2f", exp.amount)

                    canvas.drawText(dateStr, eColDate, y, bodyPaint)
                    canvas.drawText(catStr, eColCat, y, bodyPaint)
                    canvas.drawText(titleStr, eColTitle, y, bodyPaint)
                    canvas.drawText(locStr, eColLoc, y, bodyPaint)
                    canvas.drawText(amtStr, eColAmount, y, bodyPaint)

                    canvas.drawLine(40f, y + 4f, pageWidth - 40f, y + 4f, linePaint)
                }
            }

            // Footer note
            val footerY = pageHeight - 35f
            canvas.drawLine(40f, footerY - 10f, pageWidth - 40f, footerY - 10f, cardBorderPaint)
            canvas.drawText("AutoGastos • Gerado em dispositivo móvel com integridade de dados e auditoria de recibos.", 40f, footerY, subtitlePaint)

            document.finishPage(page)

            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateCsvReport(
        context: Context,
        vehicle: Vehicle,
        monthYear: String,
        fuelList: List<FuelEntry>,
        expenseList: List<ExpenseEntry>
    ): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val cleanMonth = monthYear.replace("/", "_").replace(" ", "_")
            val csvFile = File(reportsDir, "AutoGastos_$cleanMonth.csv")

            val sb = StringBuilder()
            // UTF-8 BOM for Microsoft Excel compatibility with accented characters
            sb.append("\uFEFF")

            // Metadata section
            sb.append("AutoGastos - Relatório de Abastecimentos e Despesas\n")
            sb.append("Veículo:;\"${vehicle.name} (${vehicle.brandModel} - ${vehicle.plate})\"\n")
            sb.append("Período:;\"$monthYear\"\n")
            sb.append("Data de Geração:;\"${dateFormatter.format(Date())}\"\n")
            val totalDistanceSum = fuelList.mapNotNull { it.distanceTraveledKm }.sum()
            val totalReimbursement = totalDistanceSum * vehicle.companyReimbursementRatePerKm
            val totalSpentSum = fuelList.sumOf { it.totalPrice } + expenseList.sumOf { it.amount }
            sb.append("Taxa Reembolso Empresa:;\"R$ ${String.format(Locale("pt", "BR"), "%.2f", vehicle.companyReimbursementRatePerKm)} / km\"\n")
            sb.append("Total Reembolso a Receber da Empresa:;\"R$ ${String.format(Locale("pt", "BR"), "%.2f", totalReimbursement)}\"\n")
            sb.append("Saldo Líquido do Motorista:;\"R$ ${String.format(Locale("pt", "BR"), "%.2f", totalReimbursement - totalSpentSum)}\"\n\n")

            // Fuel table
            sb.append("--- ABASTECIMENTOS ---\n")
            sb.append("Data e Hora;Posto;Tipo de Combustível;Odômetro (km);Km Rodados;Litros;Preço/Litro (R$);Total Pago (R$);Consumo (Km/L);Custo por Km (R$/Km);Tanque Cheio;Tem Foto Odômetro;Tem Recibo;Observações\n")

            for (fuel in fuelList) {
                sb.append("\"${dateFormatter.format(Date(fuel.timestamp))}\";")
                sb.append("\"${fuel.stationName ?: ""}\";")
                sb.append("\"${fuel.fuelType}\";")
                sb.append("${fuel.currentOdometerKm};")
                sb.append("${fuel.distanceTraveledKm ?: ""};")
                sb.append("${fuel.liters};")
                sb.append("${fuel.pricePerLiter};")
                sb.append("${fuel.totalPrice};")
                sb.append("${fuel.consumptionKmPerLiter ?: ""};")
                sb.append("${fuel.costPerKm ?: ""};")
                sb.append(if (fuel.isFullTank) "Sim" else "Não").append(";")
                sb.append(if (!fuel.odometerPhotoUri.isNullOrEmpty()) "Sim" else "Não").append(";")
                sb.append(if (!fuel.receiptPhotoUri.isNullOrEmpty()) "Sim" else "Não").append(";")
                sb.append("\"${fuel.notes ?: ""}\"\n")
            }

            sb.append("\n--- OUTRAS DESPESAS (ESTACIONAMENTO, PEDÁGIO, ETC.) ---\n")
            sb.append("Data e Hora;Categoria;Título;Valor (R$);Odômetro (km);Local;Tem Comprovante;Observações\n")

            for (exp in expenseList) {
                sb.append("\"${dateFormatter.format(Date(exp.timestamp))}\";")
                sb.append("\"${exp.category}\";")
                sb.append("\"${exp.title}\";")
                sb.append("${exp.amount};")
                sb.append("${exp.odometerKm ?: ""};")
                sb.append("\"${exp.location ?: ""}\";")
                sb.append(if (!exp.receiptPhotoUri.isNullOrEmpty()) "Sim" else "Não").append(";")
                sb.append("\"${exp.notes ?: ""}\"\n")
            }

            FileOutputStream(csvFile).use { out ->
                out.write(sb.toString().toByteArray(Charsets.UTF_8))
            }
            csvFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateExcelReport(
        context: Context,
        vehicle: Vehicle,
        monthYear: String,
        fuelList: List<FuelEntry>,
        expenseList: List<ExpenseEntry>
    ): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val cleanMonth = monthYear.replace("/", "_").replace(" ", "_")
            val xlsFile = File(reportsDir, "AutoGastos_$cleanMonth.xls")

            val totalFuel = fuelList.sumOf { it.totalPrice }
            val totalExpense = expenseList.sumOf { it.amount }
            val totalAll = totalFuel + totalExpense

            // XML Spreadsheet 2003 format - natively understood and opened by Excel with formatting
            val xml = buildString {
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                append("<?mso-application progid=\"Excel.Sheet\"?>\n")
                append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
                append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
                append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
                append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
                append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")

                // Styles
                append(" <Styles>\n")
                append("  <Style ss:ID=\"Default\" ss:Name=\"Normal\">\n")
                append("   <Alignment ss:Vertical=\"Bottom\"/>\n")
                append("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\" ss:Color=\"#000000\"/>\n")
                append("  </Style>\n")
                append("  <Style ss:ID=\"HeaderTitle\">\n")
                append("   <Font ss:FontName=\"Calibri\" ss:Size=\"16\" ss:Bold=\"1\" ss:Color=\"#0D9488\"/>\n")
                append("  </Style>\n")
                append("  <Style ss:ID=\"HeaderSection\">\n")
                append("   <Font ss:FontName=\"Calibri\" ss:Size=\"12\" ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/>\n")
                append("   <Interior ss:Color=\"#0D9488\" ss:Pattern=\"Solid\"/>\n")
                append("  </Style>\n")
                append("  <Style ss:ID=\"TableHead\">\n")
                append("   <Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#0F172A\"/>\n")
                append("   <Interior ss:Color=\"#E2E8F0\" ss:Pattern=\"Solid\"/>\n")
                append("   <Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/></Borders>\n")
                append("  </Style>\n")
                append("  <Style ss:ID=\"Currency\">\n")
                append("   <NumberFormat ss:Format=\"R$ #,##0.00\"/>\n")
                append("  </Style>\n")
                append("  <Style ss:ID=\"BoldCurrency\">\n")
                append("   <Font ss:Bold=\"1\"/>\n")
                append("   <NumberFormat ss:Format=\"R$ #,##0.00\"/>\n")
                append("  </Style>\n")
                append("  <Style ss:ID=\"Number1Dec\">\n")
                append("   <NumberFormat ss:Format=\"#,##0.0\"/>\n")
                append("  </Style>\n")
                append("  <Style ss:ID=\"Number2Dec\">\n")
                append("   <NumberFormat ss:Format=\"#,##0.00\"/>\n")
                append("  </Style>\n")
                append(" </Styles>\n")

                // Worksheet 1: Resumo
                append(" <Worksheet ss:Name=\"Resumo Mensal\">\n")
                append("  <Table ss:DefaultColumnWidth=\"120\">\n")
                append("   <Row><Cell ss:StyleID=\"HeaderTitle\"><Data ss:Type=\"String\">AutoGastos - Resumo Mensal</Data></Cell></Row>\n")
                append("   <Row><Cell><Data ss:Type=\"String\">Veículo: ${vehicle.name} (${vehicle.brandModel} - ${vehicle.plate})</Data></Cell></Row>\n")
                append("   <Row><Cell><Data ss:Type=\"String\">Período: $monthYear</Data></Cell></Row>\n")
                append("   <Row><Cell><Data ss:Type=\"String\">Data de Emissão: ${dateFormatter.format(Date())}</Data></Cell></Row>\n")
                append("   <Row/>\n")
                append("   <Row><Cell ss:StyleID=\"HeaderSection\"><Data ss:Type=\"String\">Métrica</Data></Cell><Cell ss:StyleID=\"HeaderSection\"><Data ss:Type=\"String\">Valor</Data></Cell></Row>\n")
                append("   <Row><Cell><Data ss:Type=\"String\">Total Geral Gasto</Data></Cell><Cell ss:StyleID=\"BoldCurrency\"><Data ss:Type=\"Number\">$totalAll</Data></Cell></Row>\n")
                append("   <Row><Cell><Data ss:Type=\"String\">Gastos com Combustível</Data></Cell><Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">$totalFuel</Data></Cell></Row>\n")
                append("   <Row><Cell><Data ss:Type=\"String\">Gastos com Estacionamento &amp; Outros</Data></Cell><Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">$totalExpense</Data></Cell></Row>\n")
                val totalDist = fuelList.mapNotNull { it.distanceTraveledKm }.sum()
                append("   <Row><Cell><Data ss:Type=\"String\">Distância Total Rodada (km)</Data></Cell><Cell ss:StyleID=\"Number1Dec\"><Data ss:Type=\"Number\">$totalDist</Data></Cell></Row>\n")
                val validCons = fuelList.mapNotNull { it.consumptionKmPerLiter }
                if (validCons.isNotEmpty()) {
                    append("   <Row><Cell><Data ss:Type=\"String\">Média Geral de Consumo (Km/L)</Data></Cell><Cell ss:StyleID=\"Number2Dec\"><Data ss:Type=\"Number\">${validCons.average()}</Data></Cell></Row>\n")
                }
                val validCostKm = fuelList.mapNotNull { it.costPerKm }
                if (validCostKm.isNotEmpty()) {
                    append("   <Row><Cell><Data ss:Type=\"String\">Custo Médio por Km (R$/Km)</Data></Cell><Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${validCostKm.average()}</Data></Cell></Row>\n")
                }
                val totalReimb = totalDist * vehicle.companyReimbursementRatePerKm
                append("   <Row><Cell><Data ss:Type=\"String\">Taxa Reembolso Empresa (R$/Km)</Data></Cell><Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${vehicle.companyReimbursementRatePerKm}</Data></Cell></Row>\n")
                append("   <Row><Cell><Data ss:Type=\"String\">Total Reembolso da Empresa a Receber</Data></Cell><Cell ss:StyleID=\"BoldCurrency\"><Data ss:Type=\"Number\">$totalReimb</Data></Cell></Row>\n")
                append("   <Row><Cell><Data ss:Type=\"String\">Saldo Líquido do Motorista (Reembolso - Gastos)</Data></Cell><Cell ss:StyleID=\"BoldCurrency\"><Data ss:Type=\"Number\">${totalReimb - totalAll}</Data></Cell></Row>\n")
                append("  </Table>\n")
                append(" </Worksheet>\n")

                // Worksheet 2: Abastecimentos
                append(" <Worksheet ss:Name=\"Abastecimentos\">\n")
                append("  <Table ss:DefaultColumnWidth=\"100\">\n")
                append("   <Row ss:StyleID=\"TableHead\">\n")
                append("    <Cell><Data ss:Type=\"String\">Data</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Posto</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Combustível</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Odômetro (km)</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Km Rodados</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Litros</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Preço/Litro</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Total Pago</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Km/L</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Custo/Km</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Tanque Cheio</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Observações</Data></Cell>\n")
                append("   </Row>\n")

                for (fuel in fuelList) {
                    append("   <Row>\n")
                    append("    <Cell><Data ss:Type=\"String\">${dateOnlyFormatter.format(Date(fuel.timestamp))}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"String\">${fuel.stationName ?: ""}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"String\">${fuel.fuelType}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"Number\">${fuel.currentOdometerKm}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"Number\">${fuel.distanceTraveledKm ?: 0}</Data></Cell>\n")
                    append("    <Cell ss:StyleID=\"Number2Dec\"><Data ss:Type=\"Number\">${fuel.liters}</Data></Cell>\n")
                    append("    <Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${fuel.pricePerLiter}</Data></Cell>\n")
                    append("    <Cell ss:StyleID=\"BoldCurrency\"><Data ss:Type=\"Number\">${fuel.totalPrice}</Data></Cell>\n")
                    append("    <Cell ss:StyleID=\"Number2Dec\"><Data ss:Type=\"Number\">${fuel.consumptionKmPerLiter ?: 0}</Data></Cell>\n")
                    append("    <Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${fuel.costPerKm ?: 0}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"String\">${if (fuel.isFullTank) "Sim" else "Não"}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"String\">${fuel.notes ?: ""}</Data></Cell>\n")
                    append("   </Row>\n")
                }
                append("  </Table>\n")
                append(" </Worksheet>\n")

                // Worksheet 3: Outras Despesas
                append(" <Worksheet ss:Name=\"Despesas\">\n")
                append("  <Table ss:DefaultColumnWidth=\"110\">\n")
                append("   <Row ss:StyleID=\"TableHead\">\n")
                append("    <Cell><Data ss:Type=\"String\">Data</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Categoria</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Descrição</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Local</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Odômetro</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Valor</Data></Cell>\n")
                append("    <Cell><Data ss:Type=\"String\">Observações</Data></Cell>\n")
                append("   </Row>\n")

                for (exp in expenseList) {
                    append("   <Row>\n")
                    append("    <Cell><Data ss:Type=\"String\">${dateOnlyFormatter.format(Date(exp.timestamp))}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"String\">${exp.category}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"String\">${exp.title}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"String\">${exp.location ?: ""}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"Number\">${exp.odometerKm ?: 0}</Data></Cell>\n")
                    append("    <Cell ss:StyleID=\"BoldCurrency\"><Data ss:Type=\"Number\">${exp.amount}</Data></Cell>\n")
                    append("    <Cell><Data ss:Type=\"String\">${exp.notes ?: ""}</Data></Cell>\n")
                    append("   </Row>\n")
                }
                append("  </Table>\n")
                append(" </Worksheet>\n")

                append("</Workbook>\n")
            }

            FileOutputStream(xlsFile).use { out ->
                out.write(xml.toByteArray(Charsets.UTF_8))
            }
            xlsFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartilhar $title"))
    }
}
