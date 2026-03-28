package com.papito.provas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings // Ícone legal para IA
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papito.provas.viewmodel.ExamViewModel
import com.papito.provas.ui.components.GeminiLoadingOverlay

@Composable
fun ExamSimulatorApp(
    viewModel: ExamViewModel,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onShowInstructions: () -> Unit,
    onImportGemini: () -> Unit
) {
    val questions = viewModel.questoesCarregadas
    val isSimuladoIniciado = questions.any { it.givenAnswerId != null }

    // Ajuste: O index só deve ser calculado na primeira vez ou quando entrar na tela de questões
    var currentQuestionIndex by remember { mutableIntStateOf(0) }

    var showQuestions by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        // Camada de Loading Superior
        if (viewModel.isLoadingGemini) {
            GeminiLoadingOverlay()
        }

        when {
            !showQuestions && !showResult -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
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

                        OutlinedButton(
                            onClick = { viewModel.shuffle() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSimuladoIniciado) Color.DarkGray else Color.Gray),
                            enabled = !isSimuladoIniciado
                        ) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp), tint = if (isSimuladoIniciado) Color.DarkGray else Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Embaralhar Ordem", color = if (isSimuladoIniciado) Color.DarkGray else Color.White)
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // --- SEÇÃO TÉCNICA ---
                        Text("GERENCIAMENTO DE DADOS", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallTechnicalButton("Backup", Icons.Default.KeyboardArrowUp, onCreateBackup, Modifier.weight(1f))
                            SmallTechnicalButton("Restore", Icons.Default.KeyboardArrowDown, onRestoreBackup, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- ÁREA DE IMPORTAÇÃO (Unificada no Diálogo) ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onImportGemini,
                                modifier = Modifier.weight(0.85f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D2D))
                            ) {
                                Icon(Icons.Default.Settings, null, Modifier.size(16.dp), tint = Color(0xFF009688))
                                Spacer(Modifier.width(8.dp))
                                Text("Importar Questões", color = Color.White, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = onShowInstructions,
                                modifier = Modifier.weight(0.15f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, Color.DarkGray),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Info, "Ajuda", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }

                        if (questions.isNotEmpty()) {
                            TextButton(onClick = { viewModel.resetarBancoCompleto() }, modifier = Modifier.padding(top = 16.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Limpar Banco de Dados", color = Color.Red.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

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
                        showQuestions = false
                    },
                    onQuestionSelect = { index -> currentQuestionIndex = index }
                )
            }

            showResult -> {
                ResultScreen(
                    questions = questions,
                    tempoFormatado = viewModel.formatarTempo(),
                    onVoltarMenu = {
                        showResult = false
                        showQuestions = false
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
fun SmallTechnicalButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color.DarkGray)
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = Color.Gray)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}