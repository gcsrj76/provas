package com.papito.provas.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.papito.provas.data.DatabaseHelper
import com.papito.provas.model.Question

class ExamViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    
    // Objeto centralizado para acesso direto das telas
    val questoesCarregadas = mutableStateListOf<Question>()

    init {
        // Carrega os dados assim que o ViewModel é criado
        carregarDadosDoBanco()
    }

    fun carregarDadosDoBanco() {
        val db = dbHelper.readableDatabase
        try {
            val cursor = db.rawQuery(
                "SELECT id, pergunta, opcao_a, opcao_b, opcao_c, opcao_d, correta, texto_referencia, resposta_dada FROM questoes",
                null
            )
            questoesCarregadas.clear()

            while (cursor.moveToNext()) {
                questoesCarregadas.add(
                    Question(
                        id = cursor.getInt(0),
                        pergunta = cursor.getString(1),
                        opcaoA = cursor.getString(2),
                        opcaoB = cursor.getString(3),
                        opcaoC = cursor.getString(4),
                        opcaoD = cursor.getString(5),
                        correta = cursor.getString(6),
                        textoReferencia = cursor.getString(7),
                        respostaDada = cursor.getString(8)
                    )
                )
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetarBancoCompleto() {
        dbHelper.limparQuestoes()
        questoesCarregadas.clear()
    }

    fun limparApenasRespostas() {
        dbHelper.limparApenasRespostas()
        // Atualiza a lista em memória para refletir que não há mais respostas
        val listaAtualizada = questoesCarregadas.map { it.copy(respostaDada = null) }
        questoesCarregadas.clear()
        questoesCarregadas.addAll(listaAtualizada)
    }

    fun salvarRespostas(respostas: Map<Int, String>) {
        dbHelper.salvarTodasAsRespostas(respostas)
        // Opcional: recarregar do banco para garantir sincronia
        carregarDadosDoBanco()
    }
}