package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val patientId: Long,
    val patientName: String,
    val patientCpf: String,
    val patientPhone: String = "",
    val issueDate: String,
    val serviceDescription: String = "Sessão de Psicanálise Clínica",
    val sessionQuantity: Int = 1,
    val unitPrice: Double = 180.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 180.0,
    val paymentMethod: String = "PIX",
    val paymentStatus: String = "PAGO", // "PAGO" or "PENDENTE"
    val notes: String = "",
    val sessionDates: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
