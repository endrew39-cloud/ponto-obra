package com.ponto.obra

import kotlin.math.*

object ValidacaoLocal {
    fun estaDentroDaObra(latUsuario: Double, lonUsuario: Double, obra: Obra): Boolean {
            // Coordenada de teste, depois altera para a sua obra
            val latObra = -22.9068
            val lonObra = -43.1729
            
            val distancia = calcularDistancia(latUsuario, lonUsuario, latObra, lonObra)
            return distancia <= obra.raioPermitidoMetros
        }

    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val raioTerra = 6371000 // metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return raioTerra * c
    }
}
