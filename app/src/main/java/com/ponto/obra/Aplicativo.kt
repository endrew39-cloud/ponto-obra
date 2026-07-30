package com.ponto.obra

import android.app.Application
import android.content.Context

class Aplicativo : Application() {
    companion object {
        private lateinit var contexto: Context

        fun getContexto(): Context = contexto
    }

    override fun onCreate() {
        super.onCreate()
        contexto = applicationContext
    }
}
