package com.ponto.obra

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConfigSegura(val contexto: Context) {
    private val pasta: SharedPreferences = contexto.getSharedPreferences("dados_protegidos", Context.MODE_PRIVATE)

    // Salvar e ler valores básicos
    fun salvarValor(chave: String, valor: String) {
        pasta.edit().putString(chave, valor).apply()
    }

    fun pegarValor(chave: String, padrao: String = ""): String {
        return pasta.getString(chave, padrao) ?: padrao
    }

    // Foto de perfil - SALVA SOMENTE NO CELULAR
    fun salvarFotoPerfil(cpf: String, fotoBase64: String) {
        val chave = "foto_$cpf"
        salvarValor(chave, fotoBase64)
    }

    fun pegarFotoPerfil(cpf: String): String? {
        val chave = "foto_$cpf"
        return pegarValor(chave, null)
    }

    // Atualizar lista de obras DIRETO DO SERVIDOR
    suspend fun atualizarListaObrasDoServidor(): Boolean {
        return try {
            val endereco = pegarValor("link_servidor", "").trim().removeSuffix("/")
            if(endereco.isEmpty()) return false

            val url = URL("$endereco/lista-obras")
            val conexao = url.openConnection() as HttpURLConnection
            conexao.setRequestProperty("ngrok-skip-browser-warning", "pontoobra")
            conexao.connectTimeout = 10000
            conexao.readTimeout = 10000

            val resposta = conexao.inputStream.reader().readText()
            val listaJson = JSONArray(resposta)

            // Limpa lista antiga
            val editor = pasta.edit()
            val qtdAntiga = pegarValor("qtd_obras", "0").toInt()
            for(i in 0 until qtdAntiga) {
                editor.remove("obra_${i}_nome")
                editor.remove("obra_${i}_lat")
                editor.remove("obra_${i}_lon")
                editor.remove("obra_${i}_raio")
            }

            // Salva lista nova
            editor.putString("qtd_obras", listaJson.length().toString())
            for(i in 0 until listaJson.length()){
                val obj = listaJson.getJSONObject(i)
                editor.putString("obra_${i}_nome", obj.getString("nome"))
                editor.putString("obra_${i}_lat", obj.getString("lat"))
                editor.putString("obra_${i}_lon", obj.getString("lon"))
                editor.putString("obra_${i}_raio", obj.getString("raio"))
            }
            editor.apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun carregarListaObras(): List<Triple<String, Double, Double, Int>> {
        val lista = mutableListOf<Triple<String, Double, Double, Int>>()
        val qtd = pegarValor("qtd_obras", "0").toInt()
        for(i in 0 until qtd) {
            val nome = pegarValor("obra_${i}_nome")
            val lat = pegarValor("obra_${i}_lat", "0.0").toDouble()
            val lon = pegarValor("obra_${i}_lon", "0.0").toDouble()
            val raio = pegarValor("obra_${i}_raio", "0").toInt()
            lista.add(Triple(nome, lat, lon, raio))
        }
        return lista
    }
}
