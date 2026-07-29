package com.ponto.obra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PrincipalActivity : AppCompatActivity() {

    private lateinit var sistemaAvisos: Notificacao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        sistemaAvisos = Notificacao(this)
    }
}
