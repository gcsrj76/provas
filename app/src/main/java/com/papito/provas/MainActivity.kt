package com.papito.provas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.papito.provas.data.DatabaseHelper
import com.papito.provas.data.JsonParser
import com.papito.provas.ui.screens.ExamSimulatorApp
import com.papito.provas.viewmodel.ExamViewModel

class MainActivity : ComponentActivity() {

    // Instancia o ViewModel de forma delegada
    private val viewModel: ExamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Passamos o viewModel inteiro para o App principal
            ExamSimulatorApp(
                viewModel = viewModel,
                onFilePickerClick = { filePickerLauncher.launch("*/*") }
            )
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val type = contentResolver.getType(it)
                val isJson = type == "application/json" || it.path?.endsWith(".json") == true
                val dbHelper = DatabaseHelper(this)

                if (isJson) {
                    JsonParser.importQuestionsJSON(this, it)
                } /**else {
                    dbHelper.importarQuestoesDB(it)
                }*/

                // Após importar, avisamos o ViewModel para recarregar os dados
                viewModel.loadQuestionsFromDatabase()

                Toast.makeText(this, "Importação finalizada!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}