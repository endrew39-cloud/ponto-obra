package com.ponto.obra

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object SalvarOffline {
    private const val NOME_ARQUIVO = "pontos_offline_cripto.json"
    private val gson = Gson()

    fun salvar(contexto: Context, registro: RegistroPonto) {
        try {
            val lista = pegarTodos(contexto).toMutableList()
            lista.add(registro)
            val arquivo = File(contexto.filesDir, NOME_ARQUIVO)
            arquivo.writeText(gson.toJson(lista))
        } catch (e: Exception) {
            Toast.makeText(contexto, "Erro ao salvar ponto!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun pegarTodos(contexto: Context): List<RegistroPonto> {
        return try {
            val arquivo = File(contexto.filesDir, NOME_ARQUIVO)
            if(!arquivo.exists()) return emptyList()
            val tipo = object : TypeToken<List<RegistroPonto>>() {}.type
            gson.fromJson(arquivo.readText(), tipo) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun limparTodos(contexto: Context) {
        try {
            File(contexto.filesDir, NOME_ARQUIVO).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
