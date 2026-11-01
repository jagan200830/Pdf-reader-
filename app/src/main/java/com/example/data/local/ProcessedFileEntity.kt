package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_files")
data class ProcessedFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val toolType: String,
    val fileType: String,
    val filePath: String,
    val originalSizeBytes: Long,
    val processedSizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val mimeType: String
)
