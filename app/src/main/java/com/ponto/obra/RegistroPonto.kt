package com.ponto.obra

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RegistroPonto(
    val id: String = "",
    val cpfFuncionario: String = "",
    val tipo: String = "",
    val dataHora: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val nomeObra: String = "",
    val sincronizado: Boolean = false,
    val hashSeguranca: String = ""
) {
    companion object {
        fun criarNovo(cpf: String, tipo: String, lat: Double, lon: Double, obra: String): RegistroPonto {
            val dataFormatada = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR")).format(Date())
            val dadosBrutos = "$cpf|$tipo|$dataFormatada|$lat|$lon|$obra"
            val hash = dadosBrutos.hashCode().toString()

            return RegistroPonto(
                cpfFuncionario = cpf,
                tipo = tipo,
                dataHora = dataFormatada,
                latitude = lat,
                longitude = lon,
                nomeObra = obra,
                hashSeguranca = hash
            )
        }
    }
}
