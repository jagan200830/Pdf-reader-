package com.example.engine

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipEngine {

    fun createZipArchive(filesToZip: List<File>, outputFile: File): Boolean {
        return try {
            val buffer = ByteArray(8192)
            FileOutputStream(outputFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    for (file in filesToZip) {
                        if (!file.exists()) continue
                        FileInputStream(file).use { fis ->
                            val zipEntry = ZipEntry(file.name)
                            zos.putNextEntry(zipEntry)
                            var len: Int
                            while (fis.read(buffer).also { len = it } > 0) {
                                zos.write(buffer, 0, len)
                            }
                            zos.closeEntry()
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createZipFromEntries(entries: Map<String, String>, outputFile: File): Boolean {
        return try {
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    for ((entryPath, content) in entries) {
                        val zipEntry = ZipEntry(entryPath)
                        zos.putNextEntry(zipEntry)
                        zos.write(content.toByteArray(Charsets.UTF_8))
                        zos.closeEntry()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
