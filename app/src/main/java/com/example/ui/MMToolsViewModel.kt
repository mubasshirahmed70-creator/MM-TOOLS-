package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.MMToolsConfig
import com.example.util.PreferencesManager
import com.example.util.RemoteConfigManager
import com.example.util.UidExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MMToolsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    private val _updateStatus =
        MutableStateFlow<RemoteConfigManager.UpdateStatus>(RemoteConfigManager.UpdateStatus.UpToDate)
    val updateStatus: StateFlow<RemoteConfigManager.UpdateStatus> = _updateStatus.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    init {
        checkForAppUpdates()
    }

    fun checkForAppUpdates() {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val status = RemoteConfigManager.checkAppUpdate()
            _updateStatus.value = status
            _isCheckingUpdate.value = false
        }
    }

    fun dismissOptionalUpdate() {
        _updateStatus.value = RemoteConfigManager.UpdateStatus.UpToDate
    }

    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    // Initially null for clean startup
    private val _targetUrl = MutableStateFlow<String?>(null)
    val targetUrl: StateFlow<String?> = _targetUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow("")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _isDesktopMode = MutableStateFlow(prefs.isDesktopModeEnabled)
    val isDesktopMode: StateFlow<Boolean> = _isDesktopMode.asStateFlow()

    private val _showWelcomeDialog = MutableStateFlow(!prefs.isFirstLaunchCompleted)
    val showWelcomeDialog: StateFlow<Boolean> = _showWelcomeDialog.asStateFlow()

    private val _showClearDataDialog = MutableStateFlow(false)
    val showClearDataDialog: StateFlow<Boolean> = _showClearDataDialog.asStateFlow()

    private val _showTwoFactorDialog = MutableStateFlow(false)
    val showTwoFactorDialog: StateFlow<Boolean> = _showTwoFactorDialog.asStateFlow()

    private val _showNameDialog = MutableStateFlow(false)
    val showNameDialog: StateFlow<Boolean> = _showNameDialog.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun onUrlChanged(url: String) {
        _currentUrl.value = url
    }

    fun onTitleChanged(title: String) {
        _pageTitle.value = title
    }

    fun loadUrl(url: String) {
        _targetUrl.value = url
    }

    fun openFacebookHome(webView: WebView?) {
        val homeUrl = MMToolsConfig.FACEBOOK_HOME_URL
        if (_targetUrl.value == homeUrl && webView != null) {
            webView.loadUrl(homeUrl)
        } else {
            _targetUrl.value = homeUrl
        }
    }

    fun openCreateAccount() {
        _targetUrl.value = MMToolsConfig.CREATE_ACCOUNT_URL
    }

    fun openTwoFactorSettings() {
        _targetUrl.value = MMToolsConfig.TWO_FACTOR_AUTH_URL
    }

    fun openTwoFactorGenerator() {
        _showTwoFactorDialog.value = true
    }

    fun dismissTwoFactorGenerator() {
        _showTwoFactorDialog.value = false
    }

    fun openNameGenerator() {
        _showNameDialog.value = true
    }

    fun dismissNameGenerator() {
        _showNameDialog.value = false
    }

    fun toggleDesktopMode() {
        val next = !_isDesktopMode.value
        _isDesktopMode.value = next
        prefs.isDesktopModeEnabled = next
        _userMessage.value = if (next) "Desktop Mode enabled" else "Mobile Mode enabled"
    }

    fun requestClearData() {
        _showClearDataDialog.value = true
    }

    fun dismissClearDataDialog() {
        _showClearDataDialog.value = false
    }

    /**
     * Executes single-tap instant complete clear:
     * 1. Stops ongoing page loading immediately
     * 2. Clears browser history, form data, and RAM/disk cache
     * 3. Purges all cookies and web storage
     * 4. Resets to clean blank screen without stuck progress
     */
    fun executeClearData(webView: WebView?) {
        _showClearDataDialog.value = false
        try {
            webView?.apply {
                stopLoading()
                clearCache(true)
                clearHistory()
                clearFormData()
                loadUrl("about:blank")
            }

            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies {
                cookieManager.flush()
            }

            WebStorage.getInstance().deleteAllData()

            _targetUrl.value = null
            _currentUrl.value = null
            _pageTitle.value = ""
            _userMessage.value = "Data and cache cleared successfully."
        } catch (e: Exception) {
            _userMessage.value = "Failed to clear data: ${e.localizedMessage}"
        }
    }

    /**
     * Extracts and copies Facebook UID only if an active user session exists.
     */
    fun copyUid(webView: WebView?, context: Context) {
        val currentUrl = _currentUrl.value

        // 1. If currently on a profile URL with an explicit numeric UID
        if (!currentUrl.isNullOrBlank()) {
            val uidFromUrl = UidExtractor.extractFromUrl(currentUrl)
            if (!uidFromUrl.isNullOrBlank()) {
                copyToClipboard(context, uidFromUrl, "Facebook UID")
                return
            }
        }

        // 2. Extract logged-in account's UID directly from CookieManager (c_user)
        val loggedInUid = UidExtractor.extractLoggedInUidFromCookieManager()
        if (!loggedInUid.isNullOrBlank()) {
            copyToClipboard(context, loggedInUid, "Facebook UID")
            return
        }

        // 3. Inspect DOM via JavaScript as fallback
        if (webView != null && !currentUrl.isNullOrBlank() && currentUrl != "about:blank") {
            webView.evaluateJavascript(UidExtractor.JS_EXTRACT_UID_SNIPPET) { rawResult ->
                val verifiedUid = UidExtractor.sanitizeCandidate(rawResult)
                if (!verifiedUid.isNullOrBlank()) {
                    copyToClipboard(context, verifiedUid, "Facebook UID")
                } else {
                    _userMessage.value = "No logged-in Facebook account found. Please log in first."
                }
            }
        } else {
            _userMessage.value = "No logged-in Facebook account found. Please log in first."
        }
    }

    /**
     * Fresh & strictly validated Facebook session cookies extractor:
     * Only copies if 'c_user' (logged-in account ID) exists.
     * Merges and cleanly formats fresh cookies across mobile and www domains.
     */
    fun copyFacebookCookies(context: Context) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.flush()

        val cookieWww = cookieManager.getCookie("https://www.facebook.com").orEmpty()
        val cookieMobile = cookieManager.getCookie("https://m.facebook.com").orEmpty()
        val combinedRaw = "$cookieWww; $cookieMobile"

        // Strict Check: c_user must exist with a valid numeric ID
        val loggedInUid = UidExtractor.extractLoggedInUidFromCookieManager()
        val hasCUser = combinedRaw.contains("c_user=") && !loggedInUid.isNullOrBlank()

        if (!hasCUser) {
            _userMessage.value = "No logged-in Facebook account found. Please log in first."
            return
        }

        // Parse, deduplicate, and clean cookies into standard format
        val cookieMap = linkedMapOf<String, String>()
        combinedRaw.split(";").forEach { item ->
            val trimmed = item.trim()
            if (trimmed.contains("=")) {
                val parts = trimmed.split("=", limit = 2)
                val key = parts[0].trim()
                val value = parts[1].trim()
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    cookieMap[key] = value
                }
            }
        }

        if (cookieMap.isEmpty() || !cookieMap.containsKey("c_user")) {
            _userMessage.value = "No logged-in Facebook account found. Please log in first."
            return
        }

        val formattedCookies = cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
        copyToClipboard(context, formattedCookies, "Facebook Cookies")
    }

    private fun copyToClipboard(context: Context, text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        _userMessage.value = "$label copied to clipboard!"
    }

    fun dismissWelcomeDialog() {
        _showWelcomeDialog.value = false
        prefs.isFirstLaunchCompleted = true
    }

    fun joinTelegram(context: Context) {
        dismissWelcomeDialog()
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MMToolsConfig.TELEGRAM_CHANNEL_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            _userMessage.value = "Unable to open Telegram link."
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showNotice(message: String) {
        _userMessage.value = message
    }
}
