package com.example.data.database

import androidx.room.*
import com.example.data.entity.Professional
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfessionalDao {
    @Query("SELECT * FROM professionals")
    fun getAllProfessionals(): Flow<List<Professional>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(professionals: List<Professional>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(professional: Professional)

    @Update
    suspend fun update(professional: Professional)

    @Delete
    suspend fun delete(professional: Professional)
}
