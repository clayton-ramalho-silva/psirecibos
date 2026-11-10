package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProfessionalProfile
import com.example.data.model.Receipt
import com.example.pdf.ReceiptPdfGenerator
import com.example.ui.components.ReceiptPreviewDialog
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.PendingAmber
import com.example.ui.theme.PendingAmberContainer
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaOnContainer
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.TerracottaPrimaryDark
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ClinicViewModel
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(
    viewModel: ClinicViewModel
) {
    val context = LocalContext.current
    val receipts by viewModel.filteredReceipts.collectAsStateWithLifecycle()
    val allReceipts by viewModel.allReceipts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.receiptSearchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.receiptFilterStatus.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val previewReceipt by viewModel.previewReceipt.collectAsStateWithLifecycle()

    var receiptToDelete by remember { mutableStateOf<Receipt?>(null) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    // Summary calculation
    val totalPaid = remember(allReceipts) {
        allReceipts.filter { it.paymentStatus.equals("PAGO", ignoreCase = true) }.sumOf { it.totalAmount }
    }
    val totalPending = remember(allReceipts) {
        allReceipts.filter { it.paymentStatus.equals("PENDENTE", ignoreCase = true) }.sumOf { it.totalAmount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Recibos de Consultas",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "${profile.name} • ${profile.title}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TerracottaPrimary
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.lockApp() },
                        modifier = Modifier.testTag("lock_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloquear Aplicativo",
                            tint = TerracottaPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.CREATE_RECEIPT) },
                containerColor = TerracottaPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("new_receipt_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Novo Recibo", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Quick financial cards banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PaidGreenContainer.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PaidGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Quitados",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PaidGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormat.format(totalPaid),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PendingAmberContainer.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = PendingAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "A Receber",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PendingAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormat.format(totalPending),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A3412)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setReceiptSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_search_input"),
                placeholder = { Text("Buscar por paciente, nº ou CPF...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setReceiptSearch("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "TODOS" to "Todos (${allReceipts.size})",
                    "PAGO" to "Quitados",
                    "PENDENTE" to "Pendentes"
                ).forEach { (key, label) ->
                    val isSelected = activeFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setReceiptFilter(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_chip_$key"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TerracottaContainer,
                            selectedLabelColor = TerracottaOnContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Receipts List
            if (receipts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Nenhum recibo encontrado" else "Nenhum recibo emitido ainda",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toque em 'Novo Recibo' para emitir",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TerracottaPrimary
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(receipts, key = { it.id }) { receipt ->
                        ReceiptItemCard(
                            receipt = receipt,
                            profile = profile,
                            currencyFormat = currencyFormat,
                            onPreview = { viewModel.showReceiptPreview(receipt) },
                            onSharePdf = {
                                try {
                                    val pdfFile = ReceiptPdfGenerator.generateReceiptPdf(context, receipt, profile)
                                    val intent = ReceiptPdfGenerator.getSharePdfIntent(context, pdfFile, receipt, profile)
                                    val chooser = Intent.createChooser(intent, "Compartilhar Recibo ${receipt.receiptNumber}")
                                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(chooser)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            },
                            onWhatsAppReminder = {
                                sendQuickReminder(context, receipt, profile)
                            },
                            onToggleStatus = { viewModel.toggleReceiptPaymentStatus(receipt) },
                            onDelete = { receiptToDelete = receipt }
                        )
                    }
                }
            }
        }
    }

    // Receipt Preview Dialog
    previewReceipt?.let { receipt ->
        ReceiptPreviewDialog(
            receipt = receipt,
            profile = profile,
            onDismiss = { viewModel.closeReceiptPreview() },
            onToggleStatus = { viewModel.toggleReceiptPaymentStatus(receipt) }
        )
    }

    // Delete Confirmation Dialog
    receiptToDelete?.let { receipt ->
        AlertDialog(
            onDismissRequest = { receiptToDelete = null },
            title = { Text("Excluir Recibo") },
            text = { Text("Deseja realmente excluir o recibo ${receipt.receiptNumber} do paciente ${receipt.patientName}? Esta ação é permanente no banco de dados do aparelho.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReceipt(receipt)
                        receiptToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { receiptToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ReceiptItemCard(
    receipt: Receipt,
    profile: ProfessionalProfile,
    currencyFormat: NumberFormat,
    onPreview: () -> Unit,
    onSharePdf: () -> Unit,
    onWhatsAppReminder: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val isPaid = receipt.paymentStatus.equals("PAGO", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() }
            .testTag("receipt_card_${receipt.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Number, Date, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerracottaContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = receipt.receiptNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = receipt.issueDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPaid) PaidGreenContainer else PendingAmberContainer)
                        .clickable { onToggleStatus() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isPaid) "PAGO" else "PENDENTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) PaidGreen else PendingAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Patient Name & Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receipt.patientName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${receipt.serviceDescription} (${receipt.sessionQuantity}x)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Text(
                    text = currencyFormat.format(receipt.totalAmount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimaryDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Action Buttons (Touch targets >= 48dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp).testTag("delete_receipt_${receipt.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir recibo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // If pending, reminder button
                if (!isPaid) {
                    IconButton(
                        onClick = onWhatsAppReminder,
                        modifier = Modifier.size(48.dp).testTag("reminder_button_${receipt.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Lembrete WhatsApp",
                            tint = PendingAmber
                        )
                    }
                }

                // Share / WhatsApp button
                IconButton(
                    onClick = onSharePdf,
                    modifier = Modifier.size(48.dp).testTag("share_receipt_${receipt.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartilhar PDF no WhatsApp",
                        tint = TerracottaPrimary
                    )
                }

                // Preview button
                IconButton(
                    onClick = onPreview,
                    modifier = Modifier.size(48.dp).testTag("view_receipt_${receipt.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Visualizar recibo",
                        tint = TerracottaPrimary
                    )
                }
            }
        }
    }
}

private fun sendQuickReminder(context: Context, receipt: Receipt, profile: ProfessionalProfile) {
    try {
        val message = ReceiptPdfGenerator.buildReminderMessage(receipt, profile)
        val cleanPhone = receipt.patientPhone.replace(Regex("[^0-9]"), "")
        val url = if (cleanPhone.length >= 10) {
            val phoneWithDdi = if (cleanPhone.startsWith("55")) cleanPhone else "55$cleanPhone"
            "https://api.whatsapp.com/send?phone=$phoneWithDdi&text=${URLEncoder.encode(message, "UTF-8")}"
        } else {
            "https://api.whatsapp.com/send?text=${URLEncoder.encode(message, "UTF-8")}"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Não foi possível abrir o WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
