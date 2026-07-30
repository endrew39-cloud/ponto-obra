package com.ponto.obra

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.json.JSONArray
import org.json.JSONObject

class ConfigSegura(context: Context) {
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val config = EncryptedSharedPreferences.create(
        "config_empresa",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val SENHA_MESTRA_PADRAO = "dev_ponto_2026"

    fun validarAcessoDev(senhaDigitada: String): Boolean {
        val senhaSalva = config.getString("senha_mestra", SENHA_MESTRA_PADRAO)
        return senhaDigitada == senhaSalva
    }

    fun salvarNomeEmpresa(nome: String) {
        config.edit().putString("nome_empresa", nome).apply()
    }

    fun pegarNomeEmpresa(): String {
        return config.getString("nome_empresa", "Ponto Obra") ?: "Ponto Obra"
    }

    fun salvarServidor(endereco: String) {
        config.edit().putString("endereco_servidor", endereco).apply()
    }

    fun pegarServidor(): String {
        return config.getString("endereco_servidor", "http://192.168.0.100:8080") ?: "http://192.168.0.100:8080"
    }

    fun salvarObra(obra: ObraCadastrada) {
        val lista = pegarTodasObras()
        lista.add(obra)

        val json = JSONArray()
        lista.forEach {
            val obj = JSONObject()
            obj.put("nome", it.nome)
            obj.put("lat", it.latitude)
            obj.put("lon", it.longitude)
            obj.put("raio", it.raioPermitidoMetros)
            json.put(obj)
        }
        config.edit().putString("obras_cadastradas", json.toString()).apply()
    }

    fun pegarTodasObras(): MutableList<ObraCadastrada> {
        val lista = mutableListOf<ObraCadastrada>()
        val texto = config.getString("obras_cadastradas", "[]") ?: "[]"
        try {
            val json = JSONArray(texto)
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                lista.add(
                    ObraCadastrada(
                        nome = obj.getString("nome"),
                        latitude = obj.getDouble("lat"),
                        longitude = obj.getDouble("lon"),
                        raioPermitidoMetros = obj.getDouble("raio")
                    )
                )
            }
        } catch (_: Exception) {}
        return lista
    }

    fun pegarObraAtual(): ObraCadastrada? {
        val nome = pegarNomeEmpresa()
        return pegarTodasObras().find { it.nome == nome }
    }
}
