package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "professional_profile")
data class ProfessionalProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Laudiceia Mendonça",
    val title: String = "Psicanalista Clínica",
    val cpf: String = "123.456.789-00",
    val professionalRegister: String = "Reg. Psicanálise 4829/SP",
    val phone: String = "(11) 98765-4321",
    val email: String = "laudiceia@psic.com",
    val pixKey: String = "laudiceia.pix@email.com",
    val address: String = "Av. Paulista, 1000 - Cj. 82 - Bela Vista, São Paulo - SP",
    val signatureName: String = "Céia Mendonça",
    val pinCode: String = "1234"
)
