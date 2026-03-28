package com.papito.provas.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import org.json.JSONArray

object JsonParser {

    fun importQuestionsJSON(context: Context, uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader().use { it?.readText() } ?: ""

            val jsonArray = JSONArray(jsonString)
            val dbHelper = DatabaseHelper(context)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                // 1. Mapeia os campos do seu JSON original
                val statement = obj.getString("pergunta")
                val referenceText = obj.optString("texto_referencia", null)
                val correctLetter = obj.getString("correta").lowercase().trim()
                val tip = obj.getString("dica").lowercase().trim()

                // 2. Monta a lista de respostas convertendo o formato antigo para o novo
                // Criamos os pares (Texto da Opção, É a correta?)
                val answerList = mutableListOf<Pair<String, Boolean>>()

                // Mapeamento manual de cada opção do seu JSON
                answerList.add(Pair(obj.getString("opcao_a"), correctLetter == "a"))
                answerList.add(Pair(obj.getString("opcao_b"), correctLetter == "b"))
                answerList.add(Pair(obj.getString("opcao_c"), correctLetter == "c"))
                answerList.add(Pair(obj.getString("opcao_d"), correctLetter == "d"))

                // Caso o seu JSON tenha a opção E (opcional)
                if (obj.has("opcao_e")) {
                    answerList.add(Pair(obj.getString("opcao_e"), correctLetter == "e"))
                }

                // 3. Insere no banco usando a lógica de duas tabelas (questions + answers)
                dbHelper.insertFullQuestion(statement, referenceText,tip, answerList)
            }

            Toast.makeText(context, "JSON importado com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao ler JSON: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // NOVA FUNÇÃO: Recebe a String diretamente
    fun importQuestionsJSONContent(context: Context, jsonString: String) {
        try {
            val dbHelper = DatabaseHelper(context)
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                // 1. Mapeia os campos do seu JSON original
                val statement = obj.getString("pergunta")
                val referenceText = obj.optString("texto_referencia", null)
                val correctLetter = obj.getString("correta").lowercase().trim()
                val tip = obj.getString("dica").lowercase().trim()

                // 2. Monta a lista de respostas convertendo o formato antigo para o novo
                // Criamos os pares (Texto da Opção, É a correta?)
                val answerList = mutableListOf<Pair<String, Boolean>>()

                // Mapeamento manual de cada opção do seu JSON
                answerList.add(Pair(obj.getString("opcao_a"), correctLetter == "a"))
                answerList.add(Pair(obj.getString("opcao_b"), correctLetter == "b"))
                answerList.add(Pair(obj.getString("opcao_c"), correctLetter == "c"))
                answerList.add(Pair(obj.getString("opcao_d"), correctLetter == "d"))

                // Caso o seu JSON tenha a opção E (opcional)
                if (obj.has("opcao_e")) {
                    answerList.add(Pair(obj.getString("opcao_e"), correctLetter == "e"))
                }

                // 3. Insere no banco usando a lógica de duas tabelas (questions + answers)
                dbHelper.insertFullQuestion(statement, referenceText,tip, answerList)
            }

            Toast.makeText(context, "Questões geradas por IA com sucesso!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            // Aqui você pode disparar um evento de erro para a UI se desejar
        }
    }
}