package com.ponto.obra

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var btnEntrar: MaterialButton
    private lateinit var txtCpf: TextInputEditText

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
                    logar()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "Biometria não reconhecida", Toast.LENGTH_SHORT).show()
                }
            })

        btnEntrar.setOnClickListener { logar() }
    }

    private fun logar() {
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
