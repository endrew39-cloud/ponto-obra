package com.ponto.obra

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConexaoServidor(private val contexto: Context) {

    suspend fun enviarPonto(dadosPonto: JSONObject): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var enderecoBase = ConfigSegura(contexto).pegarServidor().trim()
                if(enderecoBase.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(contexto, "Endereço do servidor não configurado!", Toast.LENGTH_LONG).show()
                    }
                    return@withContext false
                }

                if(enderecoBase.endsWith("/")) {
                    enderecoBase = enderecoBase.dropLast(1)
                }

                val urlCompleta = URL("$enderecoBase/receber_ponto")
                val conexao = urlCompleta.openConnection() as HttpURLConnection

                conexao.setRequestProperty("ngrok-skip-browser-warning", "pontoobra")
                conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conexao.setRequestProperty("Accept", "application/json")
                conexao.requestMethod = "POST"
                conexao.doOutput = true
                conexao.connectTimeout = 25000
                conexao.readTimeout = 25000

                conexao.outputStream.use { saida ->
                    saida.write(dadosPonto.toString().toByteArray(Charsets.UTF_8))
                }

                val codigo = conexao.responseCode
                conexao.disconnect()

                return@withContext codigo in 200..299

            } catch (erro: Exception) {
                erro.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(contexto, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
                }
                return@withContext false
            }
        }
    }

    suspend fun sincronizarLista(pontosSalvos: List<JSONObject>): Int {
        var sucessos = 0
        pontosSalvos.forEach {
            if(enviarPonto(it)) sucessos++
        }
        return sucessos
    }
}
