package com.ponto.obra

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class ConfigSegura(val contexto: Context) {
    private val pasta: SharedPreferences = contexto.getSharedPreferences("dados_protegidos", Context.MODE_PRIVATE)

    fun salvarValor(chave: String, valor: String) {
        pasta.edit().putString(chave, valor).apply()
    }

    fun pegarValor(chave: String, padrao: String = ""): String {
        return pasta.getString(chave, padrao) ?: padrao
    }

    data class Obra(val nome: String, val lat: Double, val lon: Double, val raio: Int)

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

            salvarValor("qtd_obras", listaJson.length().toString())
            for(i in 0 until listaJson.length()){
                val obj = listaJson.getJSONObject(i)
                salvarValor("obra_${i}_nome", obj.getString("nome"))
                salvarValor("obra_${i}_lat", obj.getString("lat"))
                salvarValor("obra_${i}_lon", obj.getString("lon"))
                salvarValor("obra_${i}_raio", obj.getString("raio"))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun carregarListaObras(): List<Obra> {
        val lista = mutableListOf<Obra>()
        val qtd = pegarValor("qtd_obras", "0").toInt()
        for(i in 0 until qtd) {
            val nome = pegarValor("obra_${i}_nome")
            val lat = pegarValor("obra_${i}_lat", "0.0").toDouble()
            val lon = pegarValor("obra_${i}_lon", "0.0").toDouble()
            val raio = pegarValor("obra_${i}_raio", "0").toInt()
            lista.add(Obra(nome, lat, lon, raio))
        }
        return lista
    }
}
