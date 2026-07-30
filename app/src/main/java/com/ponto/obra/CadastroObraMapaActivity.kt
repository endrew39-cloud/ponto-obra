package com.ponto.obra

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CadastroObraMapaActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var config: ConfigSegura
    private lateinit var mapaObra: GoogleMap
    private lateinit var txtNomeObra: TextInputEditText
    private lateinit var txtRaioObra: TextInputEditText
    private lateinit var btnSalvarObraMapa: MaterialButton
    private var localEscolhido: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_obra_mapa)
        supportActionBar?.title = "Cadastrar Obra"

        config = ConfigSegura(this)
        val mapaFragment = supportFragmentManager.findFragmentById(R.id.mapaObra) as SupportMapFragment
        mapaFragment.getMapAsync(this)

        txtNomeObra = findViewById(R.id.txtNomeObra)
        txtRaioObra = findViewById(R.id.txtRaioObra)
        btnSalvarObraMapa = findViewById(R.id.btnSalvarObraMapa)

        btnSalvarObraMapa.setOnClickListener { salvarObra() }
    }

    override fun onMapReady(mapa: GoogleMap) {
        mapaObra = mapa
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        mapa.isMyLocationEnabled = true
        mapa.setOnMapClickListener { ponto ->
            mapa.clear()
            mapa.addMarker(MarkerOptions().position(ponto).title("Local da Obra"))
            mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(ponto, 15f))
            localEscolhido = ponto
        }
    }

    private fun salvarObra() {
        val nome = txtNomeObra.text.toString().trim()
        val raio = txtRaioObra.text.toString().trim()

        if(nome.isEmpty() || raio.isEmpty() || localEscolhido == null) {
            Toast.makeText(this, "Preencha todos os dados e escolha o local no mapa!", Toast.LENGTH_LONG).show()
            return
        }

        val qtdAtual = config.pegarValor("qtd_obras", "0").toInt()
        config.salvarValor("obra_${qtdAtual}_nome", nome)
        config.salvarValor("obra_${qtdAtual}_lat", localEscolhido!!.latitude.toString())
        config.salvarValor("obra_${qtdAtual}_lon", localEscolhido!!.longitude.toString())
        config.salvarValor("obra_${qtdAtual}_raio", raio)
        config.salvarValor("qtd_obras", (qtdAtual + 1).toString())

        Toast.makeText(this, "✅ Obra cadastrada com sucesso!", Toast.LENGTH_LONG).show()
        finish()
    }
}
