package com.ponto.obra

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ConexaoServidor(context: Context) {
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val configLocal = EncryptedSharedPreferences.create(
        "dados_offline",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val configGeral = ConfigSegura(context)
    private val clienteHttp = OkHttpClient()

    fun temInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val rede = cm.activeNetwork ?: return false
        val capacidade = cm.getNetworkCapabilities(rede) ?: return false
        return capacidade.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capacidade.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    suspend fun enviarRegistro(registro: RegistroPonto): Boolean {
        return try {
            val contexto = Aplicativo.getContexto()
            if (!temInternet(contexto)) {
                salvarOffline(registro)
                return false
            }

            val endereco = configGeral.pegarServidor()
            val jsonEnvio = JSONObject().apply {
                put("cpf", registro.cpfFuncionario)
                put("tipo", registro.tipo)
                put("dataHora", registro.dataHora)
                put("lat", registro.latitude)
                put("lon", registro.longitude)
                put("obra", registro.nomeObra)
                put("hash", registro.hashSeguranca)
            }.toString()

            val requisicao = Request.Builder()
                .url("$endereco/receber_ponto")
                .addHeader("Content-Type", "application/json")
                .post(jsonEnvio.toRequestBody("application/json".toMediaType()))
                .build()

            val resposta = withContext(Dispatchers.IO) {
                clienteHttp.newCall(requisicao).execute()
            }

            resposta.isSuccessful
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
            val obj = JSONObject()
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
