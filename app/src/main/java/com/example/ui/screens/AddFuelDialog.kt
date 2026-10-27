package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FuelEntry
import com.example.data.model.FuelType
import com.example.ui.components.PhotoAttachmentSection
import com.example.util.PhotoManager
import java.io.File
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFuelDialog(
    initialEntry: FuelEntry? = null,
    latestOdometer: Double,
    onDismiss: () -> Unit,
    onSave: (FuelEntry) -> Unit,
    onDelete: (() -> Unit)? = null,
    onViewPhoto: (title: String, path: String) -> Unit
) {
    val context = LocalContext.current

    var odometerText by remember {
        mutableStateOf(
            if (initialEntry != null) initialEntry.currentOdometerKm.toString()
            else if (latestOdometer > 0) String.format(Locale.US, "%.0f", latestOdometer + 450)
            else ""
        )
    }

    var litersText by remember {
        mutableStateOf(if (initialEntry != null) initialEntry.liters.toString() else "")
    }

    var pricePerLiterText by remember {
        mutableStateOf(if (initialEntry != null) initialEntry.pricePerLiter.toString() else "5.89")
    }

    var totalPriceText by remember {
        mutableStateOf(if (initialEntry != null) initialEntry.totalPrice.toString() else "")
    }

    var selectedFuelType by remember {
        mutableStateOf(initialEntry?.fuelType ?: FuelType.GASOLINA_COMUM.displayName)
    }

    var isFullTank by remember {
        mutableStateOf(initialEntry?.isFullTank ?: true)
    }

    var stationName by remember {
        mutableStateOf(initialEntry?.stationName ?: "")
    }

    var notes by remember {
        mutableStateOf(initialEntry?.notes ?: "")
    }

    var odometerPhotoPath by remember {
        mutableStateOf(initialEntry?.odometerPhotoUri)
    }

    var receiptPhotoPath by remember {
        mutableStateOf(initialEntry?.receiptPhotoUri)
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Temporary storage for camera captures
    var pendingCameraTarget by remember { mutableStateOf<String?>(null) } // "odometer" or "receipt"
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    var cameraTempFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraTempFile != null) {
            val savedPath = cameraTempFile!!.absolutePath
            if (pendingCameraTarget == "odometer") {
                odometerPhotoPath = savedPath
            } else if (pendingCameraTarget == "receipt") {
                receiptPhotoPath = savedPath
            }
        }
    }

    val odometerGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = PhotoManager.saveImageToInternalStorage(context, uri)
            if (path != null) odometerPhotoPath = path
        }
    }

    val receiptGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = PhotoManager.saveImageToInternalStorage(context, uri)
            if (path != null) receiptPhotoPath = path
        }
    }

    // Auto-calculate Total Price when liters or price/liter change
    LaunchedEffect(litersText, pricePerLiterText) {
        val l = litersText.toDoubleOrNull()
        val p = pricePerLiterText.toDoubleOrNull()
        if (l != null && p != null && l > 0 && p > 0) {
            totalPriceText = String.format(Locale.US, "%.2f", l * p)
        }
    }

    // Preview distance & telemetry calculations
    val currentOdo = odometerText.toDoubleOrNull() ?: 0.0
    val distanceTraveled = if (currentOdo > latestOdometer && latestOdometer > 0) currentOdo - latestOdometer else null
    val parsedLiters = litersText.toDoubleOrNull() ?: 0.0
    val parsedTotal = totalPriceText.toDoubleOrNull() ?: 0.0
    val estimatedKmL = if (distanceTraveled != null && parsedLiters > 0) distanceTraveled / parsedLiters else null
    val estimatedCostKm = if (distanceTraveled != null && parsedTotal > 0) parsedTotal / distanceTraveled else null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("add_fuel_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalGasStation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialEntry == null) "Novo Abastecimento" else "Editar Abastecimento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_fuel_dialog_btn")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fuel Type Selection Chips
                Text(
                    text = "Tipo de Combustível",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FuelType.entries.forEach { fuel ->
                        FilterChip(
                            selected = selectedFuelType == fuel.displayName,
                            onClick = { selectedFuelType = fuel.displayName },
                            label = { Text(fuel.displayName, fontSize = 12.sp) },
                            modifier = Modifier.testTag("chip_fuel_${fuel.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Odometer (Km)
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { odometerText = it },
                    label = { Text("Km do Odômetro Atual") },
                    placeholder = { Text("Ex: 42531") },
                    leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fuel_odometer"),
                    singleLine = true
                )

                // Distance traveled insight preview
                if (distanceTraveled != null && distanceTraveled > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Distância percorrida estimada: +${String.format(Locale("pt", "BR"), "%,.0f km", distanceTraveled)} desde último abastecimento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Liters and Price Per Liter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = litersText,
                        onValueChange = { litersText = it },
                        label = { Text("Litros") },
                        placeholder = { Text("Ex: 43.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_fuel_liters"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pricePerLiterText,
                        onValueChange = { pricePerLiterText = it },
                        label = { Text("Preço / L (R$)") },
                        placeholder = { Text("Ex: 5.89") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_fuel_price_per_liter"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Price
                OutlinedTextField(
                    value = totalPriceText,
                    onValueChange = { totalPriceText = it },
                    label = { Text("Valor Total Pago (R$)") },
                    placeholder = { Text("Ex: 256.21") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fuel_total_price"),
                    singleLine = true
                )

                // Telemetry summary card if distance & liters available
                if (estimatedKmL != null || estimatedCostKm != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            if (estimatedKmL != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Consumo Médio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(
                                        String.format(Locale("pt", "BR"), "%.2f Km/L", estimatedKmL),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            if (estimatedCostKm != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Custo por Km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(
                                        String.format(Locale("pt", "BR"), "R$ %.2f / km", estimatedCostKm),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Full Tank Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tanque Cheio?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Necessário para cálculo de consumo exato (Km/L)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isFullTank,
                        onCheckedChange = { isFullTank = it },
                        modifier = Modifier.testTag("switch_fuel_full_tank")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Station Name
                OutlinedTextField(
                    value = stationName,
                    onValueChange = { stationName = it },
                    label = { Text("Nome do Posto / Bandeira (Opcional)") },
                    placeholder = { Text("Ex: Posto Ipiranga Centro") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fuel_station"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Photos Section
                Text(
                    text = "Fotos de Comprovação & Odômetro",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Photo 1: Odometer Photo
                PhotoAttachmentSection(
                    title = "Foto do Painel / Odômetro",
                    photoUri = odometerPhotoPath,
                    onPickFromGallery = {
                        odometerGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onTakePhoto = {
                        val (uri, file) = PhotoManager.createCameraUri(context)
                        cameraTempUri = uri
                        cameraTempFile = file
                        pendingCameraTarget = "odometer"
                        cameraLauncher.launch(uri)
                    },
                    onRemovePhoto = { odometerPhotoPath = null },
                    onViewPhoto = {
                        odometerPhotoPath?.let { onViewPhoto("Foto do Odômetro", it) }
                    },
                    testTagPrefix = "photo_odometer"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Photo 2: Receipt Photo
                PhotoAttachmentSection(
                    title = "Foto do Recibo / Comprovante Fiscal",
                    photoUri = receiptPhotoPath,
                    onPickFromGallery = {
                        receiptGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onTakePhoto = {
                        val (uri, file) = PhotoManager.createCameraUri(context)
                        cameraTempUri = uri
                        cameraTempFile = file
                        pendingCameraTarget = "receipt"
                        cameraLauncher.launch(uri)
                    },
                    onRemovePhoto = { receiptPhotoPath = null },
                    onViewPhoto = {
                        receiptPhotoPath?.let { onViewPhoto("Comprovante Fiscal", it) }
                    },
                    testTagPrefix = "photo_receipt"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (Opcional)") },
                    placeholder = { Text("Ex: Calibragem dos pneus feita") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fuel_notes"),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                val canSave = currentOdo > 0 && parsedLiters > 0 && parsedTotal > 0
                Button(
                    onClick = {
                        val entry = (initialEntry ?: FuelEntry(
                            currentOdometerKm = currentOdo,
                            liters = parsedLiters,
                            pricePerLiter = pricePerLiterText.toDoubleOrNull() ?: (parsedTotal / parsedLiters),
                            totalPrice = parsedTotal
                        )).copy(
                            currentOdometerKm = currentOdo,
                            liters = parsedLiters,
                            pricePerLiter = pricePerLiterText.toDoubleOrNull() ?: (parsedTotal / parsedLiters),
                            totalPrice = parsedTotal,
                            fuelType = selectedFuelType,
                            isFullTank = isFullTank,
                            stationName = stationName.ifBlank { null },
                            odometerPhotoUri = odometerPhotoPath,
                            receiptPhotoUri = receiptPhotoPath,
                            notes = notes.ifBlank { null }
                        )
                        onSave(entry)
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_fuel_entry")
                ) {
                    Text(
                        text = if (initialEntry == null) "Salvar Abastecimento" else "Atualizar Abastecimento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (initialEntry != null && onDelete != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_delete_fuel_in_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Excluir Este Abastecimento",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Excluir Abastecimento?", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente excluir este abastecimento? O histórico e as médias de consumo serão recalculados automaticamente.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_fuel_in_dialog_btn")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    modifier = Modifier.testTag("cancel_delete_fuel_in_dialog_btn")
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
