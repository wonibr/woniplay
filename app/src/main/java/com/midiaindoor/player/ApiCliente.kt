package com.midiaindoor.player

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// Fala com o backend (PHP) para login e para saber quais TVs a conta tem.
// TROCAR pela URL real quando o backend estiver pronto.
object ApiCliente {

    private const val BASE_URL = "https://wonicard.com.br/api"

    data class TvInfo(val id: String, val nome: String)

    data class ResultadoLogin(
        val sucesso: Boolean,
        val mensagem: String,
        val token: String? = null,
        val tvs: List<TvInfo> = emptyList()
    )

    fun login(usuario: String, senha: String): ResultadoLogin {
        return try {
            val url = URL("$BASE_URL/login.php")
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "POST"
            conexao.doOutput = true
            conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conexao.connectTimeout = 10000
            conexao.readTimeout = 10000

            val corpo = JSONObject()
            corpo.put("usuario", usuario)
            corpo.put("senha", senha)

            OutputStreamWriter(conexao.outputStream).use {
                it.write(corpo.toString())
                it.flush()
            }

            val codigo = conexao.responseCode
            if (codigo in 200..299) {
                val resposta = BufferedReader(InputStreamReader(conexao.inputStream)).use { it.readText() }
                val json = JSONObject(resposta)
                val sucesso = json.optBoolean("sucesso", false)
                if (sucesso) {
                    val tvsArray = json.optJSONArray("tvs")
                    val listaTvs = mutableListOf<TvInfo>()
                    if (tvsArray != null) {
                        for (i in 0 until tvsArray.length()) {
                            val item = tvsArray.getJSONObject(i)
                            listaTvs.add(TvInfo(item.getString("id"), item.getString("nome")))
                        }
                    }
                    ResultadoLogin(true, "OK", json.optString("token"), listaTvs)
                } else {
                    ResultadoLogin(false, json.optString("mensagem", "Usuario ou senha invalidos"))
                }
            } else {
                ResultadoLogin(false, "Erro do servidor (codigo $codigo)")
            }
        } catch (e: Exception) {
            ResultadoLogin(false, "Sem conexao ou servidor indisponivel: " + (e.message ?: ""))
        }
    }

    fun tvsParaJson(tvs: List<TvInfo>): String {
        val array = JSONArray()
        tvs.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("nome", it.nome)
            array.put(obj)
        }
        return array.toString()
    }
}
