package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.view.ViewGroup
import java.io.File
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.config.MMToolsConfig
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MMWebView(
    targetUrl: String?,
    isDesktopMode: Boolean,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onWebViewReady: (WebView) -> Unit,
    onErrorNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var defaultMobileUserAgent by remember { mutableStateOf("") }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Intercept back presses for web history
    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    // React to Desktop Mode changes
    LaunchedEffect(isDesktopMode, defaultMobileUserAgent) {
        webViewInstance?.let { wv ->
            if (defaultMobileUserAgent.isNotEmpty()) {
                val newAgent = if (isDesktopMode) {
                    MMToolsConfig.DESKTOP_USER_AGENT
                } else {
                    defaultMobileUserAgent
                }
                if (wv.settings.userAgentString != newAgent) {
                    wv.settings.userAgentString = newAgent
                    wv.settings.useWideViewPort = isDesktopMode
                    wv.settings.loadWithOverviewMode = isDesktopMode
                    wv.reload()
                }
            }
        }
    }

    // React to target URL changes
    LaunchedEffect(targetUrl) {
        if (!targetUrl.isNullOrBlank()) {
            val current = webViewInstance?.url
            if (current != targetUrl) {
                hasError = false
                webViewInstance?.loadUrl(targetUrl)
            }
        } else {
            isLoading = false
            loadingProgress = 0f
            webViewInstance?.stopLoading()
            webViewInstance?.loadUrl("about:blank")
        }
    }

    Box(modifier = modifier.fillMaxSize().background(NavyBackground)) {
        AndroidView(
            factory = { ctx ->
                // Enable safe browsing if available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    try {
                        WebView.startSafeBrowsing(ctx.applicationContext) { /* Safe browsing enabled */ }
                    } catch (_: Exception) {}
                }

                // Pre-create cache directories to prevent Chromium SimpleFileEnumerator error
                try {
                    val codeCacheDir = File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache")
                    File(codeCacheDir, "js").mkdirs()
                    File(codeCacheDir, "wasm").mkdirs()
                } catch (_: Exception) {}

                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Gracefully handle virtualized / emulator environments where DRM rendernode
                    // (/dev/dri/renderD128) is missing to prevent Mesa driver rendernode errors.
                    try {
                        val hasDriNode = File("/dev/dri").exists()
                        val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                                Build.FINGERPRINT.startsWith("unknown") ||
                                Build.MODEL.contains("google_sdk") ||
                                Build.MODEL.contains("Emulator") ||
                                Build.MODEL.contains("Android SDK built for") ||
                                Build.HARDWARE.contains("goldfish") ||
                                Build.HARDWARE.contains("ranchu")
                        if (!hasDriNode && isEmulator) {
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        }
                    } catch (_: Exception) {}

                    // Retain session cookies locally without extracting or transmitting
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    // Store default mobile user agent
                    defaultMobileUserAgent = settings.userAgentString
                    if (isDesktopMode) {
                        settings.userAgentString = MMToolsConfig.DESKTOP_USER_AGENT
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                    }

                    // Native standard WebView settings
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        allowFileAccess = false
                        allowContentAccess = false
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadingProgress = newProgress / 100f
                            isLoading = newProgress < 100
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            if (!title.isNullOrBlank()) {
                                onTitleChanged(title)
                            }
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            if (url == "about:blank" || url.isNullOrBlank()) {
                                isLoading = false
                                loadingProgress = 0f
                                return
                            }
                            isLoading = true
                            hasError = false
                            onUrlChanged(url)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            loadingProgress = 0f
                            if (!url.isNullOrBlank() && url != "about:blank") {
                                onUrlChanged(url)
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                val desc = error?.description?.toString() ?: "Connection failed"
                                errorMessage = desc
                                onErrorNotice("Failed to load page: $desc")
                            }
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            // STRICT SECURITY: Never proceed on SSL errors
                            handler?.cancel()
                            hasError = true
                            errorMessage = "Secure connection could not be established (SSL error)."
                            onErrorNotice(errorMessage)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            val scheme = request.url?.scheme?.lowercase()

                            if (scheme == "http" || scheme == "https") {
                                return false // Load in WebView
                            }

                            // Handle external schemes (e.g., mailto, tel, tg)
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                ctx.startActivity(intent)
                                return true
                            } catch (_: Exception) {
                                onErrorNotice("No application found to handle this link.")
                                return true
                            }
                        }
                    }

                    if (!targetUrl.isNullOrBlank()) {
                        loadUrl(targetUrl)
                    }
                    webViewInstance = this
                    onWebViewReady(this)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_web_view")
        )

        // Sleek cyan loading indicator at top
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(
                progress = { loadingProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = CyanAccent,
                trackColor = NavySurface
            )
        }

        // Graceful error state overlay
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyBackground)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Unable to Load Page",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorMessage.ifBlank { "Please check your network connection and try again." },
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            hasError = false
                            webViewInstance?.reload()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_retry_webview")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
