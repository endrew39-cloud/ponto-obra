package com.ponto.obra

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConexaoServidor(private val contexto: Context) {

    suspend fun enviarPonto(dadosPonto: JSONObject): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val enderecoBase = ConfigSegura(contexto).pegarServidor().trim()
                if(enderecoBase.isEmpty()) return@withContext false

                val urlCompleta = URL("$enderecoBase/receber_ponto")
                val conexao = urlCompleta.openConnection() as HttpURLConnection

                conexao.requestMethod = "POST"
                conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conexao.doOutput = true
                conexao.connectTimeout = 20000
                conexao.readTimeout = 20000

                conexao.outputStream.use { saida ->
                    saida.write(dadosPonto.toString().toByteArray(Charsets.UTF_8))
                }

                val codigoResposta = conexao.responseCode
                conexao.disconnect()
                return@withContext codigoResposta in 200..299

            } catch (erro: Exception) {
                erro.printStackTrace()
                return@withContext false
            }
        }
    }

    suspend fun sincronizarLista(pontosSalvos: List<JSONObject>): Int {
        var totalSucesso = 0
        pontosSalvos.forEach {
            if(enviarPonto(it)) totalSucesso++
        }
        return totalSucesso
    }
}
