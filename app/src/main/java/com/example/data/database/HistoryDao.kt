package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM processed_files ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ProcessedFileEntity>>

    @Query("SELECT * FROM processed_files ORDER BY timestamp DESC LIMIT 5")
    fun getRecentHistory(): Flow<List<ProcessedFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProcessedFileEntity): Long

    @Delete
    suspend fun deleteFile(file: ProcessedFileEntity)

    @Query("DELETE FROM processed_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM processed_files")
    suspend fun clearAll()
}
