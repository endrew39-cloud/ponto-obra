package com.ponto.obra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var btnEntrar: MaterialButton
    private lateinit var txtCpf: TextInputEditText
    private val CODIGO_PERMISSOES = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title = "Acesso ao Sistema"

        btnEntrar = findViewById(R.id.btnEntrar)
        txtCpf = findViewById(R.id.txtCpf)
        executor = ContextCompat.getMainExecutor(this)

        val biometric = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(r)
                    verificarEProsseguir()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "Biometria não reconhecida", Toast.LENGTH_SHORT).show()
                }
            })

        pedirPermissoes()

        btnEntrar.setOnClickListener { verificarEProsseguir() }
    }

    private fun pedirPermissoes() {
        val listaPermissoes = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            listaPermissoes.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            listaPermissoes.add(Manifest.permission.INTERNET)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            listaPermissoes.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }

        if (listaPermissoes.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listaPermissoes.toTypedArray(),
                CODIGO_PERMISSOES
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODIGO_PERMISSOES) {
            var todasAceitas = true
            for (resultado in grantResults) {
                if (resultado != PackageManager.PERMISSION_GRANTED) {
                    todasAceitas = false
                    break
                }
            }
            if (!todasAceitas) {
                Toast.makeText(this, "Precisa permitir GPS e Internet para usar o app!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permissões concedidas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarEProsseguir() {
        val temGps = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val temInternet = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED

        if (!temGps || !temInternet) {
            Toast.makeText(this, "Primeiro permita GPS e Internet!", Toast.LENGTH_LONG).show()
            pedirPermissoes()
            return
        }

        val cpf = txtCpf.text.toString().trim()
        if(cpf.isEmpty()) {
            Toast.makeText(this, "Digite o CPF!", Toast.LENGTH_LONG).show()
            return
        }
        val cfg = ConfigSegura(this)
        cfg.salvarValor("cpf_logado", cpf)
        cfg.salvarValor("nome_usuario", "Funcionário Teste")
        cfg.salvarValor("funcao_usuario", "Geral")
        startActivity(Intent(this, PrincipalActivity::class.java))
        finish()
    }
}
