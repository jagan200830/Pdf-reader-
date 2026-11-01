package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM processed_files ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ProcessedFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProcessedFileEntity): Long

    @Query("DELETE FROM processed_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM processed_files")
    suspend fun clearAll()
}
