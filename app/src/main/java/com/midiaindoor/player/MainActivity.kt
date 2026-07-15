package com.midiaindoor.player

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var reloadRunnable: Runnable? = null

    companion object {
        private const val PREFS_NAME = "midia_indoor_player"
        private const val KEY_URL = "player_url"
        private const val RETRY_DELAY_MS = 8000L
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        aplicarTelaCheia()

        webView = WebView(this)
        setContentView(webView)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                agendarNovaTentativa()
            }
        }

        // Toque longo em qualquer lugar da tela reabre a configuracao do link
        webView.setOnLongClickListener {
            mostrarDialogoDeUrl()
            true
        }

        val urlSalva = prefs.getString(KEY_URL, null)
        if (!urlSalva.isNullOrBlank()) {
            // Ja tem um link configurado (usuario trocou manualmente antes) -- usa ele
            carregar(urlSalva)
        } else {
            // Primeira vez abrindo: usa o link padrao de fabrica, sem perguntar nada
            val urlPadrao = getString(R.string.default_player_url)
            if (urlPadrao.isNotBlank()) {
                prefs.edit().putString(KEY_URL, urlPadrao).apply()
                carregar(urlPadrao)
            } else {
                mostrarDialogoDeUrl()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) aplicarTelaCheia()
    }

    private fun aplicarTelaCheia() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun carregar(url: String) {
        webView.loadUrl(url)
    }

    private fun agendarNovaTentativa() {
        reloadRunnable?.let { handler.removeCallbacks(it) }
        val runnable = Runnable {
            val url = prefs.getString(KEY_URL, null)
            if (!url.isNullOrBlank()) carregar(url)
        }
        reloadRunnable = runnable
        handler.postDelayed(runnable, RETRY_DELAY_MS)
    }

    private fun mostrarDialogoDeUrl() {
        val input = EditText(this)
        input.hint = "https://seusite.com/player_tv.html"
        prefs.getString(KEY_URL, null)?.let { input.setText(it) }

        val container = FrameLayout(this)
        val padding = (16 * resources.displayMetrics.density).toInt()
        container.setPadding(padding, padding, padding, padding)
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Endereco do player")
            .setMessage("Cole aqui o link do seu player (ex.: player_tv.html no seu servidor).")
            .setView(container)
            .setCancelable(false)
            .setPositiveButton("Salvar e abrir") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    prefs.edit().putString(KEY_URL, url).apply()
                    carregar(url)
                }
            }
            .show()
    }
}
