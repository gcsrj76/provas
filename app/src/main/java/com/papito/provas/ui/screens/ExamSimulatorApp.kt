package com.papito.provas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papito.provas.viewmodel.ExamViewModel

@Composable
fun ExamSimulatorApp(
    viewModel: ExamViewModel,
    onFilePickerClick: () -> Unit
) {
    // Acessamos a lista diretamente do ViewModel
    val questoes = viewModel.questoesCarregadas

    var currentQuestionIndex by remember(questoes.size) {
        val primeiroPendente = questoes.indexOfFirst { it.respostaDada == null }
        mutableIntStateOf(if (primeiroPendente != -1) primeiroPendente else 0)
    }

    // Estado local para gerenciar o que foi respondido nesta sessão
    val respostasIniciais = questoes.filter { it.respostaDada != null }
        .associate { it.id to it.respostaDada!! }

    var selectedAnswers by remember(questoes) { mutableStateOf(respostasIniciais) }
    var answeredQuestions by remember(questoes) { mutableStateOf(respostasIniciais.keys) }

    var showQuestions by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    // Estrutura Visual Mantida Identicamente
    when {
        !showQuestions && !showResult -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Prov@s",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${questoes.size} questões carregadas",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { if (questoes.isNotEmpty()) showQuestions = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
                    ) {
                        Text("Iniciar Simulado", fontSize = 18.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onFilePickerClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text("Importar Questões (JSON/DB)", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (questoes.isNotEmpty()) {
                        TextButton(onClick = { viewModel.resetarBancoCompleto() }) {
                            Text("Limpar Banco de Dados", color = Color.Red)
                        }
                    }
                }
            }
        }

        showQuestions -> {
            QuestionScreen(
                questoes = questoes,
                currentIndex = currentQuestionIndex,
                selectedAnswers = selectedAnswers,
                answeredQuestions = answeredQuestions,
                onAnswerSelected = { questionId, answer ->
                    selectedAnswers = selectedAnswers + (questionId to answer)
                    answeredQuestions = answeredQuestions + questionId
                },
                onNext = {
                    if (currentQuestionIndex < questoes.size - 1) currentQuestionIndex++
                },
                onPrevious = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                onFinalizar = {
                    // Usa o ViewModel para persistir os dados
                    viewModel.salvarRespostas(selectedAnswers)
                    showResult = true
                    showQuestions = false
                },
                onQuestionSelect = { index -> currentQuestionIndex = index }
            )
        }

        showResult -> {
            ResultScreen(
                questoes = questoes,
                selectedAnswers = selectedAnswers,
                onVoltarMenu = {
                    showResult = false
                    showQuestions = false
                    currentQuestionIndex = 0
                },
                onReiniciarSimulado = {
                    // Limpeza via ViewModel
                    viewModel.limparApenasRespostas()
                    selectedAnswers = emptyMap()
                    answeredQuestions = emptySet()
                    showResult = false
                    showQuestions = false
                    currentQuestionIndex = 0
                }
            )
        }
    }
}