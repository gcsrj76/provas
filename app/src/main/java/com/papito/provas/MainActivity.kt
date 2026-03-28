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
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.papito.provas.ui.components.ImportDialog

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
            var showGeminiDialog by remember { mutableStateOf(false) }
            // Passamos o viewModel inteiro para o App principal
            ExamSimulatorApp(
                viewModel = viewModel,
                onCreateBackup = { createBackupLauncher.launch(gerarNomeBackup()) },
                onRestoreBackup = { restoreBackupLauncher.launch(arrayOf("*/*")) },
                onShowInstructions = { exportarInstrucoesJson(this) },
                onImportGemini = { showGeminiDialog = true }
            )

            if (showGeminiDialog) {
                ImportDialog(
                    onDismiss = { showGeminiDialog = false }, // Fecha se cancelar
                    onImportFile = {
                        showGeminiDialog = false
                        filePickerLauncher.launch("*/*")
                    },
                    onImportGemini = { promptUsuario ->
                        showGeminiDialog = false
                        buscarEGravarDados(promptUsuario) // Dispara a IA com o texto digitado
                    }
                )
            }
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

    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-3-flash-preview")

    fun buscarEGravarDados(ementaPersonalizada: String) {

        val ementaFinal = if (ementaPersonalizada.isNotBlank()) ementaPersonalizada else "Tecnologia da Informação"

        val prompt = """Monte um simulado gerando o maior numero de questões possíveis, baseado na ementa:
        $ementaFinal, e enquadre ao layout JSON :
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
        ]". Observação: é fundamental que todo o conteúdo retornado sejam apenas os dados do Json, 
        ou seja, você não deve incluir nenhuma informação, além do json, na sua resposta.
        """.trimIndent()

        // O lifecycleScope garante que, se o usuário fechar o app, a busca seja cancelada
        lifecycleScope.launch {
            try {
                viewModel.isLoadingGemini = true

                val response = model.generateContent(prompt)

                // Corrigindo o erro de String? vs String:
                // Usamos o operador elvis (?: "") para garantir uma string vazia se for null,
                // ou fazemos um check de segurança.
                val importJSON = response.text

                if (!importJSON.isNullOrBlank()) {
                    // Corrigindo o erro de Context:
                    // Usamos this@MainActivity para referenciar a Activity
                    JsonParser.importQuestionsJSONContent(this@MainActivity, importJSON)

                    // Importante: Recarregar o ViewModel após a gravação
                    viewModel.loadQuestionsFromDatabase()
                    viewModel.isLoadingGemini = false
                } else {
                    viewModel.isLoadingGemini = false
                    Toast.makeText(this@MainActivity, "IA não retornou dados", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                // Use a Main Dispatcher para Toasts dentro de catch se necessário
                viewModel.isLoadingGemini = false
                Toast.makeText(this@MainActivity, "Erro na IA: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

