package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.MMToolsScreen
import com.example.ui.theme.MMToolsTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pre-create WebView cache directories to prevent Chromium SimpleFileEnumerator
        // and SimpleIndexFile "No such file or directory" error on clean startup
        try {
            val cacheDir = cacheDir
            val jsCodeCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            val wasmCodeCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!jsCodeCache.exists()) jsCodeCache.mkdirs()
            if (!wasmCodeCache.exists()) wasmCodeCache.mkdirs()
        } catch (_: Exception) {}

        enableEdgeToEdge()
        setContent {
            MMToolsTheme {
                MMToolsScreen()
            }
        }
    }
}
