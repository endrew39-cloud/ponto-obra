package com.ponto.obra

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

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
}
