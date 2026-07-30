package com.ponto.obra

import kotlin.math.*

data class ObraCadastrada(
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val raioPermitidoMetros: Double
)

object ValidacaoLocal {
    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val raioTerra = 6371000
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return raioTerra * c
    }

    fun estaDentroDaObra(latUsuario: Double, lonUsuario: Double, obra: ObraCadastrada): Boolean {
        val distancia = calcularDistancia(
            latUsuario, lonUsuario,
            obra.latitude, obra.longitude
        )
        return distancia <= obra.raioPermitidoMetros
    }
}
