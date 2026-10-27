package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Vehicle

@Composable
fun EditVehicleDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onSave: (name: String, model: String, plate: String, tankCapacity: Double, reimbursementRate: Double) -> Unit
) {
    var name by remember { mutableStateOf(vehicle.name) }
    var model by remember { mutableStateOf(vehicle.brandModel) }
    var plate by remember { mutableStateOf(vehicle.plate) }
    var tankCapacityText by remember { mutableStateOf(vehicle.tankCapacityLiters.toString()) }
    var reimbursementRateText by remember { mutableStateOf(vehicle.companyReimbursementRatePerKm.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_vehicle_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perfil do Veículo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_vehicle_dialog_btn")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Apelido do Carro") },
                    placeholder = { Text("Ex: Meu Carro, Sedan Família") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Marca / Modelo") },
                    placeholder = { Text("Ex: Corolla 2.0 Flex, Onix Turbo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_model"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it.uppercase() },
                    label = { Text("Placa") },
                    placeholder = { Text("Ex: ABC-1D23") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_plate"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tankCapacityText,
                    onValueChange = { tankCapacityText = it },
                    label = { Text("Capacidade do Tanque (Litros)") },
                    placeholder = { Text("Ex: 50.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_tank_capacity"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Company reimbursement rate per kilometer field
                Text(
                    text = "Reembolso Corporativo",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = reimbursementRateText,
                    onValueChange = { reimbursementRateText = it },
                    label = { Text("Valor pago pela empresa por Km rodado (R$/Km)") },
                    placeholder = { Text("Ex: 1.15") },
                    supportingText = { Text("Usado para calcular automaticamente o total a receber no mês") },
                    leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_reimbursement_rate"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val cap = tankCapacityText.toDoubleOrNull() ?: 50.0
                        val rate = reimbursementRateText.toDoubleOrNull() ?: 0.0
                        onSave(name.trim(), model.trim(), plate.trim(), cap, rate)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_vehicle"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Alterações", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
