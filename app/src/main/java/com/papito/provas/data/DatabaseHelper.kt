package com.papito.provas.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.net.Uri

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "simulador.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuestion = """
        CREATE TABLE questions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            statement TEXT NOT NULL,
            reference_text TEXT,
            given_answer_id INTEGER,
            sort_order INTEGER  
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

    fun backupDatabase(context: Context, outUri: Uri): Boolean {
        val db = this.writableDatabase

        return try {
            db.rawQuery("PRAGMA wal_checkpoint(FULL);", null).use { it.moveToFirst() }
            this.close()

            val dbName = "simulador.db"
            val dbFile = context.getDatabasePath(dbName)

            if (dbFile.exists() && dbFile.length() > 0) {
                context.contentResolver.openOutputStream(outUri)?.use { output ->
                    dbFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                android.util.Log.e("BACKUP_ERROR", "Arquivo não encontrado ou vazio em: ${dbFile.absolutePath}")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("BACKUP_ERROR", "Falha catastrófica: ${e.message}")
            false
        }
    }

    fun restoreDatabase(context: Context, inUri: Uri): Boolean {
        return try {
            this.close() // Crucial: fecha o banco antes de sobrescrever o arquivo

            val dbFile = context.getDatabasePath("simulador.db")

            context.contentResolver.openInputStream(inUri)?.use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shuffleDatabasePhysically() {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // 1. EMBARALHAR QUESTÕES
            val questionIds = mutableListOf<Int>()
            db.rawQuery("SELECT id FROM questions", null).use { cursor ->
                while (cursor.moveToNext()) questionIds.add(cursor.getInt(0))
            }

            questionIds.shuffled().forEachIndexed { index, id ->
                val values = ContentValues().apply { put("sort_order", index) }
                db.update("questions", values, "id = ?", arrayOf(id.toString()))
            }

            // 2. EMBARALHAR RESPOSTAS (dentro de cada questão)
            questionIds.forEach { qId ->
                val answerIds = mutableListOf<Int>()
                db.rawQuery("SELECT id FROM answers WHERE question_id = ?", arrayOf(qId.toString())).use { cursor ->
                    while (cursor.moveToNext()) answerIds.add(cursor.getInt(0))
                }

                answerIds.shuffled().forEachIndexed { index, id ->
                    val values = ContentValues().apply { put("sort_order", index) }
                    db.update("answers", values, "id = ?", arrayOf(id.toString()))
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}