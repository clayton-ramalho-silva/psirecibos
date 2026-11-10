package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProfessionalProfile
import com.example.data.model.Receipt
import com.example.pdf.ReceiptPdfGenerator
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.PendingAmber
import com.example.ui.theme.PendingAmberContainer
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.TerracottaPrimaryDark
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReceiptPreviewDialog(
    receipt: Receipt,
    profile: ProfessionalProfile,
    onDismiss: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val isPaid = receipt.paymentStatus.equals("PAGO", ignoreCase = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("receipt_preview_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Visualização do Recibo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimaryDark
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_preview_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar visualização"
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Receipt Document
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    // Header band
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(TerracottaPrimary)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clinic Header with circular logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(TerracottaContainer)
                                .border(1.5.dp, TerracottaPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Céia",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = TerracottaPrimaryDark
                                )
                                Text(
                                    text = "Psic.",
                                    fontSize = 9.sp,
                                    color = TerracottaPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = profile.name.uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = Color(0xFF2B2A29)
                                )
                            )
                            Text(
                                text = "${profile.title} • ${profile.professionalRegister}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TerracottaPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "CPF: ${profile.cpf} • PIX: ${profile.pixKey}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = Color(0xFF6A6765)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tarja "RECIBO"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerracottaPrimary)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECIBO DE ATENDIMENTO",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Nº ${receipt.receiptNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = receipt.issueDate,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Boxes DE & PARA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // DE
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = TerracottaContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "DE:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimaryDark
                                )
                                Text(
                                    text = profile.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2B2A29)
                                )
                                Text(
                                    text = profile.title,
                                    fontSize = 9.sp,
                                    color = Color(0xFF6A6765)
                                )
                                Text(
                                    text = profile.cpf,
                                    fontSize = 9.sp,
                                    color = Color(0xFF6A6765)
                                )
                            }
                        }

                        // PARA
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = TerracottaContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "PARA:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimaryDark
                                )
                                Text(
                                    text = receipt.patientName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2B2A29)
                                )
                                Text(
                                    text = "CPF: ${receipt.patientCpf}",
                                    fontSize = 9.sp,
                                    color = Color(0xFF6A6765)
                                )
                                if (receipt.patientPhone.isNotBlank()) {
                                    Text(
                                        text = receipt.patientPhone,
                                        fontSize = 9.sp,
                                        color = Color(0xFF6A6765)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tabela de Serviços
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFE0DCD8), RoundedCornerShape(6.dp))
                    ) {
                        // Table header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF4A3E39))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Descrição", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                            Text("Qtd", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
                            Text("Unit.", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text("Total", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                        }

                        // Table row (zebrado)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9F8F6))
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(
                                    text = receipt.serviceDescription,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2B2A29)
                                )
                                if (receipt.sessionDates.isNotBlank()) {
                                    Text(
                                        text = "Dias: ${receipt.sessionDates}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF6A6765)
                                    )
                                }
                            }
                            Text(
                                text = "${receipt.sessionQuantity}",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.6f),
                                color = Color(0xFF2B2A29)
                            )
                            Text(
                                text = currencyFormat.format(receipt.unitPrice),
                                fontSize = 11.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF2B2A29)
                            )
                            Text(
                                text = currencyFormat.format(receipt.sessionQuantity * receipt.unitPrice),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f),
                                color = Color(0xFF2B2A29)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Totais & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isPaid) PaidGreenContainer else PendingAmberContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPaid) "✓ QUITADO / PAGO" else "⏳ AGUARDANDO PAGAMENTO",
                                color = if (isPaid) PaidGreen else PendingAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Total amount
                        Column(horizontalAlignment = Alignment.End) {
                            if (receipt.discount > 0.0) {
                                Text(
                                    text = "Desconto: - ${currencyFormat.format(receipt.discount)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB3261E)
                                )
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "TOTAL: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimaryDark
                                )
                                Text(
                                    text = currencyFormat.format(receipt.totalAmount),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimaryDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legal Note
                    Text(
                        text = "Válido para fins de comprovação e dedução no Imposto de Renda Pessoa Física (IRPF / Carnê-Leão).",
                        fontSize = 9.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF888481),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Assinatura
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile.signatureName,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TerracottaPrimaryDark
                        )
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(1.dp)
                                .background(Color(0xFF888481))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${profile.name} • ${profile.title}",
                            fontSize = 9.sp,
                            color = Color(0xFF2B2A29),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons (PDF, WhatsApp, Toggle Status)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button 1: Share PDF directly
                    Button(
                        onClick = {
                            shareReceiptPdf(context, receipt, profile)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar / Compartilhar PDF no WhatsApp")
                    }

                    // Button 2: If Pending, Send WhatsApp Friendly Reminder
                    if (!isPaid) {
                        OutlinedButton(
                            onClick = {
                                sendWhatsAppReminder(context, receipt, profile)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_whatsapp_reminder_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = PendingAmber
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Lembrete Amigável no WhatsApp")
                        }
                    }

                    // Button 3: Toggle Paid / Pending status
                    OutlinedButton(
                        onClick = onToggleStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_status_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isPaid) "Marcar como PENDENTE" else "Marcar como PAGO / QUITADO"
                        )
                    }
                }
            }
        }
    }
}

private fun shareReceiptPdf(context: Context, receipt: Receipt, profile: ProfessionalProfile) {
    try {
        val pdfFile = ReceiptPdfGenerator.generateReceiptPdf(context, receipt, profile)
        val intent = ReceiptPdfGenerator.getSharePdfIntent(context, pdfFile, receipt, profile)
        val chooser = Intent.createChooser(intent, "Compartilhar Recibo ${receipt.receiptNumber}")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun sendWhatsAppReminder(context: Context, receipt: Receipt, profile: ProfessionalProfile) {
    try {
        val message = ReceiptPdfGenerator.buildReminderMessage(receipt, profile)
        val cleanPhone = receipt.patientPhone.replace(Regex("[^0-9]"), "")
        val url = if (cleanPhone.length >= 10) {
            val phoneWithDdi = if (cleanPhone.startsWith("55")) cleanPhone else "55$cleanPhone"
            "https://api.whatsapp.com/send?phone=$phoneWithDdi&text=${URLEncoder.encode(message, "UTF-8")}"
        } else {
            "https://api.whatsapp.com/send?text=${URLEncoder.encode(message, "UTF-8")}"
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Não foi possível abrir o WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
