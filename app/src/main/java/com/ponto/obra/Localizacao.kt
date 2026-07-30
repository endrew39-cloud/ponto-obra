package com.ponto.obra

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat

class Localizacao(private val contexto: Context) {
    private val gerenciador = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun pegarAtual(): Pair<Double, Double> {
        if (ActivityCompat.checkSelfPermission(
                contexto,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                contexto,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Pair(0.0, 0.0)
        }

        val localizacao: Location? = gerenciador.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        return if (localizacao != null) {
            Pair(localizacao.latitude, localizacao.longitude)
        } else {
            Pair(-22.9068, -43.1729) // Local padrão se não achar (ex: centro RJ)
        }
    }
}
