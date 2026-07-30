package com.ponto.obra

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrincipalActivity : AppCompatActivity() {
    private lateinit var sistemaAvisos: Notificacao
    private lateinit var config: ConfigSegura
    private lateinit var localizacao: Localizacao
    private lateinit var servidor: ConexaoServidor
    private lateinit var txtBemVindo: android.widget.TextView
    private lateinit var txtDataHora: android.widget.TextView
    private lateinit var btnBaterPonto: MaterialButton
    private lateinit var btnMeusRegistros: MaterialButton
    private lateinit var btnSincronizar: MaterialButton
    private lateinit var btnConfigDev: MaterialButton

    private val permissaoLocalizacao = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val ok = resultado.all { it.value }
        if (ok) {
            abrirOpcoesPonto()
        } else {
            Toast.makeText(this, "Permita localização para bater ponto", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        sistemaAvisos = Notificacao(this)
        config = ConfigSegura(this)
        localizacao = Localizacao(this)
        servidor = ConexaoServidor(this)

        inicializarElementos()
        atualizarDataHora()
        configurarBotoes()
        verificarPermissoes()
    }

    private fun inicializarElementos() {
        txtBemVindo = findViewById(R.id.txtBemVindo)
        txtDataHora = findViewById(R.id.txtDataHora)
        btnBaterPonto = findViewById(R.id.btnBaterPonto)
        btnMeusRegistros = findViewById(R.id.btnMeusRegistros)
        btnSincronizar = findViewById(R.id.btnSincronizar)
        btnConfigDev = findViewById(R.id.btnConfigDev)

        txtBemVindo.text = "Olá!"
    }

    private fun atualizarDataHora() {
        val formato = SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale("pt", "BR"))
        txtDataHora.text = formato.format(Date())
    }

    private fun configurarBotoes() {
        btnBaterPonto.setOnClickListener {
            checarPermissoesEAbrir()
        }

        btnMeusRegistros.setOnClickListener {
            Toast.makeText(this, "Em breve: Lista de registros", Toast.LENGTH_SHORT).show()
        }

        btnSincronizar.setOnClickListener {
            sincronizarRegistros()
        }

        btnConfigDev.setOnClickListener {
            val entrada = TextInputEditText(this)
            entrada.hint = "Senha Mestra"

            AlertDialog.Builder(this)
                .setTitle("Acesso Restrito")
                .setMessage("Apenas para configurações do sistema")
                .setView(entrada)
                .setPositiveButton("Entrar") { _, _ ->
                    val senha = entrada.text.toString()
                    if (config.validarAcessoDev(senha)) {
                        abrirTelaConfiguracoes()
                    } else {
                        Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun verificarPermissoes() {
        val precisa = mutableListOf<String>()
        precisa.add(Manifest.permission.ACCESS_FINE_LOCATION)
        precisa.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            precisa.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissaoLocalizacao.launch(precisa.toTypedArray())
    }

    private fun checarPermissoesEAbrir() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            abrirOpcoesPonto()
        } else {
            verificarPermissoes()
        }
    }

    private fun abrirOpcoesPonto() {
        val tipos = arrayOf("Entrada", "Saída para Almoço", "Volta do Almoço", "Saída")

        AlertDialog.Builder(this)
            .setTitle("Tipo de Registro")
            .setItems(tipos) { _, indice ->
                registrarPonto(tipos[indice])
            }
            .show()
    }

    private fun registrarPonto(tipo: String) {
        Toast.makeText(this, "Buscando localização...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            val local = localizacao.pegarAtual()
            val nomeEmpresa = config.pegarNomeEmpresa()
            val cpfUsuario = "12345678900" // Aqui depois pega do login salvo

            val registro = RegistroPonto.criarNovo(
                cpf = cpfUsuario,
                tipo = tipo,
                lat = local.first,
                lon = local.second,
                obra = nomeEmpresa
            )

            val enviado = servidor.enviarRegistro(registro)

            withContext(Dispatchers.Main) {
                if (enviado) {
                    sistemaAvisos.mostrar("Ponto Registrado ✅", "Seu $tipo foi enviado ao servidor!")
                    Toast.makeText(this@PrincipalActivity, "Ponto registrado com sucesso!", Toast.LENGTH_LONG).show()
                } else {
                    sistemaAvisos.mostrar("Sem conexão ⚠️", "Ponto salvo seguro no aparelho, sincroniza depois!")
                    Toast.makeText(this@PrincipalActivity, "Salvo offline, sincroniza quando voltar internet", Toast.LENGTH_LONG).show()
                }
                atualizarDataHora()
            }
        }
    }

    private fun sincronizarRegistros() {
        CoroutineScope(Dispatchers.IO).launch {
            val qtd = servidor.sincronizarPendentes()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@PrincipalActivity, "$qtd registros sincronizados!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirTelaConfiguracoes() {
        val entradaNome = TextInputEditText(this)
        entradaNome.setText(config.pegarNomeEmpresa())
        entradaNome.hint = "Nome da Empresa/Obra"

        AlertDialog.Builder(this)
            .setTitle("Configuração da Empresa")
            .setView(entradaNome)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = entradaNome.text.toString().trim()
                if (nome.isNotEmpty()) {
                    config.salvarNomeEmpresa(nome)
                    Toast.makeText(this, "Nome atualizado! Volte para o login para ver a alteração", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
