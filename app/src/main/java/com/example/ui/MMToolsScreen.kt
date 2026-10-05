package com.example.ui

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ClearDataDialog
import com.example.ui.components.CompactToolbar
import com.example.ui.components.FloatingActionButtonsGroup
import com.example.ui.components.ForceUpdateDialog
import com.example.ui.components.MMWebView
import com.example.ui.components.TwoFactorDialog
import com.example.ui.components.UsaNameDialog
import com.example.ui.components.WelcomeDialog
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextPrimary

@Composable
fun MMToolsScreen(
    viewModel: MMToolsViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUrl by viewModel.currentUrl.collectAsStateWithLifecycle()
    val targetUrl by viewModel.targetUrl.collectAsStateWithLifecycle()
    val isDesktopMode by viewModel.isDesktopMode.collectAsStateWithLifecycle()
    val showWelcomeDialog by viewModel.showWelcomeDialog.collectAsStateWithLifecycle()
    val showClearDataDialog by viewModel.showClearDataDialog.collectAsStateWithLifecycle()
    val showTwoFactorDialog by viewModel.showTwoFactorDialog.collectAsStateWithLifecycle()
    val showNameDialog by viewModel.showNameDialog.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkForAppUpdates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            // Sleek action buttons row directly at top
            CompactToolbar(
                isDesktopMode = isDesktopMode,
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                onCreateAccountClick = { viewModel.openCreateAccount() },
                onCopyUidClick = { viewModel.copyUid(webViewRef, context) },
                onCopyCookieClick = { viewModel.copyFacebookCookies(context) },
                onTwoFactorClick = { viewModel.openTwoFactorSettings() },
                onClearDataClick = { viewModel.requestClearData() }
            )
        },
        floatingActionButton = {
            // Bottom circular Facebook FAB & Quick Utility FAB (Name & Key options)
            FloatingActionButtonsGroup(
                onFacebookClick = { viewModel.openFacebookHome(webViewRef) },
                onNameClick = { viewModel.openNameGenerator() },
                onKeyClick = { viewModel.openTwoFactorGenerator() },
                modifier = Modifier.padding(bottom = 12.dp, end = 4.dp)
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = NavySurface,
                    contentColor = TextPrimary,
                    actionColor = CyanAccent
                )
            }
        },
        containerColor = NavyBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NavyBackground)
        ) {
            MMWebView(
                targetUrl = targetUrl,
                isDesktopMode = isDesktopMode,
                onUrlChanged = { url -> viewModel.onUrlChanged(url) },
                onTitleChanged = { title -> viewModel.onTitleChanged(title) },
                onWebViewReady = { wv -> webViewRef = wv },
                onErrorNotice = { error -> viewModel.showNotice(error) }
            )
        }
    }

    // First Launch Welcome Dialog
    if (showWelcomeDialog) {
        WelcomeDialog(
            onJoinTelegram = { viewModel.joinTelegram(context) },
            onDismiss = { viewModel.dismissWelcomeDialog() }
        )
    }

    // USA Name Generator Dialog
    if (showNameDialog) {
        UsaNameDialog(
            onDismiss = { viewModel.dismissNameGenerator() },
            onCopiedNotice = { notice -> viewModel.showNotice(notice) }
        )
    }

    // 2FA TOTP Code Generator Dialog
    if (showTwoFactorDialog) {
        TwoFactorDialog(
            onDismiss = { viewModel.dismissTwoFactorGenerator() },
            onOpenSettings = { viewModel.openTwoFactorSettings() }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        ClearDataDialog(
            onConfirm = { viewModel.executeClearData(webViewRef) },
            onDismiss = { viewModel.dismissClearDataDialog() }
        )
    }

    // Force Update / Maintenance Mode Dialog (Highest Priority)
    ForceUpdateDialog(
        status = updateStatus,
        isChecking = isCheckingUpdate,
        onRetry = { viewModel.checkForAppUpdates() },
        onDismissOptional = { viewModel.dismissOptionalUpdate() }
    )
}
