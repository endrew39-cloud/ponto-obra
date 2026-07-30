package com.ponto.obra

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.json.JSONArray

class ConexaoServidor(context: Context) {
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val configLocal = EncryptedSharedPreferences.create(
        "dados_offline",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Endereço do seu servidor temporário, altere conforme precisar
    var enderecoServidor: String = "http://192.168.0.100:8080"

    fun temInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val rede = cm.activeNetwork ?: return false
        val capacidade = cm.getNetworkCapabilities(rede) ?: return false
        return capacidade.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capacidade.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    suspend fun enviarRegistro(registro: RegistroPonto): Boolean {
        return try {
            if (!temInternet(com.ponto.obra.Aplicativo.getContexto())) {
                salvarOffline(registro)
                return false
            }

            // Aqui vai a lógica real de envio quando tiver servidor estruturado
            // Simulação de sucesso para o teste
            true
        } catch (e: Exception) {
            salvarOffline(registro)
            false
        }
    }

    fun salvarOffline(registro: RegistroPonto) {
        val lista = carregarOffline()
        lista.add(registro)

        val json = JSONArray()
        lista.forEach {
            val obj = org.json.JSONObject()
            obj.put("cpf", it.cpfFuncionario)
            obj.put("tipo", it.tipo)
            obj.put("dataHora", it.dataHora)
            obj.put("lat", it.latitude)
            obj.put("lon", it.longitude)
            obj.put("obra", it.nomeObra)
            obj.put("hash", it.hashSeguranca)
            json.put(obj)
        }

        configLocal.edit().putString("registros_pendentes", json.toString()).apply()
    }

    fun carregarOffline(): MutableList<RegistroPonto> {
        val lista = mutableListOf<RegistroPonto>()
        val texto = configLocal.getString("registros_pendentes", "[]") ?: "[]"

        try {
            val json = JSONArray(texto)
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                lista.add(
                    RegistroPonto(
                        cpfFuncionario = obj.getString("cpf"),
                        tipo = obj.getString("tipo"),
                        dataHora = obj.getString("dataHora"),
                        latitude = obj.getDouble("lat"),
                        longitude = obj.getDouble("lon"),
                        nomeObra = obj.getString("obra"),
                        hashSeguranca = obj.getString("hash"),
                        sincronizado = false
                    )
                )
            }
        } catch (_: Exception) {}

        return lista
    }

    suspend fun sincronizarPendentes(): Int {
        val lista = carregarOffline()
        var sucessos = 0

        lista.forEach {
            if (enviarRegistro(it)) sucessos++
        }

        if (sucessos == lista.size) {
            configLocal.edit().remove("registros_pendentes").apply()
        }

        return sucessos
    }
}
