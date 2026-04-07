package com.papito.provas.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.net.Uri

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "simulador.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuestion = """
        CREATE TABLE questions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            statement TEXT NOT NULL,
            reference_text TEXT,
            tip TEXT,
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

    // NOVA FUNCIONALIDADE: Atualiza o conteúdo de uma questão e suas respostas
    fun updateQuestionContent(questionId: Int, newStatement: String, newAnswerTexts: List<Pair<Int, String>>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // Atualiza enunciado
            val qValues = ContentValues().apply { put("statement", newStatement) }
            db.update("questions", qValues, "id = ?", arrayOf(questionId.toString()))

            // Atualiza cada resposta pelo seu ID
            newAnswerTexts.forEach { (ansId, ansText) ->
                val aValues = ContentValues().apply { put("text", ansText) }
                db.update("answers", aValues, "id = ?", arrayOf(ansId.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertFullQuestion(statement: String, refText: String?, tip: String?, answerList: List<Pair<String, Boolean>>) {
        val db = this.writableDatabase
        val qValues = ContentValues().apply {
            put("statement", statement)
            put("reference_text", refText)
            put("tip", tip)
        }
        val qId = db.insert("questions", null, qValues).toInt()

        answerList.forEachIndexed { index, pair ->
            val aValues = ContentValues().apply {
                put("question_id", qId)
                put("text", pair.first)
                put("is_correct", if (pair.second) 1 else 0)
                put("sort_order", index)
            }
            db.insert("answers", null, aValues)
        }
    }

    fun saveUserAnswer(questionId: Int, answerId: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("given_answer_id", answerId) }
        db.update("questions", values, "id = ?", arrayOf(questionId.toString()))
    }

    fun clearUserAnswers() {
        val db = this.writableDatabase
        val values = ContentValues().apply { putNull("given_answer_id") }
        db.update("questions", values, null, null)
    }

    fun clearDatabase() {
        val db = this.writableDatabase
        db.delete("answers", null, null)
        db.delete("questions", null, null)
    }

    fun backupDatabase(context: Context, uri: Uri) {
        try {
            val dbFile = context.getDatabasePath("simulador.db")
            context.contentResolver.openOutputStream(uri)?.use { output ->
                dbFile.inputStream().use { input -> input.copyTo(output) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun restoreDatabase(context: Context, uri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath("simulador.db")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dbFile.outputStream().use { output -> input.copyTo(output) }
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
            val questionIds = mutableListOf<Int>()
            db.rawQuery("SELECT id FROM questions", null).use { cursor ->
                while (cursor.moveToNext()) questionIds.add(cursor.getInt(0))
            }

            questionIds.shuffled().forEachIndexed { index, id ->
                val values = ContentValues().apply { put("sort_order", index) }
                db.update("questions", values, "id = ?", arrayOf(id.toString()))
            }

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