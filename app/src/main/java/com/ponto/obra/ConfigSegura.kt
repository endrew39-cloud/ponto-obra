package com.ponto.obra

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ConfigSegura(private val contexto: Context) {

    private val SENHA_MESTRA_PADRAO = "pontoobra2026"
    private val preferencias: SharedPreferences

    init {
        val chaveMestra = MasterKey.Builder(contexto)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        preferencias = EncryptedSharedPreferences.create(
            contexto,
            "configuracoes_seguras",
            chaveMestra,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun validarAcessoDev(senhaDigitada: String): Boolean {
        val senhaSalva = preferencias.getString("senha_mestra", SENHA_MESTRA_PADRAO)
        return senhaDigitada.trim() == senhaSalva
    }

    fun salvarNomeEmpresa(nome: String) {
        preferencias.edit().putString("nome_empresa", nome).apply()
    }

    fun pegarNomeEmpresa(): String {
        return preferencias.getString("nome_empresa", "Minha Obra") ?: "Minha Obra"
    }

    fun salvarServidor(endereco: String) {
        preferencias.edit().putString("endereco_servidor", endereco).apply()
    }

    fun pegarServidor(): String {
        return preferencias.getString("endereco_servidor", "") ?: ""
    }

    fun salvarObra(obra: Obra) {
        val lista = pegarTodasObras().toMutableList()
        lista.add(obra)
        val nomes = lista.joinToString(" | ") { it.nome }
        val lats = lista.joinToString(" | ") { it.latitude.toString() }
        val lons = lista.joinToString(" | ") { it.longitude.toString() }
        val raios = lista.joinToString(" | ") { it.raioPermitidoMetros.toString() }
        preferencias.edit()
            .putString("lista_obras_nomes", nomes)
            .putString("lista_obras_lat", lats)
            .putString("lista_obras_lon", lons)
            .putString("lista_obras_raios", raios)
            .apply()
    }

    fun pegarTodasObras(): List<Obra> {
        val nomes = preferencias.getString("lista_obras_nomes", "") ?: ""
        val lats = preferencias.getString("lista_obras_lat", "") ?: ""
        val lons = preferencias.getString("lista_obras_lon", "") ?: ""
        val raios = preferencias.getString("lista_obras_raios", "") ?: ""

        if (nomes.isEmpty() || lats.isEmpty() || lons.isEmpty() || raios.isEmpty()) return emptyList()

        val listaNomes = nomes.split(" | ").filter { it.isNotEmpty() }
        val listaLats = lats.split(" | ").mapNotNull { it.toDoubleOrNull() }
        val listaLons = lons.split(" | ").mapNotNull { it.toDoubleOrNull() }
        val listaRaios = raios.split(" | ").mapNotNull { it.toIntOrNull() }

        return listaNomes.zip(listaLats.zip(listaLons.zip(listaRaios))) { nome, dados ->
            Obra(nome, dados.first, dados.second.first, dados.second.second)
        }
    }

    fun pegarObraAtual(): Obra? {
        val lista = pegarTodasObras()
        return lista.firstOrNull()
    }
}
