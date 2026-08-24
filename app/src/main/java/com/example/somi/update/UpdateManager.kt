package com.example.somi.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isUpdateAvailable: Boolean = false,
    val currentVersion: String = "0.1",
    val latestVersion: String = "",
    val releaseTitle: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val apkSize: Long = 0L,
    val publishedAt: String = "",
    val errorMessage: String? = null
)

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(val progress: Float) : UpdateDownloadState()
    object ReadyToInstall : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

object UpdateManager {
    private const val GITHUB_OWNER = "piccardino"
    private const val GITHUB_REPO = "SOMI"
    private const val API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    const val CURRENT_VERSION_TAG = "v0.1.1"
    const val CURRENT_VERSION_NAME = "0.1.1"

    suspend fun checkLatestRelease(currentVersion: String = CURRENT_VERSION_NAME): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "SOMI-App")
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonText)

                val tagName = json.optString("tag_name", "")
                val name = json.optString("name", tagName)
                val body = json.optString("body", "")
                val publishedAt = json.optString("published_at", "")

                var downloadUrl = ""
                var apkSize = 0L

                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetName = asset.optString("name", "")
                        if (assetName.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                val hasUpdate = isNewerVersion(currentVersion, tagName)

                UpdateInfo(
                    isUpdateAvailable = hasUpdate && downloadUrl.isNotEmpty(),
                    currentVersion = currentVersion,
                    latestVersion = tagName,
                    releaseTitle = name,
                    releaseNotes = body,
                    downloadUrl = downloadUrl,
                    apkSize = apkSize,
                    publishedAt = publishedAt
                )
            } else if (responseCode == 404) {
                UpdateInfo(
                    isUpdateAvailable = false,
                    currentVersion = currentVersion,
                    errorMessage = "Nessuna release pubblica trovata su GitHub."
                )
            } else {
                UpdateInfo(
                    isUpdateAvailable = false,
                    currentVersion = currentVersion,
                    errorMessage = "Errore server GitHub ($responseCode)."
                )
            }
        } catch (e: Exception) {
            UpdateInfo(
                isUpdateAvailable = false,
                currentVersion = currentVersion,
                errorMessage = "Connessione non disponibile: ${e.localizedMessage ?: "Errore di rete"}"
            )
        }
    }

    private fun isNewerVersion(current: String, latestTag: String): Boolean {
        val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()
        val cleanLatest = latestTag.removePrefix("v").removePrefix("V").trim()

        if (cleanLatest.isEmpty()) return false
        if (cleanCurrent == cleanLatest) return false

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
            }

            val totalSize = connection.contentLength
            val destinationDir = File(context.cacheDir, "updates")
            if (!destinationDir.exists()) destinationDir.mkdirs()

            val apkFile = File(destinationDir, "SOMI-update.apk")
            if (apkFile.exists()) apkFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (totalSize > 0) {
                            val progress = totalBytesRead.toFloat() / totalSize.toFloat()
                            onProgress(progress)
                        }
                    }
                    output.flush()
                }
            }
            apkFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
