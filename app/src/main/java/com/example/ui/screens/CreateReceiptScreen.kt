package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Patient
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.PendingAmber
import com.example.ui.theme.PendingAmberContainer
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.TerracottaPrimaryDark
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ClinicViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateReceiptScreen(
    viewModel: ClinicViewModel
) {
    val context = LocalContext.current
    val patients by viewModel.allPatients.collectAsStateWithLifecycle()
    val preselectedPatient by viewModel.preselectedPatient.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.clearPreselectedPatient()
        viewModel.navigateTo(AppScreen.RECEIPTS)
    }

    var selectedPatient by remember { mutableStateOf<Patient?>(preselectedPatient) }
    var patientDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(preselectedPatient) {
        if (preselectedPatient != null) {
            selectedPatient = preselectedPatient
        } else if (patients.isNotEmpty() && selectedPatient == null) {
            selectedPatient = patients.first()
        }
    }

    var serviceDescription by remember { mutableStateOf("Sessão de Psicanálise Clínica") }
    var sessionQuantity by remember { mutableIntStateOf(1) }
    var unitPrice by remember {
        mutableDoubleStateOf(selectedPatient?.defaultSessionValue ?: 180.0)
    }
    var discountInput by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("PIX") }
    var paymentStatus by remember { mutableStateOf("PAGO") }
    var sessionDates by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val todayFormatted = remember {
        SimpleDateFormat("dd - MMM - yyyy", Locale("pt", "BR")).format(Date())
    }
    var issueDateInput by remember { mutableStateOf(todayFormatted) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    // When patient changes, update default session value
    LaunchedEffect(selectedPatient) {
        selectedPatient?.let {
            unitPrice = it.defaultSessionValue
        }
    }

    val discount = discountInput.toDoubleOrNull() ?: 0.0
    val totalCalculated = maxOf(0.0, (sessionQuantity * unitPrice) - discount)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Emitir Novo Recibo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.clearPreselectedPatient()
                            viewModel.navigateTo(AppScreen.RECEIPTS)
                        },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Patient Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TerracottaPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Paciente / Destinatário",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimaryDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (patients.isEmpty()) {
                        Text(
                            text = "Nenhum paciente cadastrado. Cadastre um paciente na aba Pacientes primeiro.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = patientDropdownExpanded,
                            onExpandedChange = { patientDropdownExpanded = !patientDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedPatient?.let { "${it.name} (${it.cpf})" } ?: "Selecione um paciente",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("patient_select_field"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = patientDropdownExpanded,
                                onDismissRequest = { patientDropdownExpanded = false }
                            ) {
                                patients.forEach { patient ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(patient.name, fontWeight = FontWeight.SemiBold)
                                                Text("CPF: ${patient.cpf} • R$ ${patient.defaultSessionValue}", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            selectedPatient = patient
                                            patientDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Service & Sessions Details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dados do Atendimento",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimaryDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = serviceDescription,
                        onValueChange = { serviceDescription = it },
                        label = { Text("Descrição do Serviço") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("service_desc_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Suggestion Chips
                    Text("Sugestões rápidas:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Sessão de Psicanálise Clínica",
                            "Pacote Mensal (4 sessões)",
                            "Atendimento Psicoterápico",
                            "Supervisão Clínica"
                        ).forEach { suggestion ->
                            FilterChip(
                                selected = serviceDescription == suggestion,
                                onClick = {
                                    serviceDescription = suggestion
                                    if (suggestion.contains("4")) {
                                        sessionQuantity = 4
                                    }
                                },
                                label = { Text(suggestion, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quantity Stepper & Unit Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quantity
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Quantidade", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { if (sessionQuantity > 1) sessionQuantity-- },
                                    modifier = Modifier.size(36.dp).testTag("qty_minus_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Diminuir")
                                }
                                Text(
                                    text = "$sessionQuantity",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                IconButton(
                                    onClick = { sessionQuantity++ },
                                    modifier = Modifier.size(36.dp).testTag("qty_plus_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Aumentar")
                                }
                            }
                        }

                        // Unit Price
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Valor Unitário (R$)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = if (unitPrice == 0.0) "" else unitPrice.toString(),
                                onValueChange = {
                                    unitPrice = it.toDoubleOrNull() ?: 0.0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unit_price_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Discount & Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Desconto (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("discount_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = issueDateInput,
                            onValueChange = { issueDateInput = it },
                            label = { Text("Data de Emissão") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("issue_date_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sessionDates,
                        onValueChange = { sessionDates = it },
                        label = { Text("Datas das Sessões (opcional)") },
                        placeholder = { Text("Ex: 02, 09, 16 e 23 de Novembro") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("session_dates_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Section 3: Payment Method & Status
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pagamento",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimaryDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Forma de Pagamento:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PIX", "Transferência", "Dinheiro", "Cartão").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Status de Liquidação:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = paymentStatus == "PAGO",
                            onClick = { paymentStatus = "PAGO" },
                            label = { Text("✓ PAGO (Quitado)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PaidGreenContainer,
                                selectedLabelColor = PaidGreen
                            )
                        )
                        FilterChip(
                            selected = paymentStatus == "PENDENTE",
                            onClick = { paymentStatus = "PENDENTE" },
                            label = { Text("⏳ PENDENTE (A Receber)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PendingAmberContainer,
                                selectedLabelColor = PendingAmber
                            )
                        )
                    }
                }
            }

            // Section 4: Total & Big Action Button
            Card(
                colors = CardDefaults.cardColors(containerColor = TerracottaContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Valor Total a Emitir:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TerracottaPrimaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "$sessionQuantity sessão(ões) x ${currencyFormat.format(unitPrice)}" +
                                    if (discount > 0) " - ${currencyFormat.format(discount)}" else "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF6A6765)
                            )
                        )
                    }

                    Text(
                        text = currencyFormat.format(totalCalculated),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimaryDark
                        )
                    )
                }
            }

            Button(
                onClick = {
                    val patient = selectedPatient
                    if (patient == null) {
                        Toast.makeText(context, "Selecione um paciente para emitir o recibo", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.saveReceipt(
                        patient = patient,
                        serviceDescription = serviceDescription,
                        sessionQuantity = sessionQuantity,
                        unitPrice = unitPrice,
                        discount = discount,
                        paymentMethod = paymentMethod,
                        paymentStatus = paymentStatus,
                        notes = notes,
                        sessionDates = sessionDates,
                        customDate = issueDateInput,
                        onComplete = { newReceipt ->
                            viewModel.showReceiptPreview(newReceipt)
                            Toast.makeText(context, "Recibo ${newReceipt.receiptNumber} gerado e salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_receipt_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Emitir e Gerar Recibo PDF",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
