package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cpf: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val defaultSessionValue: Double = 180.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
