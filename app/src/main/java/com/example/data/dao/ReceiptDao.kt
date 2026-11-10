package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Receipt
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY id DESC")
    fun getAllReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE id = :id LIMIT 1")
    suspend fun getReceiptById(id: Long): Receipt?

    @Query("SELECT * FROM receipts WHERE patientId = :patientId ORDER BY id DESC")
    fun getReceiptsByPatient(patientId: Long): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE patientName LIKE '%' || :query || '%' OR receiptNumber LIKE '%' || :query || '%' OR patientCpf LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchReceipts(query: String): Flow<List<Receipt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Update
    suspend fun updateReceipt(receipt: Receipt)

    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Query("SELECT COUNT(*) FROM receipts")
    suspend fun getReceiptCount(): Int

    @Query("SELECT * FROM receipts WHERE paymentStatus = :status ORDER BY id DESC")
    fun getReceiptsByStatus(status: String): Flow<List<Receipt>>
}
