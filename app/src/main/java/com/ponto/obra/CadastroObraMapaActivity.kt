package com.seuapp.pontoobra

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.HttpURLConnection
import java.net.URL

class CadastroObraMapaActivity : AppCompatActivity() {
    private lateinit var mapa: MapView
    private lateinit var txtNomeObra: TextInputEditText
    private lateinit var txtRaio: TextInputEditText
    private lateinit var btnSalvarObra: MaterialButton
    private lateinit var config: ConfigSegura
    private var marcadorEscolhido: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_obra_mapa)
        supportActionBar?.title = "Cadastrar Obra no Mapa"

        config = ConfigSegura(this)
        inicializarComponentes()
        verificarPermissaoLocalizacao()
    }

    private fun inicializarComponentes() {
        mapa = findViewById(R.id.mapaObra)
        txtNomeObra = findViewById(R.id.txtNomeObra)
        txtRaio = findViewById(R.id.txtRaioObra)
        btnSalvarObra = findViewById(R.id.btnSalvarObraMapa)

        txtRaio.setText("100")
        mapa.setMultiTouchControls(true)
        mapa.controller?.setZoom(15.0)
        mapa.controller?.setCenter(GeoPoint(-22.9068, -43.1729))

        mapa.setOnMapClickListener { _, ponto ->
            adicionarOuMoverMarcador(ponto)
        }

        btnSalvarObra.setOnClickListener { enviarParaServidor() }
    }

    private fun adicionarOuMoverMarcador(ponto: GeoPoint) {
        mapa.overlays.remove(marcadorEscolhido)
        marcadorEscolhido = Marker(mapa).apply {
            position = ponto
            title = "Local da Obra"
            isDraggable = true
        }
        mapa.overlays.add(marcadorEscolhido)
        mapa.invalidate()
    }

    private fun verificarPermissaoLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            val localizacao = com.google.android.gms.location.LocationServices
                .getFusedLocationProviderClient(this)
            localizacao.lastLocation.addOnSuccessListener { loc ->
                if(loc != null) {
                    val meuPonto = GeoPoint(loc.latitude, loc.longitude)
                    mapa.controller?.setCenter(meuPonto)
                    adicionarOuMoverMarcador(meuPonto)
                }
            }
        }
    }

    private fun enviarParaServidor() {
        val nome = txtNomeObra.text.toString().trim()
        val raio = txtRaio.text.toString().trim().toIntOrNull() ?: 100
        val ponto = marcadorEscolhido?.position

        if(nome.isEmpty()) {
            Toast.makeText(this, "Digite o nome da obra!", Toast.LENGTH_LONG).show()
            return
        }
        if(ponto == null) {
            Toast.makeText(this, "Clique no mapa para marcar o local!", Toast.LENGTH_LONG).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val end = config.pegarValor("link_servidor", "").trim().removeSuffix("/")
                val url = URL("$end/admin/salvar-obra")
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "POST"
                con.setRequestProperty("Content-Type", "application/json")
                con.setRequestProperty("ngrok-skip-browser-warning", "pontoobra")
                con.doOutput = true

                val dados = JSONObject().apply {
                    put("nome", nome)
                    put("latitude", ponto.latitude)
                    put("longitude", ponto.longitude)
                    put("raio", raio)
                }

                con.outputStream.write(dados.toString().toByteArray())
                val resposta = con.responseCode

                withContext(Dispatchers.Main) {
                    if(resposta in 200..299) {
                        Toast.makeText(this@CadastroObraMapaActivity, 
                            "✅ Obra cadastrada com sucesso!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                    Toast.makeText(this@CadastroObraMapaActivity, 
                            "❌ Erro ao cadastrar obra!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CadastroObraMapaActivity, 
                        "❌ Sem conexão com o servidor!", Toast.LENGTH_LONG).show()
                }
            }
    }
}
