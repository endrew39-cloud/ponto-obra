package com.ponto.obra

import android.Manifest
import android.content.Intent
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
import org.json.JSONObject
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
        if (ok) abrirOpcoesPonto()
        else Toast.makeText(this, "Permita localização para bater ponto", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_principal)

            sistemaAvisos = Notificacao(this)
            config = ConfigSegura(this)
            localizacao = Localizacao(this)
            servidor = ConexaoServidor(this)

            inicializarElementos()
            atualizarDataHora()
            configurarBotoes()
            verificarPermissoes()

        } catch (erro: Exception) {
            erro.printStackTrace()
            Toast.makeText(this, "Problema ao carregar: reinstale o app", Toast.LENGTH_LONG).show()
            finish()
        }
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
        btnBaterPonto.setOnClickListener { checarPermissoesEAbrir() }

        btnMeusRegistros.setOnClickListener {
            startActivity(Intent(this, RegistrosActivity::class.java))
        }

        btnSincronizar.setOnClickListener { sincronizarRegistros() }

        btnConfigDev.setOnClickListener {
            val entrada = TextInputEditText(this)
            entrada.hint = "Digite a senha mestra"

            AlertDialog.Builder(this)
                .setTitle("Acesso Restrito")
                .setMessage("Apenas para configurações do sistema")
                .setView(entrada)
                .setPositiveButton("Entrar") { _, _ ->
                    val senha = entrada.text.toString()
                    if (config.validarAcessoDev(senha)) abrirMenuConfiguracoes()
                    else Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun abrirMenuConfiguracoes() {
        val opcoes = arrayOf(
            "Alterar nome da empresa",
            "Configurar endereço do servidor",
            "Cadastrar nova obra",
            "Ver todas obras cadastradas"
        )

        AlertDialog.Builder(this)
            .setTitle("Configurações Avançadas")
            .setItems(opcoes) { _, indice ->
                try {
                    when(indice) {
                        0 -> alterarNomeEmpresa()
                        1 -> alterarServidor()
                        2 -> cadastrarObra()
                        3 -> mostrarObras()
                    }
                } catch (erro: Exception) {
                    Toast.makeText(this, "Erro ao abrir opção: ${erro.message}", Toast.LENGTH_LONG).show()
                }
            }
            .show()
    }

    private fun alterarNomeEmpresa() {
        val entrada = TextInputEditText(this)
        entrada.hint = "Digite o nome da empresa/obra"
        entrada.setText(config.pegarNomeEmpresa())

        AlertDialog.Builder(this)
            .setTitle("Alterar Nome")
            .setMessage("Esse nome aparece no registro do ponto")
            .setView(entrada)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = entrada.text.toString().trim()
                if (nome.isNotEmpty()) {
                    config.salvarNomeEmpresa(nome)
                    Toast.makeText(this, "Nome atualizado: $nome", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Digite um nome válido!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun alterarServidor() {
        val entrada = TextInputEditText(this)
        entrada.hint = "Ex: https://codigo.ngrok.io"
        entrada.setText(config.pegarServidor())

        AlertDialog.Builder(this)
            .setTitle("Endereço do Servidor")
            .setMessage("Mantenha o ngrok rodando no computador!")
            .setView(entrada)
            .setPositiveButton("Salvar") { _, _ ->
                val endereco = entrada.text.toString().trim()
                if (endereco.isNotEmpty()) {
                    config.salvarServidor(endereco)
                    Toast.makeText(this, "Servidor configurado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Digite o endereço correto!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cadastrarObra() {
        val entradaNome = TextInputEditText(this)
        entradaNome.hint = "Nome da Obra"

        val entradaLat = TextInputEditText(this)
        entradaLat.hint = "Latitude (ex: -22.927458)"

        val entradaLon = TextInputEditText(this)
        entradaLon.hint = "Longitude (ex: -43.645371)"

        val entradaRaio = TextInputEditText(this)
        entradaRaio.hint = "Raio em metros"
        entradaRaio.setText("100")

        val formulario = android.widget.LinearLayout(this)
        formulario.orientation = android.widget.LinearLayout.VERTICAL
        formulario.setPadding(48, 24, 48, 24)
        formulario.addView(entradaNome)
        formulario.addView(entradaLat)
        formulario.addView(entradaLon)
        formulario.addView(entradaRaio)

        AlertDialog.Builder(this)
            .setTitle("Cadastrar Nova Obra")
            .setView(formulario)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = entradaNome.text.toString().trim()
                val latTexto = entradaLat.text.toString().trim()
                val lonTexto = entradaLon.text.toString().trim()
                val raioTexto = entradaRaio.text.toString().trim()

                if (nome.isEmpty() || latTexto.isEmpty() || lonTexto.isEmpty() || raioTexto.isEmpty()) {
                    Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val lat = latTexto.toDoubleOrNull()
                val lon = lonTexto.toDoubleOrNull()
                val raio = raioTexto.toIntOrNull() ?: 100

                if (lat == null || lon == null) {
                    Toast.makeText(this, "Digite latitude e longitude válidas!", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val obra = Obra(nome, lat, lon, raio)
                config.salvarObra(obra)
                Toast.makeText(this, "Obra cadastrada com sucesso!", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarObras() {
        val lista = config.pegarTodasObras()
        
        if (lista.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Obras Cadastradas")
                .setMessage("Nenhuma obra cadastrada ainda!\nCadastre uma primeiro para validar o ponto.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val texto = lista.mapIndexed { indice, obra ->
            "${indice + 1}. ${obra.nome}\n   Latitude: ${obra.latitude}\n   Longitude: ${obra.longitude}\n   Raio permitido: ${obra.raioPermitidoMetros} metros"
        }.joinToString("\n\n")

        AlertDialog.Builder(this)
            .setTitle("Obras Cadastradas")
            .setMessage(texto)
            .setPositiveButton("OK", null)
            .show()
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
            val listaObras = config.pegarTodasObras()
            val obraAtual = listaObras.firstOrNull()
            val cpfUsuario = "12345678900"

            val podeRegistrar = obraAtual?.let {
                ValidacaoLocal.estaDentroDaObra(local.first, local.second, it)
            } ?: true

            withContext(Dispatchers.Main) {
                if (!podeRegistrar) {
                    Toast.makeText(this@PrincipalActivity,
                        "Você não está dentro do raio permitido da obra!",
                        Toast.LENGTH_LONG).show()
                    return@withContext
                }

                val registro = RegistroPonto.criarNovo(
                    cpf = cpfUsuario,
                    tipo = tipo,
                    lat = local.first,
                    lon = local.second,
                    obra = nomeEmpresa
                )

                val jsonPonto = JSONObject(registro.paraMapa())
                val enviado = servidor.enviarPonto(jsonPonto)

                if (enviado) {
                    sistemaAvisos.mostrar("Ponto Registrado ✅", "Seu $tipo foi enviado ao servidor!")
                    Toast.makeText(this@PrincipalActivity, "Ponto registrado com sucesso!", Toast.LENGTH_LONG).show()
                } else {
                    SalvarOffline.salvar(this@PrincipalActivity, registro)
                    sistemaAvisos.mostrar("Sem conexão ⚠️", "Ponto salvo seguro no aparelho!")
                    Toast.makeText(this@PrincipalActivity, "Salvo offline, sincroniza depois!", Toast.LENGTH_LONG).show()
                }
                atualizarDataHora()
            }
        }
    }

    private fun sincronizarRegistros() {
        CoroutineScope(Dispatchers.IO).launch {
            val listaSalvos = SalvarOffline.pegarTodos(this@PrincipalActivity)
            val listaJson = listaSalvos.map { JSONObject(it.paraMapa()) }
            val qtd = servidor.sincronizarLista(listaJson)
            
            if(qtd > 0) SalvarOffline.limparTodos(this@PrincipalActivity)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@PrincipalActivity, "$qtd registros sincronizados!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
