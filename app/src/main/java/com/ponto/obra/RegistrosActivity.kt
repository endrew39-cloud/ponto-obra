package com.ponto.obra

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale

class RegistrosActivity : AppCompatActivity() {

    private lateinit var listaRegistros: LinearLayout
    private lateinit var txtVazio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registros)

        inicializarTela()
        carregarRegistros()
    }

    private fun inicializarTela() {
        supportActionBar?.setTitle("Meus Registros")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listaRegistros = findViewById(R.id.listaRegistros)
        txtVazio = findViewById(R.id.txtVazio)
    }

    private fun carregarRegistros() {
        val lista = SalvarOffline.pegarTodos(this)

        if(lista.isEmpty()) {
            txtVazio.visibility = android.view.View.VISIBLE
            listaRegistros.visibility = android.view.View.GONE
            return
        }

        txtVazio.visibility = android.view.View.GONE
        listaRegistros.visibility = android.view.View.VISIBLE
        listaRegistros.removeAllViews()

        val formatoData = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))

        lista.sortedByDescending { it.dataHora }.forEach { registro ->
            val cartao = criarCartaoRegistro(registro, formatoData)
            listaRegistros.addView(cartao)
        }
    }

    private fun criarCartaoRegistro(registro: RegistroPonto, formato: SimpleDateFormat): android.widget.LinearLayout {
        val cartao = android.widget.LinearLayout(this)
        cartao.orientation = android.widget.LinearLayout.VERTICAL
        cartao.setPadding(48, 32, 48, 32)
        cartao.setBackgroundColor(0xFFF5F5F5.toInt())
        val margem = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        margem.setMargins(0, 0, 0, 24)
        cartao.layoutParams = margem

        val txtTipo = TextView(this)
        txtTipo.text = registro.tipo
        txtTipo.textSize = 18f
        txtTipo.setTextColor(if(registro.enviado) 0xFF2E7D32.toInt() else 0xFFEF6C00.toInt())
        txtTipo.setTypeface(null, android.graphics.Typeface.BOLD)
        cartao.addView(txtTipo)

        val txtData = TextView(this)
        txtData.text = formato.format(registro.dataHora)
        txtData.textSize = 14f
        txtData.setTextColor(0xFF424242.toInt())
        txtData.setPadding(0, 12, 0, 0)
        cartao.addView(txtData)

        val txtObra = TextView(this)
        txtObra.text = "Obra: ${registro.nomeObra}"
        txtObra.textSize = 14f
        txtObra.setTextColor(0xFF424242.toInt())
        txtObra.setPadding(0, 4, 0, 0)
        cartao.addView(txtObra)

        val txtStatus = TextView(this)
        txtStatus.text = if(registro.enviado) "✅ Enviado ao servidor" else "📂 Salvo offline"
        txtStatus.textSize = 13f
        txtStatus.setTextColor(if(registro.enviado) 0xFF2E7D32.toInt() else 0xFFEF6C00.toInt())
        txtStatus.setPadding(0, 8, 0, 0)
        cartao.addView(txtStatus)

        cartao.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Detalhes do Registro")
                .setMessage("""
                    Tipo: ${registro.tipo}
                    Data/Hora: ${formato.format(registro.dataHora)}
                    Obra: ${registro.nomeObra}
                    Latitude: ${registro.lat}
                    Longitude: ${registro.lon}
                    Status: ${if(registro.enviado) "Enviado" else "Aguardando sincronização"}
                """.trimIndent())
                .setPositiveButton("Fechar", null)
                .show()
        }

        return cartao
    }

    override fun onResume() {
        super.onResume()
        carregarRegistros()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
