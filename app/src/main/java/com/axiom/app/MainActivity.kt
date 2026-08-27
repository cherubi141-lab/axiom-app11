package com.axiom.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var offlineView: LinearLayout

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val results: Array<Uri>? = when {
                result.resultCode != RESULT_OK || data == null -> null
                data.clipData != null -> {
                    val count = data.clipData!!.itemCount
                    Array(count) { i -> data.clipData!!.getItemAt(i).uri }
                }
                data.data != null -> arrayOf(data.data!!)
                else -> null
            }
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }

    private var pendingPermissionRequest: PermissionRequest? = null
    private val micPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
            val req = pendingPermissionRequest
            pendingPermissionRequest = null
            if (req == null) return@registerForActivityResult
            if (granted) {
                req.grant(req.resources)
            } else {
                req.deny()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        offlineView = findViewById(R.id.offlineView)
        findViewById<Button>(R.id.retryButton).setOnClickListener { loadApp() }

        configureWebView()
        loadApp()
    }

    private fun configureWebView() {
        val s: WebSettings = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.allowFileAccess = true
        s.allowContentAccess = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.mediaPlaybackRequiresUserGesture = false
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.setSupportZoom(false)
        s.builtInZoomControls = false
        s.textZoom = 100

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.overScrollMode = View.OVER_SCROLL_NEVER
        webView.isScrollbarFadingEnabled = true

        if (WebViewFeature.isFeatureSupported(WebViewFeature.OFFSCREEN_PRERASTER)) {
            WebSettingsCompat.setOffscreenPreRaster(s, true)
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(s, false)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.visibility = View.VISIBLE
                offlineView.visibility = View.GONE
            }
        } 
        override fun onPermissionRequest(request: PermissionRequest) {
                val needsAudio = request.resources.any { it == PermissionRequest.RESOURCE_AUDIO_CAPTURE }
                if (!needsAudio) {
                    request.deny()
                    return
                }
                val hasPermission = ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    request.grant(request.resources)
                } else {
                    pendingPermissionRequest = request
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                val intent = fileChooserParams?.createIntent()
                return try {
                    if (intent != null) {
                        fileChooserLauncher.launch(intent)
                        true
                    } else {
                        filePathCallback = null
                        false
                    }
                } catch (e: Exception) {
                    filePathCallback = null
                    false
                }
            }
        }
    }

    private fun loadApp() {
        if (isOnline()) {
            webView.visibility = View.VISIBLE
            offlineView.visibility = View.GONE
            webView.loadUrl("file:///android_asset/axiom.html")
        } else {
            webView.visibility = View.GONE
            offlineView.visibility = View.VISIBLE
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

        webView.webChromeClient = object : WebChromeClient() {
