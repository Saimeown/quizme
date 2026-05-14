package com.example.quizme

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizme.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView

    inner class WebAppInterface {
        @JavascriptInterface
        fun updateTheme(isDarkMode: Boolean) {
            runOnUiThread {
                updateSystemBars(isDarkMode)
            }
        }
    }

    private fun updateSystemBars(isDarkMode: Boolean) {
        val color = if (isDarkMode) "#1A1A1A" else "#FAF3E0"
        window.statusBarColor = android.graphics.Color.parseColor(color)
        window.navigationBarColor = android.graphics.Color.parseColor(color)
        
        // Use binding safely
        binding.main.setBackgroundColor(android.graphics.Color.parseColor(color))
        
        // Use webView safely
        if (::webView.isInitialized) {
            webView.setBackgroundColor(android.graphics.Color.parseColor(color))
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            var flags = 0
            if (!isDarkMode) {
                flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            } else {
                // In dark mode, we want light icons (default), so flags = 0 is fine
                // But on some devices we might need to explicitly clear them
                window.decorView.systemUiVisibility = 0
            }
            window.decorView.systemUiVisibility = flags
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize WebView BEFORE calling updateSystemBars
        webView = binding.root.findViewById(R.id.webview)

        // Initial setup
        updateSystemBars(false)

        // Hide UI components not needed for the WebView app
        supportActionBar?.hide()
        binding.toolbar.visibility = View.GONE
        binding.fab.visibility = View.GONE

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configure WebView
        webView.overScrollMode = View.OVER_SCROLL_NEVER
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        webView.addJavascriptInterface(WebAppInterface(), "Android")

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl("file:///android_asset/index.html")

        // Handle back button for WebView navigation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
