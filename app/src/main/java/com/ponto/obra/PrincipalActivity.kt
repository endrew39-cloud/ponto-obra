package com.ponto.obra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputAutoCompleteTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrincipalActivity : AppCompatActivity() {
    private lateinit var config: ConfigSegura
    private lateinit var btnEntrada: MaterialButton
    private lateinit var btnSaida: MaterialButton
    private lateinit var btnCadastrarObra: MaterialButton
    private lateinit var listaObras: TextInputAutoCompleteTextView
    private val PERMISSAO_LOCAL = 202

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        supportActionBar?.title = "Ponto de Obra"

        config = ConfigSegura(this)
        inicializarTela()

        CoroutineScope(Dispatchers.IO).launch {
            config.atualizarListaObrasDoServidor()
            carregarObrasNaTela()
        }

        verificarPermissaoLocalizacao()
    }

    private fun inicializarTela() {
        btnEntrada = findViewById(R.id.btnEntrada)
        btnSaida = findViewById(R.id.btnSaida)
        btnCadastrarObra = findViewById(R.id.btnCadastrarObra)
        listaObras = findViewById(R.id.listaObras)

        val nomeUsuario = config.pegarValor("nome_usuario", "Funcionário")
        Toast.makeText(this, "👋 Olá $nomeUsuario!", Toast.LENGTH_SHORT).show()

        btnEntrada.setOnClickListener { registrarPonto("Entrada") }
        btnSaida.setOnClickListener { registrarPonto("Saída") }
        btnCadastrarObra.setOnClickListener {
            startActivity(Intent(this, CadastroObraMapaActivity::class.java))
        }
    }

    private suspend fun carregarObrasNaTela() {
        withContext(Dispatchers.Main) {
            val obras = config.carregarListaObras().map { it.nome }
            val adaptador = object : android.widget.ArrayAdapter<String>(
                this@PrincipalActivity,
                android.R.layout.simple_dropdown_item_1line,
                obras
            ) {}
            listaObras.setAdapter(adaptador)
        }
    }

    private fun verificarPermissaoLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSAO_LOCAL
            )
        }
    }

    private fun registrarPonto(tipo: String) {
        val obraEscolhida = listaObras.text.toString().trim()
        if(obraEscolhida.isEmpty()) {
            Toast.makeText(this, "Escolha uma obra primeiro!", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this, "✅ $tipo registrado para $obraEscolhida!", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            config.atualizarListaObrasDoServidor()
            carregarObrasNaTela()
        }
    }
}
