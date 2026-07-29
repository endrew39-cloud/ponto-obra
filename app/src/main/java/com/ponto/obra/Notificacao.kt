package com.ponto.obra

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class Notificacao(private val contexto: Context) {
    private val canalId = "ponto_obra_avisos"
    private val gerenciador = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Avisos Ponto Obra",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de local, horário e registro"
            }
            gerenciador.createNotificationChannel(canal)
        }
    }

    fun mostrar(titulo: String, mensagem: String) {
        val aviso = NotificationCompat.Builder(contexto, canalId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        gerenciador.notify(System.currentTimeMillis().toInt(), aviso)
    }
}
