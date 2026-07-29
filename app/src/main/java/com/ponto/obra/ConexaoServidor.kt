package com.ponto.obra

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ConexaoServidor {
    private val urlBase = "https://married-baked-salutary.ngrok-free.dev"
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun enviarPonto(dados: JSONObject): Boolean {
        return try {
            val corpo = dados.toString().toRequestBody("application/json".toMediaType())
            val requisicao = Request.Builder()
                .url("$urlBase/registrar-ponto")
                .post(corpo)
                .build()

            val resposta = cliente.newCall(requisicao).execute()
            resposta.isSuccessful
        } catch (erro: Exception) {
            Log.e("Conexao", "Sem internet ou erro no servidor", erro)
            false
        }
    }
}
