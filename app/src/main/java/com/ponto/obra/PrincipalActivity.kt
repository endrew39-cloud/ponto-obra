package com.ponto.obra

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrincipalActivity : AppCompatActivity() {
    private lateinit var sistemaAvisos: Notificacao
    private lateinit var config: ConfigSegura
    private lateinit var txtBemVindo: TextView
    private lateinit var txtDataHora: TextView
    private lateinit var btnBaterPonto: MaterialButton
    private lateinit var btnMeusRegistros: MaterialButton
    private lateinit var btnConfigDev: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        sistemaAvisos = Notificacao(this)
        config = ConfigSegura(this)

        inicializarElementos()
        atualizarDataHora()
        configurarBotoes()
    }

    private fun inicializarElementos() {
        txtBemVindo = findViewById(R.id.txtBemVindo)
        txtDataHora = findViewById(R.id.txtDataHora)
        btnBaterPonto = findViewById(R.id.btnBaterPonto)
        btnMeusRegistros = findViewById(R.id.btnMeusRegistros)
        btnConfigDev = findViewById(R.id.btnConfigDev)
    }

    private fun atualizarDataHora() {
        val formato = SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale("pt", "BR"))
        val dataAtual = Date()
        txtDataHora.text = formato.format(dataAtual)
    }

    private fun configurarBotoes() {
        btnBaterPonto.setOnClickListener {
            sistemaAvisos.mostrar("Ponto", "Função de bater ponto em desenvolvimento!")
            Toast.makeText(this, "Em breve: Bater Ponto", Toast.LENGTH_SHORT).show()
        }

        btnMeusRegistros.setOnClickListener {
            Toast.makeText(this, "Em breve: Meus Registros", Toast.LENGTH_SHORT).show()
        }

        btnConfigDev.setOnClickListener {
            val senhaDigitada = android.widget.EditText(this)
            senhaDigitada.hint = "Digite a senha mestra"

            val caixa = android.app.AlertDialog.Builder(this)
                .setTitle("Acesso Restrito")
                .setMessage("Área apenas para desenvolvedores/administradores")
                .setView(senhaDigitada)
                .setPositiveButton("Entrar") { _, _ ->
                    val senha = senhaDigitada.text.toString()
                    if (config.validarAcessoDev(senha)) {
                        Toast.makeText(this, "Acesso liberado!", Toast.LENGTH_SHORT).show()
                        abrirTelaConfiguracoes()
                    } else {
                        Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .create()

            caixa.show()
        }
    }

    private fun abrirTelaConfiguracoes() {
        Toast.makeText(this, "Tela de configurações em construção", Toast.LENGTH_SHORT).show()
    }
}
