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
    val questions = viewModel.questoesCarregadas

    var currentQuestionIndex by remember(questions.size) {
        val firstPending = questions.indexOfFirst { it.givenAnswerId == null }
        mutableIntStateOf(if (firstPending != -1) firstPending else 0)
    }

    var showQuestions by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black // Mantendo o tema escuro solicitado
    ) {
        when {
            // --- TELA INICIAL (MENU) ---
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
                            text = "${questions.size} questões carregadas",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { if (questions.isNotEmpty()) showQuestions = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                            enabled = questions.isNotEmpty()
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

                        if (questions.isNotEmpty()) {
                            TextButton(onClick = { viewModel.resetarBancoCompleto() }) {
                                Text("Limpar Banco de Dados", color = Color.Red)
                            }
                        }
                    }
                }
            }

            // --- TELA DE QUESTÕES ---
            showQuestions -> {
                QuestionScreen(
                    viewModel = viewModel,
                    currentIndex = currentQuestionIndex,
                    onNext = {
                        if (currentQuestionIndex < questions.size - 1) currentQuestionIndex++
                    },
                    onPrevious = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                    onFinalizar = {
                        showResult = false
                        showQuestions = false
                    },
                    onQuestionSelect = { index -> currentQuestionIndex = index }
                )
            }

            // --- TELA DE RESULTADOS ---
            showResult -> {
                ResultScreen(
                    questions = questions,
                    onVoltarMenu = {
                        showResult = false
                        showQuestions = false
                        currentQuestionIndex = 0
                    },
                    onReiniciarSimulado = {
                        viewModel.limparApenasRespostas()
                        showResult = false
                        showQuestions = false
                        currentQuestionIndex = 0
                    }
                )
            }
        }
    }
}