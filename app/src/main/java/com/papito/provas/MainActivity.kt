package com.papito.provas

import android.content.Context
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
                onRestoreBackup = { restoreBackupLauncher.launch(arrayOf("*/*")) },
                onShowInstructions = { exportarInstrucoesJson(this) }
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

    private fun exportarInstrucoesJson(context: Context) {
        val instrucoes = """
        - Solicite a geraçao das questoes pela IA;
        - Copie e cole o conteudo gerado em um arquivo;
        - Salve o arquivo com uma extensao .json;
         
        Estrutura do arquivo JSON para importçao:
        
        O arquivo deve ser uma lista [] de objetos, seguindo este padrão:
        "[
        {
        "pergunta": "Texto da pergunta",
        "opcao_a": "Opção A",
        "opcao_b": "Opção B",
        "opcao_c": "Opção C",
        "opcao_d": "Opção D",
        "correta": "<opção correta>",
        "link_conteudo": "Vazio",
        "texto_referencia": "Vazio",
        "materia": "Vazio"
        }
        ]"
        
        Exemplo de prompt de solicitaçao para a IA:        
        
        Monte um simulado gerando o maior numero de questões possíveis, baseado na ementa:
        "Formação de palavras: derivação / Frase, oração e período / Tipos de verbo", e enquadre ao layout JSON : 
        "[
            {
                "pergunta": "Texto da pergunta",
                "opcao_a": "Opção A",
                "opcao_b": "Opção B",
                "opcao_c": "Opção C",
                "opcao_d": "Opção D",
                "correta": "<opção correta>",
                "dica": "Conteudo para auxiliar o entendimento da questao",
                "texto_referencia": "Vazio",
                "materia": "Vazio"
            }
        ]"
    """.trimIndent()

        // Lógica para salvar ou compartilhar o texto...
        val sendIntent: android.content.Intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, instrucoes)
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "Salvar Instruções")
        context.startActivity(shareIntent)
    }
}