package com.example.data.database

import androidx.room.*
import com.example.data.entity.BookingSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingSlotDao {
    @Query("SELECT * FROM booking_slots ORDER BY id ASC")
    fun getAllSlots(): Flow<List<BookingSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slot: BookingSlot)

    @Update
    suspend fun update(slot: BookingSlot)

    @Delete
    suspend fun delete(slot: BookingSlot)

    @Query("DELETE FROM booking_slots WHERE id = :id")
    suspend fun deleteById(id: Int)
}
