package com.midiaindoor.player

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences
    private lateinit var debugText: TextView
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

        val root = FrameLayout(this)

        webView = WebView(this)
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        root.addView(
            webView,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )

        // Camada de diagnostico: mostra qual link esta tentando abrir, e se der
        // erro, mostra o erro exato na tela (em vez de so ficar branco sem explicar nada).
        debugText = TextView(this)
        debugText.setBackgroundColor(Color.BLACK)
        debugText.setTextColor(Color.WHITE)
        debugText.textSize = 16f
        val pad = (24 * resources.displayMetrics.density).toInt()
        debugText.setPadding(pad, pad, pad, pad)
        debugText.gravity = Gravity.TOP or Gravity.START
        root.addView(
            debugText,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )

        setContentView(root)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                debugText.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                val msg = "ERRO ao carregar:\n" + (request?.url?.toString() ?: "?") +
                    "\n\ncodigo: " + (error?.errorCode ?: "?") +
                    "\ndescricao: " + (error?.description ?: "?")
                debugText.text = msg
                debugText.visibility = View.VISIBLE
                agendarNovaTentativa()
            }
        }

        // Toque longo em qualquer lugar da tela reabre a configuracao do link
        webView.setOnLongClickListener {
            mostrarDialogoDeUrl()
            true
        }

        val urlSalva = prefs.getString(KEY_URL, null)
        val urlParaCarregar: String? = if (!urlSalva.isNullOrBlank()) {
            urlSalva
        } else {
            val urlPadrao = getString(R.string.default_player_url)
            if (urlPadrao.isNotBlank()) {
                prefs.edit().putString(KEY_URL, urlPadrao).apply()
                urlPadrao
            } else {
                null
            }
        }

        if (urlParaCarregar != null) {
            debugText.text = "Tentando abrir:\n" + urlParaCarregar
            carregar(urlParaCarregar)
        } else {
            mostrarDialogoDeUrl()
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
