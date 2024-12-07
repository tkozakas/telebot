package com.telebot.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Component
class MediaUtil {

    @Value("\${media.output-dir}")
    private lateinit var outputDir: String

    private val tempFiles = mutableListOf<File>()

    private fun ensureOutputDirExists() {
        val dirPath = Paths.get(outputDir)
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath)
        }
    }

    fun downloadFile(url: String, fileName: String): File {
        ensureOutputDirExists()
        val file = File(outputDir, fileName)
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.inputStream.use { input ->
                file.outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            }
            if (!file.exists()) {
                throw IOException("Failed to download file: $url")
            }
            return file
        } catch (e: Exception) {
            println("Error downloading file: ${e.message}")
            throw e
        }
    }

    fun convertGifToMp4(gifUrl: String): String? {
        return try {
            ensureOutputDirExists()
            val uniqueId = UUID.randomUUID().toString()
            val gifFile = downloadFile(gifUrl, "temp_$uniqueId.gif")
            val mp4File = File(outputDir, "output_$uniqueId.mp4")

            if (runFfmpegConversion(gifFile, mp4File)) {
                gifFile.delete()
                tempFiles.add(mp4File)
                mp4File.absolutePath
            } else {
                gifFile.delete()
                null
            }
        } catch (e: Exception) {
            println("Error converting GIF to MP4: ${e.message}")
            null
        }
    }

    private fun runFfmpegConversion(inputFile: File, outputFile: File): Boolean {
        println("Converting GIF to MP4. Input: ${inputFile.absolutePath}, Output: ${outputFile.absolutePath}")

        val process = ProcessBuilder(
            "ffmpeg",
            "-i", inputFile.absolutePath,
            "-preset", "ultrafast",
            "-movflags", "faststart",
            "-pix_fmt", "yuv420p",
            outputFile.absolutePath
        ).start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            println("FFmpeg conversion failed. Exit code: $exitCode")
        }
        return exitCode == 0
    }

    fun deleteTempFiles() {
        tempFiles.forEach { file ->
            if (file.exists() && !file.delete()) {
                println("Failed to delete temp file: ${file.absolutePath}")
            }
        }
        tempFiles.clear()
    }
}
