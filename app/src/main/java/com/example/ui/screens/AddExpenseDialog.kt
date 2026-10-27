package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntry
import com.example.ui.components.PhotoAttachmentSection
import com.example.util.PhotoManager
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    initialEntry: ExpenseEntry? = null,
    currentOdometer: Double,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntry) -> Unit,
    onDelete: (() -> Unit)? = null,
    onViewPhoto: (title: String, path: String) -> Unit
) {
    val context = LocalContext.current

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    var selectedCategory by remember {
        mutableStateOf(initialEntry?.category ?: ExpenseCategory.ESTACIONAMENTO.displayName)
    }

    var titleText by remember {
        mutableStateOf(
            initialEntry?.title ?: if (selectedCategory == ExpenseCategory.ESTACIONAMENTO.displayName) "Estacionamento" else ""
        )
    }

    var amountText by remember {
        mutableStateOf(if (initialEntry != null) initialEntry.amount.toString() else "")
    }

    var odometerText by remember {
        mutableStateOf(
            if (initialEntry?.odometerKm != null) initialEntry.odometerKm.toString()
            else if (currentOdometer > 0) String.format(java.util.Locale.US, "%.0f", currentOdometer)
            else ""
        )
    }

    var locationText by remember {
        mutableStateOf(initialEntry?.location ?: "")
    }

    var notesText by remember {
        mutableStateOf(initialEntry?.notes ?: "")
    }

    var receiptPhotoPath by remember {
        mutableStateOf(initialEntry?.receiptPhotoUri)
    }

    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    var cameraTempFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraTempFile != null) {
            receiptPhotoPath = cameraTempFile!!.absolutePath
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = PhotoManager.saveImageToInternalStorage(context, uri)
            if (path != null) receiptPhotoPath = path
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 660.dp)
                .testTag("add_expense_dialog"),
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
                        imageVector = Icons.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialEntry == null) "Nova Despesa" else "Editar Despesa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_expense_dialog_btn")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = "Categoria da Despesa",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.displayName,
                            onClick = {
                                selectedCategory = cat.displayName
                                if (titleText.isBlank() || titleText in ExpenseCategory.entries.map { it.displayName }) {
                                    titleText = cat.displayName
                                }
                            },
                            label = { Text(cat.displayName, fontSize = 12.sp) },
                            modifier = Modifier.testTag("chip_cat_${cat.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Descrição / Título do Gasto") },
                    placeholder = { Text("Ex: Estacionamento Shopping, Troca de pastilhas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_title"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount and Odometer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Valor Total (R$)") },
                        placeholder = { Text("Ex: 28.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_expense_amount"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = odometerText,
                        onValueChange = { odometerText = it },
                        label = { Text("Odômetro Km (Opcional)") },
                        placeholder = { Text("Ex: 42531") },
                        leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("input_expense_odometer"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location / Establishment
                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = { Text("Local / Estabelecimento (Opcional)") },
                    placeholder = { Text("Ex: Shopping Eldorado, Estapar Centro") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_location"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Photo Section
                PhotoAttachmentSection(
                    title = "Foto do Recibo / Comprovante de Pagamento",
                    photoUri = receiptPhotoPath,
                    onPickFromGallery = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onTakePhoto = {
                        val (uri, file) = PhotoManager.createCameraUri(context)
                        cameraTempUri = uri
                        cameraTempFile = file
                        cameraLauncher.launch(uri)
                    },
                    onRemovePhoto = { receiptPhotoPath = null },
                    onViewPhoto = {
                        receiptPhotoPath?.let { onViewPhoto("Comprovante - $titleText", it) }
                    },
                    testTagPrefix = "expense_receipt_photo"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observações (Opcional)") },
                    placeholder = { Text("Ex: Pago via Pix, recibo arquivado") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_notes"),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                val canSave = titleText.isNotBlank() && parsedAmount > 0
                Button(
                    onClick = {
                        val entry = (initialEntry ?: ExpenseEntry(
                            title = titleText.trim(),
                            amount = parsedAmount
                        )).copy(
                            category = selectedCategory,
                            title = titleText.trim(),
                            amount = parsedAmount,
                            odometerKm = odometerText.toDoubleOrNull(),
                            location = locationText.ifBlank { null },
                            receiptPhotoUri = receiptPhotoPath,
                            notes = notesText.ifBlank { null }
                        )
                        onSave(entry)
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_expense_entry")
                ) {
                    Text(
                        text = if (initialEntry == null) "Salvar Despesa" else "Atualizar Despesa",
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
                            .testTag("btn_delete_expense_in_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Excluir Este Ticket / Despesa",
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
            title = { Text("Excluir Despesa / Ticket?", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente excluir este lançamento de despesa? Os relatórios e cálculos mensais serão atualizados.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_expense_in_dialog_btn")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    modifier = Modifier.testTag("cancel_delete_expense_in_dialog_btn")
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
