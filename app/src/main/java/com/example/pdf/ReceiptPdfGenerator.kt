package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.ProfessionalProfile
import com.example.data.model.Receipt
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object ReceiptPdfGenerator {

    private val ptLocale = Locale("pt", "BR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(ptLocale)

    /**
     * Generates a high quality vector PDF receipt matching the user's template
     * (A4: 595 x 842 pt).
     */
    fun generateReceiptPdf(
        context: Context,
        receipt: Receipt,
        profile: ProfessionalProfile
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        drawReceipt(canvas, receipt, profile)

        pdfDocument.finishPage(page)

        // Save to cache dir so it can be shared via FileProvider
        val pdfDir = File(context.cacheDir, "receipts")
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }
        val safeNumber = receipt.receiptNumber.replace("/", "_").replace(" ", "_")
        val file = File(pdfDir, "Recibo_${safeNumber}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        return file
    }

    private fun drawReceipt(canvas: Canvas, receipt: Receipt, profile: ProfessionalProfile) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. White Background
        canvas.drawColor(Color.WHITE)

        // Palette
        val terracottaColor = Color.parseColor("#C05A3E")
        val darkTerracotta = Color.parseColor("#963821")
        val pastelTerracotta = Color.parseColor("#FBF0EC")
        val zebraBg = Color.parseColor("#F9F8F6")
        val textDark = Color.parseColor("#2B2A29")
        val textMuted = Color.parseColor("#6A6765")
        val borderSoft = Color.parseColor("#E0DCD8")

        // Top Header decorative band
        paint.color = terracottaColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, 595f, 8f, paint)

        // 2. Circular Clinic Badge / Logo (Ceia Psic.)
        val logoCenterX = 70f
        val logoCenterY = 60f
        val logoRadius = 32f

        paint.color = pastelTerracotta
        canvas.drawCircle(logoCenterX, logoCenterY, logoRadius, paint)

        paint.color = terracottaColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawCircle(logoCenterX, logoCenterY, logoRadius, paint)

        // Logo text
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Céia", logoCenterX, logoCenterY - 4f, paint)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Psic.", logoCenterX, logoCenterY + 10f, paint)

        // 3. Clinic Header Text
        paint.textAlign = Paint.Align.LEFT
        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 17f
        canvas.drawText(profile.name.uppercase(), 115f, 48f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = terracottaColor
        canvas.drawText(profile.title.uppercase() + " • " + profile.professionalRegister, 115f, 62f, paint)

        paint.color = textMuted
        paint.textSize = 8.5f
        canvas.drawText("CPF: ${profile.cpf} • Tel/WhatsApp: ${profile.phone}", 115f, 75f, paint)
        canvas.drawText("Chave PIX: ${profile.pixKey}", 115f, 87f, paint)

        // 4. "RECIBO" Tarja / Banner
        val bannerTop = 108f
        val bannerHeight = 36f
        val bannerRect = RectF(40f, bannerTop, 555f, bannerTop + bannerHeight)
        paint.color = terracottaColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(bannerRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 16f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("RECIBO DE ATENDIMENTO", 56f, bannerTop + 24f, paint)

        paint.textSize = 11f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Nº ${receipt.receiptNumber}", 540f, bannerTop + 16f, paint)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Emissão: ${receipt.issueDate}", 540f, bannerTop + 29f, paint)

        // 5. DE & PARA Cards side by side
        val cardsTop = 156f
        val cardsHeight = 90f
        val colWidth = 250f

        // "DE" Box
        val deRect = RectF(40f, cardsTop, 40f + colWidth, cardsTop + cardsHeight)
        fillPaint.color = pastelTerracotta
        fillPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(deRect, 6f, 6f, fillPaint)

        paint.color = borderSoft
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(deRect, 6f, 6f, paint)

        // DE content
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.color = darkTerracotta
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("DE (PROFISSIONAL EMISSORA):", 52f, cardsTop + 18f, paint)

        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText(profile.name, 52f, cardsTop + 34f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = textMuted
        canvas.drawText(profile.title, 52f, cardsTop + 48f, paint)
        canvas.drawText("CPF: ${profile.cpf}", 52f, cardsTop + 61f, paint)
        canvas.drawText(profile.professionalRegister, 52f, cardsTop + 74f, paint)

        // "PARA" Box
        val paraRect = RectF(305f, cardsTop, 305f + colWidth, cardsTop + cardsHeight)
        fillPaint.color = pastelTerracotta
        canvas.drawRoundRect(paraRect, 6f, 6f, fillPaint)
        paint.color = borderSoft
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(paraRect, 6f, 6f, paint)

        // PARA content
        paint.style = Paint.Style.FILL
        paint.color = darkTerracotta
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("PARA (PACIENTE / CLIENTE):", 317f, cardsTop + 18f, paint)

        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText(receipt.patientName, 317f, cardsTop + 34f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = textMuted
        canvas.drawText("CPF: ${receipt.patientCpf}", 317f, cardsTop + 48f, paint)
        if (receipt.patientPhone.isNotBlank()) {
            canvas.drawText("Telefone: ${receipt.patientPhone}", 317f, cardsTop + 61f, paint)
        }
        canvas.drawText("Data Ref.: ${receipt.issueDate}", 317f, cardsTop + 74f, paint)

        // 6. Tabela de Atendimento
        val tableTop = 262f
        val tableHeaderHeight = 24f
        val tableRect = RectF(40f, tableTop, 555f, tableTop + tableHeaderHeight)

        // Table Header background
        fillPaint.color = Color.parseColor("#4A3E39")
        canvas.drawRoundRect(tableRect, 4f, 4f, fillPaint)

        // Table Header titles
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("ITEM", 50f, tableTop + 15f, paint)
        canvas.drawText("DESCRIÇÃO DOS SERVIÇOS", 95f, tableTop + 15f, paint)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("QTD", 360f, tableTop + 15f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("VALOR UNIT.", 460f, tableTop + 15f, paint)
        canvas.drawText("MONTANTE", 545f, tableTop + 15f, paint)

        // Table Row 1 (Zebrado suave)
        val rowTop = tableTop + tableHeaderHeight + 2f
        val rowHeight = 44f
        val rowRect = RectF(40f, rowTop, 555f, rowTop + rowHeight)
        fillPaint.color = zebraBg
        canvas.drawRect(rowRect, fillPaint)

        // Row content
        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("01", 54f, rowTop + 20f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(receipt.serviceDescription, 95f, rowTop + 18f, paint)

        if (receipt.sessionDates.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 8f
            paint.color = textMuted
            canvas.drawText("Datas: ${receipt.sessionDates}", 95f, rowTop + 33f, paint)
        }

        paint.color = textDark
        paint.textSize = 9.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(receipt.sessionQuantity.toString(), 360f, rowTop + 22f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(currencyFormat.format(receipt.unitPrice), 460f, rowTop + 22f, paint)

        val subTotal = receipt.sessionQuantity * receipt.unitPrice
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(currencyFormat.format(subTotal), 545f, rowTop + 22f, paint)

        // Table border line bottom
        paint.color = borderSoft
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(40f, rowTop + rowHeight, 555f, rowTop + rowHeight, paint)

        // 7. Totais Box
        val totalsTop = rowTop + rowHeight + 14f
        val totalsWidth = 230f
        val totalsLeft = 555f - totalsWidth
        val totalsHeight = 110f
        val totalsRect = RectF(totalsLeft, totalsTop, 555f, totalsTop + totalsHeight)

        fillPaint.color = pastelTerracotta
        fillPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(totalsRect, 6f, 6f, fillPaint)
        paint.color = borderSoft
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(totalsRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 9.5f
        paint.color = textMuted
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Sub Total:", totalsLeft + 16f, totalsTop + 22f, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = textDark
        canvas.drawText(currencyFormat.format(subTotal), 540f, totalsTop + 22f, paint)

        if (receipt.discount > 0.0) {
            paint.textAlign = Paint.Align.LEFT
            paint.color = textMuted
            canvas.drawText("Desconto:", totalsLeft + 16f, totalsTop + 40f, paint)
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.parseColor("#B3261E")
            canvas.drawText("- " + currencyFormat.format(receipt.discount), 540f, totalsTop + 40f, paint)
        }

        paint.textAlign = Paint.Align.LEFT
        paint.color = textMuted
        canvas.drawText("Taxas / Encargos:", totalsLeft + 16f, totalsTop + 58f, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = textDark
        canvas.drawText("R$ 0,00", 540f, totalsTop + 58f, paint)

        // Divider
        paint.color = borderSoft
        paint.style = Paint.Style.STROKE
        canvas.drawLine(totalsLeft + 12f, totalsTop + 68f, 545f, totalsTop + 68f, paint)

        // TOTAL FINAL HIGHLIGHT
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = darkTerracotta
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL:", totalsLeft + 16f, totalsTop + 90f, paint)

        paint.textSize = 14f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(currencyFormat.format(receipt.totalAmount), 540f, totalsTop + 90f, paint)

        // 8. Payment info & Status Box on the left
        val payRect = RectF(40f, totalsTop, totalsLeft - 16f, totalsTop + totalsHeight)
        fillPaint.color = zebraBg
        canvas.drawRoundRect(payRect, 6f, 6f, fillPaint)
        paint.color = borderSoft
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(payRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.color = darkTerracotta
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("INFORMAÇÕES DE PAGAMENTO", 54f, totalsTop + 22f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = textDark
        canvas.drawText("Forma: ${receipt.paymentMethod}", 54f, totalsTop + 40f, paint)
        canvas.drawText("Chave PIX: ${profile.pixKey}", 54f, totalsTop + 56f, paint)

        // Status Badge
        val statusRect = RectF(54f, totalsTop + 68f, 170f, totalsTop + 94f)
        val isPaid = receipt.paymentStatus.equals("PAGO", ignoreCase = true)
        fillPaint.color = if (isPaid) Color.parseColor("#E7F4EC") else Color.parseColor("#FFF3E0")
        canvas.drawRoundRect(statusRect, 4f, 4f, fillPaint)

        paint.color = if (isPaid) Color.parseColor("#1B693A") else Color.parseColor("#B25E00")
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            if (isPaid) "✓ QUITADO / PAGO" else "⏳ PENDENTE",
            statusRect.centerX(),
            statusRect.centerY() + 3.5f,
            paint
        )

        // 9. Legal Disclaimer for IRPF / Carnê-Leão
        val disclaimerTop = totalsTop + totalsHeight + 24f
        paint.color = textMuted
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(
            "Declaro para os devidos fins de comprovação e dedução no Imposto de Renda Pessoa Física (IRPF / Carnê-Leão)",
            40f,
            disclaimerTop,
            paint
        )
        canvas.drawText(
            "ter recebido de ${receipt.patientName} a quantia de ${currencyFormat.format(receipt.totalAmount)} referente aos serviços profissionais descritos.",
            40f,
            disclaimerTop + 13f,
            paint
        )

        // 10. Signature & Stamp (Céia Mendonça)
        val sigTop = disclaimerTop + 75f
        val sigCenterX = 595f / 2f

        // Signature Line
        paint.color = Color.parseColor("#706C69")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(sigCenterX - 130f, sigTop, sigCenterX + 130f, sigTop, paint)

        // Stylized cursive signature simulation
        paint.style = Paint.Style.FILL
        paint.color = darkTerracotta
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
        paint.textSize = 18f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile.signatureName, sigCenterX, sigTop - 8f, paint)

        // Professional title under line
        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText(profile.name, sigCenterX, sigTop + 14f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = textMuted
        canvas.drawText("${profile.title} • ${profile.professionalRegister}", sigCenterX, sigTop + 27f, paint)

        // Footer note at bottom
        paint.color = Color.parseColor("#9B9895")
        paint.textSize = 7.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Documento gerado em conformidade legal pelo sistema do Consultório de Psicanálise.",
            sigCenterX,
            815f,
            paint
        )
    }

    /**
     * Prepares sharing Intent for the receipt PDF.
     */
    fun getSharePdfIntent(context: Context, pdfFile: File, receipt: Receipt, profile: ProfessionalProfile): android.content.Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val message = buildWhatsAppMessage(receipt, profile)

        return android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Recibo de Consulta - ${receipt.receiptNumber}")
            putExtra(android.content.Intent.EXTRA_TEXT, message)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Builds WhatsApp message with polite text and Pix key.
     */
    fun buildWhatsAppMessage(receipt: Receipt, profile: ProfessionalProfile): String {
        val statusText = if (receipt.paymentStatus.equals("PAGO", ignoreCase = true)) {
            "Constatamos o pagamento com sucesso. Muito obrigada!"
        } else {
            "Lembramos que o pagamento está pendente. Chave PIX: ${profile.pixKey}"
        }

        return """
            *${profile.name} • ${profile.title}*
            Olá, ${receipt.patientName}! Tudo bem?
            
            Segue em anexo o *Recibo Oficial Nº ${receipt.receiptNumber}* referente aos seus atendimentos de psicanálise.
            
            📅 *Data:* ${receipt.issueDate}
            📋 *Descrição:* ${receipt.serviceDescription} (${receipt.sessionQuantity} sessão(ões))
            💰 *Valor Total:* ${currencyFormat.format(receipt.totalAmount)}
            
            $statusText
            
            Agradeço imensamente pela confiança em nosso processo terapêutico! Qualquer dúvida, estou à disposição.
        """.trimIndent()
    }

    /**
     * Builds a friendly gentle reminder for pending payments.
     */
    fun buildReminderMessage(receipt: Receipt, profile: ProfessionalProfile): String {
        return """
            Olá, ${receipt.patientName}! Espero que esteja bem.
            
            Passando apenas para enviar um lembrete gentil sobre a sessão de psicanálise realizada (Recibo ${receipt.receiptNumber}), no valor de ${currencyFormat.format(receipt.totalAmount)}.
            
            🔑 *Chave PIX:* ${profile.pixKey}
            *Favorecido:* ${profile.name}
            
            Assim que realizar a transferência, você pode me enviar o comprovante por aqui. Muito obrigada!
        """.trimIndent()
    }
}
