package com.ponto.obra

import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import java.util.concurrent.Executor

class LoginActivity : MainActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var edtCpf: TextInputEditText
    private lateinit var edtSenha: TextInputEditText
    private lateinit var config: ConfigSegura


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        try {
            config = ConfigSegura(this)
        val nomeEmpresa = config.pegarNomeEmpresa()
        findViewById<TextView>(R.id.txtEmpresa).text = nomeEmpresa
            edtCpf = findViewById(R.id.edtCpf)
            edtSenha = findViewById(R.id.edtSenha)
            val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
            val btnBiometria = findViewById<MaterialButton>(R.id.btnBiometria)

            executor = ContextCompat.getMainExecutor(this)
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(this@LoginActivity, "Autenticado com sucesso!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(this@LoginActivity, "Falha na autenticação", Toast.LENGTH_SHORT).show()
                    }
                })

            btnLogin.setOnClickListener {
                val cpf = edtCpf.text.toString().trim()
                val senha = edtSenha.text.toString().trim()

                if (cpf == "12345678900" && senha == "123456") {
                    Toast.makeText(this, "Login efetuado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, getString(R.string.erro_login), Toast.LENGTH_SHORT).show()
                }
            }

            btnBiometria.setOnClickListener {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verificação de identidade")
                    .setSubtitle("Use sua digital ou rosto para acessar")
                    .setNegativeButtonText("Cancelar")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao carregar tela", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
