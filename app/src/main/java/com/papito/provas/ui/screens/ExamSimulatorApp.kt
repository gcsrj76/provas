package com.papito.provas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowUp   // Para o Backup
import androidx.compose.material.icons.filled.KeyboardArrowDown // Para o Restore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info // Ícone de ajuda
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papito.provas.viewmodel.ExamViewModel

@Composable
fun ExamSimulatorApp(
    viewModel: ExamViewModel,
    onFilePickerClick: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onShowInstructions: () -> Unit,
    onImportGemini: () -> Unit
) {
    val questions = viewModel.questoesCarregadas
    val isSimuladoIniciado = questions.any { it.givenAnswerId != null }

    var currentQuestionIndex by remember(questions.size) {
        val firstPending = questions.indexOfFirst { it.givenAnswerId == null }
        mutableIntStateOf(if (firstPending != -1) firstPending else 0)
    }

    var showQuestions by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
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
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Prov@s", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("(${questions.size} questões)", color = Color.Gray, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(40.dp))

                        // --- AÇÃO PRINCIPAL ---
                        Button(
                            onClick = {
                                if (questions.isNotEmpty()) {
                                    val firstPending = questions.indexOfFirst { it.givenAnswerId == null }
                                    currentQuestionIndex = if (firstPending != -1) firstPending else 0
                                    viewModel.iniciarTimer()
                                    showQuestions = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isSimuladoIniciado) "CONTINUAR SIMULADO" else "INICIAR SIMULADO",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- UTILITÁRIO DE ESTUDO ---
                        OutlinedButton(
                            onClick = { viewModel.shuffle() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSimuladoIniciado) Color.DarkGray else Color.Gray),
                            enabled = !isSimuladoIniciado
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSimuladoIniciado) Color.DarkGray else Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Embaralhar Ordem",
                                color = if (isSimuladoIniciado) Color.DarkGray else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // --- SEÇÃO TÉCNICA ---
                        Text(
                            "GERENCIAMENTO DE DADOS",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Divider(
                            color = Color.DarkGray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallTechnicalButton(
                                "Backup",
                                Icons.Default.KeyboardArrowUp,
                                onCreateBackup,
                                Modifier.weight(1f)
                            )
                            SmallTechnicalButton(
                                "Restore",
                                Icons.Default.KeyboardArrowDown,
                                onRestoreBackup,
                                Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- ÁREA DE IMPORTAÇÃO (90/10) ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Botão Importar (90%)
                            OutlinedButton(
                                onClick = onImportGemini,
                                modifier = Modifier.weight(0.9f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, Color.DarkGray)
                            ) {
                                Text("Importar Questões", color = Color.LightGray, fontSize = 13.sp)
                            }

                            // Botão Ajuda (10%)
                            OutlinedButton(
                                onClick = onShowInstructions,
                                modifier = Modifier.weight(0.1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, Color.DarkGray),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Instruções",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (questions.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.resetarBancoCompleto() },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Limpar tudo", color = Color.Red, fontSize = 12.sp)
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
                    onNext = { if (currentQuestionIndex < questions.size - 1) currentQuestionIndex++ },
                    onPrevious = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                    onFinalizar = {
                        viewModel.pararTimer()
                        showResult = true
                        showQuestions = false
                    },
                    onPausar = {
                        viewModel.pararTimer()
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
                    tempoFormatado = viewModel.formatarTempo(),
                    onVoltarMenu = {
                        showResult = false
                        showQuestions = false
                        currentQuestionIndex = 0
                    },
                    onReiniciarSimulado = {
                        viewModel.limparApenasRespostas()
                        showResult = false
                        showQuestions = true
                        currentQuestionIndex = 0
                    }
                )
            }
        }
    }
}

@Composable
fun SmallTechnicalButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color.Gray),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.White
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontSize = 12.sp)
    }
}