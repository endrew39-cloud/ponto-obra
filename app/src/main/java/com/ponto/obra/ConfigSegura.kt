package com.ponto.obra

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
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
        val raios = lista.joinToString(" | ") { it.raioPermitidoMetros.toString() }
        preferencias.edit()
            .putString("lista_obras_nomes", nomes)
            .putString("lista_obras_raios", raios)
            .apply()
    }

    fun pegarTodasObras(): List<Obra> {
        val nomes = preferencias.getString("lista_obras_nomes", "") ?: ""
        val raios = preferencias.getString("lista_obras_raios", "") ?: ""

        if (nomes.isEmpty() || raios.isEmpty()) return emptyList()

        val listaNomes = nomes.split(" | ").filter { it.isNotEmpty() }
        val listaRaios = raios.split(" | ").mapNotNull { it.toIntOrNull() }

        return listaNomes.zip(listaRaios) { n, r -> Obra(n, r) }
    }

    fun pegarObraAtual(): Obra? {
        val lista = pegarTodasObras()
        return lista.firstOrNull()
    }
}
