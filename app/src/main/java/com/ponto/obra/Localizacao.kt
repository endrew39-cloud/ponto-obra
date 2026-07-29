package com.ponto.obra

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import kotlin.math.sqrt

class Localizacao(private val activity: MainActivity) {

    private val locationManager = activity.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager

    fun verificarSeDentroDaObra(latObra: Double, longObra: Double, raioPermitido: Int = 100): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: return false

        val diferencaLat = Math.toRadians(ultimaLocalizacao.latitude - latObra)
        val diferencaLong = Math.toRadians(ultimaLocalizacao.longitude - longObra)

        val a = Math.sin(diferencaLat / 2) * Math.sin(diferencaLat / 2) +
                Math.cos(Math.toRadians(latObra)) * Math.cos(Math.toRadians(ultimaLocalizacao.latitude)) *
                Math.sin(diferencaLong / 2) * Math.sin(diferencaLong / 2)

        val distanciaMetros = 6371000 * 2 * Math.atan2(sqrt(a), sqrt(1 - a))

        return distanciaMetros <= raioPermitido
    }
}
