package com.ponto.obra

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroPonto(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.Purpose.ENCRYPT_DECRYPT)
        .build()

    private val arquivoSeguro = EncryptedSharedPreferences.create(
        context,
        "registros_offline",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun salvarOffline(tipo: String, latitude: Double, longitude: Double, obra: String): Boolean {
        return try {
            val dataHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("pt", "BR")).format(Date())
            
            val registro = JSONObject().apply {
                put("tipo", tipo)
                put("dataHora", dataHora)
                put("latitude", latitude)
                put("longitude", longitude)
                put("obra", obra)
                put("sincronizado", false)
            }

            arquivoSeguro.edit()
                .putString("ponto_${System.currentTimeMillis()}", registro.toString())
                .apply()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun pegarRegistrosPendentes(): List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        arquivoSeguro.all.forEach { _, valor ->
            try {
                lista.add(JSONObject(valor.toString()))
            } catch (_: Exception) {}
        }
        return lista
    }
}
