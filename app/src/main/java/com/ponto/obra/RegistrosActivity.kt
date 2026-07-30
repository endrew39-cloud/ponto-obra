package com.ponto.obra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RegistrosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registros)

        val lista = findViewById<RecyclerView>(R.id.listaRegistros)
        lista.layoutManager = LinearLayoutManager(this)

        val todosOsRegistros = ConexaoServidor(this).carregarOffline()
        lista.adapter = AdaptadorRegistro(todosOsRegistros)
    }

    inner class AdaptadorRegistro(private val itens: List<RegistroPonto>) :
        RecyclerView.Adapter<AdaptadorRegistro.VisaoHolder>() {

        inner class VisaoHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textoTipo: TextView = view.findViewById(android.R.id.text1)
            val textoData: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(pai: ViewGroup, tipo: Int): VisaoHolder {
            val visual = LayoutInflater.from(pai.context)
                .inflate(android.R.layout.simple_list_item_2, pai, false)
            return VisaoHolder(visual)
        }

        override fun onBindViewHolder(holder: VisaoHolder, posicao: Int) {
            val registro = itens[posicao]
            holder.textoTipo.text = registro.tipo
            holder.textoData.text = "${registro.dataHora} - ${if(registro.sincronizado) "✅ Enviado" else "⚠️ Pendente"}"
        }

        override fun getItemCount(): Int = itens.size
    }
}
