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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    // Instancia o ViewModel de forma delegada
    private val viewModel: ExamViewModel by viewModels()

    // Launcher para CRIAR o arquivo de backup
    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { viewModel.createBackup(it, this) }
    }

    // Launcher para SELECIONAR um backup existente
    private val restoreBackupLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreBackup(it, this) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Passamos o viewModel inteiro para o App principal
            ExamSimulatorApp(
                viewModel = viewModel,
                onFilePickerClick = { filePickerLauncher.launch("*/*") },
                onCreateBackup = { createBackupLauncher.launch(gerarNomeBackup()) },
                onRestoreBackup = { restoreBackupLauncher.launch(arrayOf("*/*")) }
            )
        }
    }

    private fun gerarNomeBackup(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dataHoraAtual = sdf.format(Date())
        // return "prova_bkp_$dataHoraAtual.db"
        return "prova_bkp.db"
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