package com.example.util

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object RemoteConfigManager {

    private const val TAG = "RemoteConfigManager"

    // Primary: Custom Domain Hosting (instant zero-cache control via cPanel)
    // Fallback: GitHub raw content
    private val CONFIG_ENDPOINTS = listOf(
        "https://mm-tools.bdz1.top/app-config.json",
        "https://mm-tools.bdz1.top/config.php",
        "http://mm-tools.bdz1.top/app-config.json",
        "https://raw.githubusercontent.com/mubasshirahmed70-creator/MM-TOOLS-/main/app-config.json"
    )

    data class AppConfig(
        val minRequiredVersion: Int = 1,
        val latestVersion: String = "1.0",
        val isForceUpdate: Boolean = false,
        val isAppDisabled: Boolean = false,
        val disabledTitle: String = "Maintenance Mode",
        val disabledMessage: String = "MM Tools is temporarily disabled for maintenance. Please check our Telegram channel for announcements.",
        val updateTitle: String = "Update Required",
        val updateMessage: String = "A new version of MM Tools is available. Please update the app to continue using it.",
        val downloadUrl: String = "https://t.me/mmtechnical420",
        val telegramUrl: String = "https://t.me/mmtechnical420",
        val forceJoinTelegram: Boolean = false,
        val forceJoinTitle: String = "Join Telegram Channel",
        val forceJoinMessage: String = "Join our official Telegram channel for updates, announcements, and support.",
        val forceJoinUrl: String = "https://t.me/mmtechnical420",
        val forceJoinDismissible: Boolean = true
    )

    sealed class UpdateStatus {
        data class AppDisabled(val title: String, val message: String, val telegramUrl: String) : UpdateStatus()
        data class ForceUpdateRequired(
            val title: String,
            val message: String,
            val downloadUrl: String
        ) : UpdateStatus()
        data class OptionalUpdateAvailable(
            val title: String,
            val message: String,
            val downloadUrl: String
        ) : UpdateStatus()
        data class ForceJoinRequired(
            val title: String,
            val message: String,
            val telegramUrl: String,
            val isDismissible: Boolean
        ) : UpdateStatus()
        object UpToDate : UpdateStatus()
    }

    suspend fun checkAppUpdate(): UpdateStatus = withContext(Dispatchers.IO) {
        for (endpoint in CONFIG_ENDPOINTS) {
            try {
                val separator = if (endpoint.contains("?")) "&" else "?"
                val cacheBusterUrl = "$endpoint${separator}ts=${System.currentTimeMillis()}"
                val url = URL(cacheBusterUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    instanceFollowRedirects = true
                    requestMethod = "GET"
                    useCaches = false
                    defaultUseCaches = false
                    setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                    setRequestProperty("Pragma", "no-cache")
                    setRequestProperty("User-Agent", "MMTools-Android/${BuildConfig.VERSION_NAME}")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.use { it.readText() }.trim()
                    
                    // Verify it is actual JSON, not an HTML 404 page
                    if (!response.startsWith("{") || !response.endsWith("}")) {
                        continue
                    }
                    val json = JSONObject(response)

                    val config = AppConfig(
                        minRequiredVersion = json.optInt("min_required_version", 1),
                        latestVersion = json.optString("latest_version", "1.0"),
                        isForceUpdate = json.optBoolean("force_update", false),
                        isAppDisabled = json.optBoolean("is_app_disabled", false),
                        disabledTitle = json.optString("disabled_title", "Maintenance Mode"),
                        disabledMessage = json.optString(
                            "disabled_message",
                            "MM Tools is temporarily disabled for maintenance. Please check our Telegram channel for announcements."
                        ),
                        updateTitle = json.optString("update_title", "Update Required"),
                        updateMessage = json.optString(
                            "update_message",
                            "A new version of MM Tools is available. Please update the app to continue using it."
                        ),
                        downloadUrl = json.optString(
                            "download_url",
                            "https://t.me/mmtechnical420"
                        ),
                        telegramUrl = json.optString(
                            "telegram_url",
                            "https://t.me/mmtechnical420"
                        ),
                        forceJoinTelegram = json.optBoolean("force_join_telegram", false),
                        forceJoinTitle = json.optString("force_join_title", "Join Telegram Channel"),
                        forceJoinMessage = json.optString(
                            "force_join_message",
                            "Join our official Telegram channel for updates, announcements, and support."
                        ),
                        forceJoinUrl = json.optString("force_join_url", "https://t.me/mmtechnical420"),
                        forceJoinDismissible = json.optBoolean("force_join_dismissible", true)
                    )

                    // 1. Is the whole app remotely disabled?
                    if (config.isAppDisabled) {
                        return@withContext UpdateStatus.AppDisabled(
                            title = config.disabledTitle,
                            message = config.disabledMessage,
                            telegramUrl = config.telegramUrl
                        )
                    }

                    // 2. Is this version older than min required or flagged for force update?
                    val currentVersionCode = BuildConfig.VERSION_CODE
                    val isBelowMin = currentVersionCode < config.minRequiredVersion
                    val isForceOutdated = config.isForceUpdate && (currentVersionCode < config.minRequiredVersion || BuildConfig.VERSION_NAME != config.latestVersion)

                    if (isBelowMin || isForceOutdated) {
                        return@withContext UpdateStatus.ForceUpdateRequired(
                            title = config.updateTitle,
                            message = config.updateMessage,
                            downloadUrl = config.downloadUrl
                        )
                    }

                    // 3. Force Join Telegram required?
                    if (config.forceJoinTelegram) {
                        return@withContext UpdateStatus.ForceJoinRequired(
                            title = config.forceJoinTitle,
                            message = config.forceJoinMessage,
                            telegramUrl = if (config.forceJoinUrl.isNotEmpty()) config.forceJoinUrl else config.telegramUrl,
                            isDismissible = config.forceJoinDismissible
                        )
                    }

                    // 4. Optional update check
                    if (BuildConfig.VERSION_NAME != config.latestVersion && config.minRequiredVersion <= currentVersionCode) {
                        return@withContext UpdateStatus.OptionalUpdateAvailable(
                            title = "New Update Available",
                            message = "Version ${config.latestVersion} is now available with new features and updates.",
                            downloadUrl = config.downloadUrl
                        )
                    }

                    return@withContext UpdateStatus.UpToDate
                } else {
                    Log.w(TAG, "Endpoint $endpoint returned code: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Endpoint $endpoint failed: ${e.message}")
            }
        }

        // If all endpoints failed or no active restriction, default to UpToDate
        return@withContext UpdateStatus.UpToDate
    }
}
