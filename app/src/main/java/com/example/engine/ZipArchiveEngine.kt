package com.example.engine

import android.content.Context
import androidx.core.content.FileProvider
import com.example.data.model.ProcessedOutputFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ZipArchiveEngine {

    suspend fun createZipArchive(
        context: Context,
        files: List<ProcessedOutputFile>,
        zipFileName: String = "docmaster_batch_${System.currentTimeMillis()}.zip"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outDir = File(context.cacheDir, "docmaster_output")
        if (!outDir.exists()) outDir.mkdirs()

        val zipFile = File(outDir, zipFileName)
        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                for (file in files) {
                    val diskFile = File(file.filePath)
                    if (diskFile.exists()) {
                        zos.putNextEntry(ZipEntry(file.name))
                        FileInputStream(diskFile).use { fis ->
                            fis.copyTo(zos)
                        }
                        zos.closeEntry()
                    }
                }
            }
        }

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zipFile)
        ProcessedOutputFile(
            name = zipFile.name,
            uri = outUri,
            filePath = zipFile.absolutePath,
            sizeBytes = zipFile.length(),
            mimeType = "application/zip"
        )
    }
}
