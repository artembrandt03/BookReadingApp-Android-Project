package com.example.mobile_dev_project.data.repository

import android.content.ContentValues
import android.util.Log
import com.example.mobile_dev_project.data.UnzipUtils
import jakarta.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FileRepository @Inject constructor(
    private val client: OkHttpClient
){

    fun createFile(baseDir: File, fileName: String): File {
        if (!baseDir.exists()) baseDir.mkdirs()
        return File(baseDir, fileName)
    }

    fun downloadFile(url: String, file: File): Boolean {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) return false

            response.body!!.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    copyStream(inputStream, outputStream)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    fun unzipFile(zipFile: File, targetDir: File) {
        try {
            Log.d(ContentValues.TAG, "Starting unzip of: ${zipFile.absolutePath}")
            Log.d(ContentValues.TAG, "Target directory: ${targetDir.absolutePath}")
            Log.d(ContentValues.TAG, "ZIP file exists: ${zipFile.exists()}")
            Log.d(ContentValues.TAG, "ZIP file size: ${zipFile.length()} bytes")

            // Use UnzipUtils to extract the ZIP file
            UnzipUtils.unzip(zipFile, targetDir.absolutePath)
            Log.d(ContentValues.TAG, "Unzip complete")

        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error during unzip", e)
            e.printStackTrace()
            throw e
        }
    }
    private fun copyStream(input: InputStream, output: FileOutputStream) {
        val buffer = ByteArray(1024)
        var length: Int
        while (input.read(buffer).also { length = it } > 0) {
            output.write(buffer, 0, length)
        }
    }

    fun deleteDirectoryContents(baseDir: File) {
        baseDir.listFiles()?.forEach { it.deleteRecursively() }
    }
}