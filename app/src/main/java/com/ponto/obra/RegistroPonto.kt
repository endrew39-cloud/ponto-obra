package com.ponto.obra

import java.util.Date

data class RegistroPonto(
    val cpf: String,
    val tipo: String,
    val lat: Double,
    val lon: Double,
    val nomeObra: String,
    val dataHora: Date,
    val enviado: Boolean = false
) {
    fun paraMapa(): Map<String, Any?> {
        return mapOf(
            "cpf" to cpf,
            "tipo" to tipo,
            "latitude" to lat,
            "longitude" to lon,
            "nome_obra" to nomeObra,
            "data_hora" to dataHora.time,
            "enviado" to enviado
        )
    }

    companion object {
        fun criarNovo(cpf: String, tipo: String, lat: Double, lon: Double, obra: String): RegistroPonto {
            return RegistroPonto(
                cpf = cpf,
                tipo = tipo,
                lat = lat,
                lon = lon,
                nomeObra = obra,
                dataHora = Date(),
                enviado = false
            )
        }
    }
}
