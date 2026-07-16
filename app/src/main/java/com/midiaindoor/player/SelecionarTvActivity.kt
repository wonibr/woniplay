package com.midiaindoor.player

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class SelecionarTvActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("midia_indoor_player", MODE_PRIVATE)

        val dp = resources.displayMetrics.density
        val pad48 = (48 * dp).toInt()
        val pad32 = (32 * dp).toInt()
        val pad24 = (24 * dp).toInt()
        val pad10 = (10 * dp).toInt()
        val pad8 = (8 * dp).toInt()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        root.setPadding(pad32, pad48, pad32, pad32)

        val titulo = TextView(this)
        titulo.text = "Qual TV e essa?"
        titulo.setTextColor(Color.WHITE)
        titulo.textSize = 24f
        titulo.gravity = Gravity.CENTER
        root.addView(titulo)

        val subtitulo = TextView(this)
        subtitulo.text = "Escolha a tela correspondente a este aparelho"
        subtitulo.setTextColor(Color.parseColor("#B9CBBF"))
        subtitulo.textSize = 14f
        subtitulo.gravity = Gravity.CENTER
        subtitulo.setPadding(0, pad8, 0, pad24)
        root.addView(subtitulo)

        val scroll = ScrollView(this)
        val lista = LinearLayout(this)
        lista.orientation = LinearLayout.VERTICAL
        scroll.addView(lista)
        root.addView(scroll)

        setContentView(root)

        val tvsJson = intent.getStringExtra("tvs_json") ?: "[]"
        val array = JSONArray(tvsJson)

        if (array.length() == 0) {
            val vazio = TextView(this)
            vazio.text = "Nenhuma TV cadastrada nessa conta ainda.\nCadastre uma no painel do site primeiro."
            vazio.setTextColor(Color.parseColor("#FF8A75"))
            vazio.gravity = Gravity.CENTER
            lista.addView(vazio)
            return
        }

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val id = item.getString("id")
            val nome = item.getString("nome")

            val botao = Button(this)
            botao.text = nome
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.topMargin = pad10
            botao.setOnClickListener { escolherTv(id, nome) }
            lista.addView(botao, params)
        }
    }

    private fun escolherTv(id: String, nome: String) {
        prefs.edit()
            .putString("tv_id", id)
            .putString("tv_nome", nome)
            .apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
