package com.midiaindoor.player

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var campoUsuario: EditText
    private lateinit var campoSenha: EditText
    private lateinit var textoStatus: TextView
    private lateinit var barraProgresso: ProgressBar
    private lateinit var botaoEntrar: Button
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("midia_indoor_player", MODE_PRIVATE)

        val dp = resources.displayMetrics.density
        val pad48 = (48 * dp).toInt()
        val pad32 = (32 * dp).toInt()
        val pad20 = (20 * dp).toInt()
        val pad12 = (12 * dp).toInt()
        val pad8 = (8 * dp).toInt()
        val largura = (320 * dp).toInt()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(Color.parseColor("#12261C"))
        root.setPadding(pad48, pad48, pad48, pad48)

        val titulo = TextView(this)
        titulo.text = "Midia Indoor Player"
        titulo.setTextColor(Color.WHITE)
        titulo.textSize = 26f
        titulo.gravity = Gravity.CENTER
        root.addView(titulo)

        val subtitulo = TextView(this)
        subtitulo.text = "Entre com sua conta"
        subtitulo.setTextColor(Color.parseColor("#B9CBBF"))
        subtitulo.textSize = 15f
        subtitulo.gravity = Gravity.CENTER
        subtitulo.setPadding(0, pad8, 0, pad32)
        root.addView(subtitulo)

        campoUsuario = EditText(this)
        campoUsuario.hint = "Usuario ou e-mail"
        campoUsuario.setHintTextColor(Color.parseColor("#8FA69A"))
        campoUsuario.setTextColor(Color.WHITE)
        root.addView(campoUsuario, LinearLayout.LayoutParams(largura, LinearLayout.LayoutParams.WRAP_CONTENT))

        campoSenha = EditText(this)
        campoSenha.hint = "Senha"
        campoSenha.setHintTextColor(Color.parseColor("#8FA69A"))
        campoSenha.setTextColor(Color.WHITE)
        campoSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        val paramsSenha = LinearLayout.LayoutParams(largura, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsSenha.topMargin = pad12
        root.addView(campoSenha, paramsSenha)

        botaoEntrar = Button(this)
        botaoEntrar.text = "Entrar"
        val paramsBotao = LinearLayout.LayoutParams(largura, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsBotao.topMargin = pad20
        root.addView(botaoEntrar, paramsBotao)

        barraProgresso = ProgressBar(this)
        barraProgresso.visibility = View.GONE
        val paramsBarra = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsBarra.topMargin = pad20
        root.addView(barraProgresso, paramsBarra)

        textoStatus = TextView(this)
        textoStatus.setTextColor(Color.parseColor("#FF8A75"))
        textoStatus.gravity = Gravity.CENTER
        textoStatus.setPadding(0, pad20, 0, 0)
        root.addView(textoStatus)

        setContentView(root)

        botaoEntrar.setOnClickListener { tentarLogin() }
    }

    private fun tentarLogin() {
        val usuario = campoUsuario.text.toString().trim()
        val senha = campoSenha.text.toString()

        if (usuario.isEmpty() || senha.isEmpty()) {
            textoStatus.text = "Preencha usuario e senha"
            return
        }

        botaoEntrar.isEnabled = false
        barraProgresso.visibility = View.VISIBLE
        textoStatus.text = ""

        executor.execute {
            val resultado = ApiCliente.login(usuario, senha)
            runOnUiThread {
                botaoEntrar.isEnabled = true
                barraProgresso.visibility = View.GONE
                if (resultado.sucesso) {
                    prefs.edit()
                        .putString("token", resultado.token)
                        .putString("usuario", usuario)
                        .apply()
                    val intent = Intent(this, SelecionarTvActivity::class.java)
                    intent.putExtra("tvs_json", ApiCliente.tvsParaJson(resultado.tvs))
                    startActivity(intent)
                    finish()
                } else {
                    textoStatus.text = resultado.mensagem
                }
            }
        }
    }
}
