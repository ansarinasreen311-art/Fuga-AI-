package com.example.builder

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ExportResult(
    val zipFile: File,
    val zipUri: Uri,
    val previewFile: File?,
    val previewUri: Uri?
)

object ProjectZipExporter {

    fun exportProjectToZip(
        context: Context,
        projectName: String,
        files: Map<String, String>,
        isWebsite: Boolean = false
    ): ExportResult {
        val safeName = projectName.lowercase().replace(Regex("[^a-z0-9_]"), "_").take(30)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val zipFile = File(exportDir, "${safeName}_source.zip")

        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                for ((path, content) in files) {
                    val entryName = if (path.startsWith("/")) path.substring(1) else path
                    val entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    zos.write(content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
        }

        val authority = "${context.packageName}.fileprovider"
        val zipUri = FileProvider.getUriForFile(context, authority, zipFile)

        var previewFile: File? = null
        var previewUri: Uri? = null

        if (isWebsite) {
            val htmlContent = files["index.html"] ?: files.entries.firstOrNull { it.key.endsWith(".html") }?.value
            if (htmlContent != null) {
                // To make sure all linked css and js work in local browser, inline them or save to directory
                val webDir = File(exportDir, safeName).apply { mkdirs() }
                for ((relPath, content) in files) {
                    val target = File(webDir, relPath)
                    target.parentFile?.mkdirs()
                    target.writeText(content)
                }

                previewFile = File(webDir, "index.html")
                if (!previewFile.exists()) {
                    previewFile.writeText(htmlContent)
                }
                previewUri = FileProvider.getUriForFile(context, authority, previewFile)
            }
        }

        return ExportResult(
            zipFile = zipFile,
            zipUri = zipUri,
            previewFile = previewFile,
            previewUri = previewUri
        )
    }

    fun shareZipFile(context: Context, zipUri: Uri, projectName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, zipUri)
            putExtra(Intent.EXTRA_SUBJECT, "$projectName - Source Code (.zip)")
            putExtra(Intent.EXTRA_TEXT, "Here is the generated project source code for $projectName.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Download / Share $projectName .ZIP").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun openInBrowser(context: Context, previewUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(previewUri, "text/html")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: chooser
            val chooser = Intent.createChooser(intent, "Open in Browser").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    fun parseFilesJson(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val obj = JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optString(key, "")
            }
        } catch (e: Exception) {
            // If raw text or fallback
            map["main_file.txt"] = jsonStr
        }
        return map
    }

    fun filesToJson(map: Map<String, String>): String {
        val obj = JSONObject()
        for ((k, v) in map) {
            obj.put(k, v)
        }
        return obj.toString()
    }
}
