package com.ponto.obra

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConexaoServidor(val contexto: Context) {
    private val config = ConfigSegura(contexto)

    suspend fun pegarHorarioOficial(): Triple<String, String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val endereco = config.pegarValor("link_servidor", "").trim().removeSuffix("/")
                if(endereco.isEmpty()) return@withContext null

                val url = URL("$endereco/pegar-horario")
                val conexao = url.openConnection() as HttpURLConnection
                conexao.setRequestProperty("ngrok-skip-browser-warning", "pontoobra")
                conexao.connectTimeout = 8000
                conexao.readTimeout = 8000

                val resposta = conexao.inputStream.reader().readText()
                val json = JSONObject(resposta)

                return@withContext Triple(
                    json.getString("data"),
                    json.getString("hora"),
                    json.getString("padrao")
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    suspend fun verificarDadosUsuario(cpf: String): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                val endereco = config.pegarValor("link_servidor", "").trim().removeSuffix("/")
                val url = URL("$endereco/verificar-usuario?cpf=$cpf")
                val conexao = url.openConnection() as HttpURLConnection
                conexao.setRequestProperty("ngrok-skip-browser-warning", "pontoobra")
                
                val resposta = conexao.inputStream.reader().readText()
                return@withContext JSONObject(resposta)
            } catch (e: Exception) {
                return@withContext null
            }
        }
    }

    suspend fun enviarPonto(registro: Map<String, Any>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val endereco = config.pegarValor("link_servidor", "").trim().removeSuffix("/")
                val url = URL("$endereco/receber_ponto")
                val conexao = url.openConnection() as HttpURLConnection
                conexao.requestMethod = "POST"
                conexao.setRequestProperty("Content-Type", "application/json")
                conexao.setRequestProperty("ngrok-skip-browser-warning", "pontoobra")
                conexao.doOutput = true

                val jsonEnvio = JSONObject(registro).toString()
                conexao.outputStream.write(jsonEnvio.toByteArray(Charsets.UTF_8))

                val resposta = conexao.responseCode
                return@withContext resposta in 200..299
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
    }
}
