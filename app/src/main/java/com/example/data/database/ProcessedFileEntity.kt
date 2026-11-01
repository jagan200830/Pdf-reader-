package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_files")
data class ProcessedFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val originalFileName: String,
    val toolType: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val originalSizeBytes: Long,
    val mimeType: String,
    val timestamp: Long = System.currentTimeMillis()
)
