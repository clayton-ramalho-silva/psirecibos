package com.example.data.repository

import com.example.data.dao.PatientDao
import com.example.data.dao.ProfessionalProfileDao
import com.example.data.dao.ReceiptDao
import com.example.data.model.Patient
import com.example.data.model.ProfessionalProfile
import com.example.data.model.Receipt
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ClinicRepository(
    private val patientDao: PatientDao,
    private val receiptDao: ReceiptDao,
    private val profileDao: ProfessionalProfileDao
) {
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()
    val allReceipts: Flow<List<Receipt>> = receiptDao.getAllReceipts()
    val professionalProfile: Flow<ProfessionalProfile?> = profileDao.getProfile()

    suspend fun getPatientById(id: Long): Patient? = patientDao.getPatientById(id)
    suspend fun insertPatient(patient: Patient): Long = patientDao.insertPatient(patient)
    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)
    suspend fun deletePatient(patient: Patient) = patientDao.deletePatient(patient)
    fun searchPatients(query: String): Flow<List<Patient>> = patientDao.searchPatients(query)

    suspend fun getReceiptById(id: Long): Receipt? = receiptDao.getReceiptById(id)
    suspend fun insertReceipt(receipt: Receipt): Long = receiptDao.insertReceipt(receipt)
    suspend fun updateReceipt(receipt: Receipt) = receiptDao.updateReceipt(receipt)
    suspend fun deleteReceipt(receipt: Receipt) = receiptDao.deleteReceipt(receipt)
    fun searchReceipts(query: String): Flow<List<Receipt>> = receiptDao.searchReceipts(query)
    fun getReceiptsByPatient(patientId: Long): Flow<List<Receipt>> = receiptDao.getReceiptsByPatient(patientId)

    suspend fun getProfileSync(): ProfessionalProfile? = profileDao.getProfileSync()
    suspend fun updateProfile(profile: ProfessionalProfile) = profileDao.insertOrUpdate(profile)

    suspend fun generateNextReceiptNumber(): String {
        val count = receiptDao.getReceiptCount() + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return "REC-$year-${count.toString().padStart(3, '0')}"
    }
}
