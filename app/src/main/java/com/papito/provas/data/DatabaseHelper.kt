package com.papito.provas.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.widget.Toast
import com.papito.provas.model.Question
import java.io.File

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "simulador.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuestion = """
        CREATE TABLE questions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            statement TEXT NOT NULL,
            reference_text TEXT,
            given_answer_id INTEGER -- Armazena o ID da tabela 'answers'    
    )
        """.trimIndent()
        db.execSQL(createTableQuestion)

        val createTableAnswer = """
        CREATE TABLE answers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            question_id INTEGER NOT NULL,
            text TEXT NOT NULL,
            is_correct INTEGER NOT NULL,
            sort_order INTEGER NOT NULL,
            FOREIGN KEY(question_id) REFERENCES questions(id) ON DELETE CASCADE
        )
        """.trimIndent()
        db.execSQL(createTableAnswer)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS answers")
        db.execSQL("DROP TABLE IF EXISTS questions")
        onCreate(db)
    }

    fun insertFullQuestion(statement: String, refText: String?, answerList: List<Pair<String, Boolean>>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val qValues = ContentValues().apply {
                put("statement", statement)
                put("reference_text", refText)
            }
            val questionId = db.insert("questions", null, qValues)

            answerList.forEachIndexed { index, pair ->
                val aValues = ContentValues().apply {
                    put("question_id", questionId)
                    put("text", pair.first)
                    put("is_correct", if (pair.second) 1 else 0)
                    put("sort_order", index)
                }
                db.insert("answers", null, aValues)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Método para salvar a escolha do usuário
    fun saveUserAnswer(questionId: Int, answerId: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("given_answer_id", answerId)
        }
        db.update("questions", values, "id = ?", arrayOf(questionId.toString()))
    }
    // Limpa apenas as respostas dadas (reinicia o simulado)
    fun clearUserAnswers() {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            putNull("given_answer_id")
        }
        db.update("questions", values, null, null)
    }

    // Limpa todo o banco de dados (todas as questões e respostas)
    fun clearDatabase() {
        val db = this.writableDatabase
        db.beginTransaction() // Inicia transação para segurança
        try {
            // 1. Remove os dados das tabelas (Nomes padronizados em inglês)
            // O DELETE em 'questions' removerá 'answers' se o CASCADE estiver ativo,
            // mas executamos em ambos para garantir limpeza total.
            db.execSQL("DELETE FROM answers")
            db.execSQL("DELETE FROM questions")

            // 2. Reseta os contadores de ID do SQLite
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='questions'")
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='answers'")

            db.setTransactionSuccessful() // Confirma as alterações
        } catch (e: Exception) {
            e.printStackTrace()
            // Aqui você poderia registrar o erro em um log ou avisar a UI
        } finally {
            db.endTransaction() // Finaliza a transação
        }
    }

    /** Sera substituido por uma funçao de criaçao/restauraçao de backup
    fun importarQuestoesDB(uri: android.net.Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "temp_questoes.db")

            inputStream?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }

            val externalDatabase = SQLiteDatabase.openDatabase(
                tempFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            val cursor = externalDatabase.rawQuery(
                "SELECT id, pergunta, opcao_a, opcao_b, opcao_c, opcao_d, correta, texto_referencia FROM questoes", 
                null
            )

            val dbInterno = this.writableDatabase

            while (cursor.moveToNext()) {
                val qBase = Question(
                    id = 0,
                    pergunta = cursor.getString(1),
                    opcaoA = cursor.getString(2),
                    opcaoB = cursor.getString(3),
                    opcaoC = cursor.getString(4),
                    opcaoD = cursor.getString(5),
                    correta = cursor.getString(6),
                    textoReferencia = cursor.getString(7)
                )
                inserirQuestao(dbInterno, qBase)
            }

            cursor.close()
            externalDatabase.close()
            dbInterno.close()

            Toast.makeText(context, "Banco de dados importado!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro no DB: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    */
}