package com.papito.provas.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papito.provas.data.DatabaseHelper
import com.papito.provas.model.Question
import com.papito.provas.model.Answer
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ExamViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    
    // Objeto centralizado para acesso direto das telas
    val questoesCarregadas = mutableStateListOf<Question>()

    var segundosDecorridos by mutableIntStateOf(0)
        private set

    private var timerJob: Job? = null

    var isLoadingGemini by mutableStateOf(false)

    init {
        // Carrega os dados assim que o ViewModel é criado
        loadQuestionsFromDatabase()
    }

    fun loadQuestionsFromDatabase() {
        val db = dbHelper.readableDatabase
        val newQuestionsList = mutableListOf<Question>()

        val cursor = db.rawQuery("SELECT * FROM questions ORDER BY sort_order ASC", null)

        while (cursor.moveToNext()) {
            val qId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))

            // BUSCA AS RESPOSTAS DESTA QUESTÃO ESPECÍFICA
            val ansCursor = db.rawQuery(
                "SELECT * FROM answers WHERE question_id = ? ORDER BY sort_order ASC",
                arrayOf(qId.toString())
            )

            val answerList = mutableListOf<Answer>()
            while (ansCursor.moveToNext()) {
                answerList.add(Answer(
                    id = ansCursor.getInt(ansCursor.getColumnIndexOrThrow("id")),
                    questionId = qId,
                    text = ansCursor.getString(ansCursor.getColumnIndexOrThrow("text")),
                    isCorrect = ansCursor.getInt(ansCursor.getColumnIndexOrThrow("is_correct")) == 1,
                    sortOrder = ansCursor.getInt(ansCursor.getColumnIndexOrThrow("sort_order"))
                ))
            }
            ansCursor.close()

            // MONTA O OBJETO COMPLETO
            newQuestionsList.add(Question(
                id = qId,
                statement = cursor.getString(cursor.getColumnIndexOrThrow("statement")),
                answers = answerList, // Lista populada acima
                referenceText = cursor.getString(cursor.getColumnIndexOrThrow("reference_text")),
                tip = cursor.getString(cursor.getColumnIndexOrThrow("tip")),
                givenAnswerId = if (cursor.isNull(cursor.getColumnIndexOrThrow("given_answer_id"))) null
                else cursor.getInt(cursor.getColumnIndexOrThrow("given_answer_id"))
            ))
        }
        cursor.close()

        // Atualiza a SnapshotStateList da UI
        questoesCarregadas.clear()
        questoesCarregadas.addAll(newQuestionsList)
    }

    fun updateQuestion(questionId: Int, newStatement: String, newAnswerUpdates: List<Triple<Int, String, Boolean>>) {
        viewModelScope.launch {
            try {
                dbHelper.updateQuestionContent(questionId, newStatement, newAnswerUpdates)
                // Recarrega a lista para refletir as mudanças imediatamente na UI
                loadQuestionsFromDatabase()
            } catch (e: Exception) {
                // Erro tratado silenciosamente
            }
        }
    }

    fun selectAnswer(questionId: Int, answerId: Int) {
        // 1. Salva no banco de dados imediatamente
        dbHelper.saveUserAnswer(questionId, answerId)

        // 2. Sincroniza a memória com o banco
        // Isso atualiza a lista 'questoesCarregadas' e reflete na UI
        loadQuestionsFromDatabase()
    }

    fun resetarBancoCompleto() {
        dbHelper.clearDatabase()
        questoesCarregadas.clear()
    }

    fun limparApenasRespostas() {
        dbHelper.clearUserAnswers()
        loadQuestionsFromDatabase()
    }

    fun restoreBackup(uri: Uri, context: Context) {
        if (dbHelper.restoreDatabase(context, uri)) {
            // Após restaurar o arquivo físico, recarregamos a lista em memória
            loadQuestionsFromDatabase()
        }
    }

    fun createBackup(uri: Uri, context: Context) {
        dbHelper.backupDatabase(context, uri)
    }

    fun shuffle(context: Context) {
        dbHelper.shuffleDatabasePhysically()
        loadQuestionsFromDatabase() // Recarrega a UI com a nova ordem vinda do DB
        Toast.makeText(context, "Questões embaralhadas com sucesso!", Toast.LENGTH_SHORT).show()
    }

    // Funções do Temporizador (Coroutines)
    fun iniciarTimer() {
        timerJob?.cancel()
        segundosDecorridos = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                segundosDecorridos++
            }
        }
    }

    fun pararTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun formatarTempo(): String {
        val minutos = segundosDecorridos / 60
        val segundos = segundosDecorridos % 60
        return "%02d:%02d".format(minutos, segundos)
    }
}