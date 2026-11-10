package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ProfessionalProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfessionalProfileDao {
    @Query("SELECT * FROM professional_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<ProfessionalProfile?>

    @Query("SELECT * FROM professional_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): ProfessionalProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: ProfessionalProfile)
}
