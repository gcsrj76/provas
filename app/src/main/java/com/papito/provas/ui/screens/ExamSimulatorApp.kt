package com.papito.provas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.papito.provas.viewmodel.ExamViewModel
import com.papito.provas.ui.components.GeminiLoadingOverlay
import com.papito.provas.R
import com.papito.provas.ui.theme.AppColors



// Se você for usar o modificador .paint() para repetir o fundo
import androidx.compose.ui.draw.paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

// Se você for usar cores e transparência (Alpha)
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background

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

    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        //color = Color.Black
    ) {
        // Camada de Loading Superior
        if (viewModel.isLoadingGemini) {
            GeminiLoadingOverlay()
        }

        when {
            !showQuestions && !showResult -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fundo_gatinhos),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.8f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f))
                    )
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Pingo Prov@s", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.atual.FontePadrao)
                        Text("(${questions.size} questões)", color = AppColors.atual.FonteSuave, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(40.dp))

                        OutlinedButton(
                            onClick = {
                                if (questions.isNotEmpty()) {
                                    val firstPending = questions.indexOfFirst { it.givenAnswerId == null }
                                    currentQuestionIndex = if (firstPending != -1) firstPending else 0
                                    viewModel.iniciarTimer()
                                    showQuestions = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (questions.isNotEmpty()) AppColors.atual.BordaPadrao else Color.DarkGray),
                            colors = if (questions.isNotEmpty()) ButtonDefaults.buttonColors(containerColor = AppColors.atual.BotaoPrincipal)else ButtonDefaults.buttonColors(containerColor = AppColors.atual.BotaoPrincipalDesabilitado)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp), tint = if (questions.isNotEmpty()) AppColors.atual.FontePadrao else AppColors.atual.FonteDesabilitada)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isSimuladoIniciado) "CONTINUAR SIMULADO" else "INICIAR SIMULADO",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (questions.isNotEmpty()) AppColors.atual.FontePadrao else AppColors.atual.FonteDesabilitada
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { viewModel.shuffle(context) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (!isSimuladoIniciado && questions.isNotEmpty()) AppColors.atual.BordaPadrao else AppColors.atual.BordaDesabilitada),
                            enabled = !isSimuladoIniciado && questions.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp), tint = if (!isSimuladoIniciado && questions.isNotEmpty()) AppColors.atual.FontePadrao else AppColors.atual.FonteDesabilitada)
                            Spacer(Modifier.width(8.dp))
                            Text("Embaralhar Ordem", color = if (!isSimuladoIniciado && questions.isNotEmpty()) AppColors.atual.FontePadrao else AppColors.atual.FonteDesabilitada)
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // --- SEÇÃO TÉCNICA ---
                        Text("GERENCIAMENTO DE DADOS", color = AppColors.atual.FontePadrao, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Divider(color = AppColors.atual.BordaPadraoSuave, modifier = Modifier.padding(vertical = 8.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallTechnicalButton("Backup", Icons.Default.KeyboardArrowUp, onCreateBackup, Modifier.weight(1f),questions.isNotEmpty())
                            SmallTechnicalButton("Restore", Icons.Default.KeyboardArrowDown, onRestoreBackup, Modifier.weight(1f),true)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- ÁREA DE IMPORTAÇÃO (Unificada no Diálogo) ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onImportGemini,
                                modifier = Modifier.weight(0.85f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, AppColors.atual.BordaPadrao),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.atual.BotaoSuave)
                            ) {
                                Icon(Icons.Default.Settings, null, Modifier.size(16.dp), tint = AppColors.atual.FontePadrao)
                                Spacer(Modifier.width(8.dp))
                                Text("Importar Questões", color = AppColors.atual.FontePadrao, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = onShowInstructions,
                                modifier = Modifier.weight(0.15f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, AppColors.atual.BordaPadraoSuave),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Info, "Ajuda", tint = AppColors.atual.FontePadrao, modifier = Modifier.size(20.dp))
                            }
                        }

                        if (questions.isNotEmpty()) {
                            TextButton(onClick = { viewModel.resetarBancoCompleto() }, modifier = Modifier.padding(top = 16.dp)) {
                                Icon(Icons.Default.Delete, null, tint = AppColors.atual.FonteAlerta, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Limpar Banco de Dados", color = AppColors.atual.FonteAlerta, fontSize = 13.sp)
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
fun SmallTechnicalButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, if (enabled) AppColors.atual.BordaPadraoSuave else AppColors.atual.BordaDesabilitada),
        enabled = enabled
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = if (enabled) AppColors.atual.BordaPadraoSuave else AppColors.atual.BordaDesabilitada)
        Spacer(Modifier.width(8.dp))
        Text(text, color = if (enabled) AppColors.atual.FontePadrao else AppColors.atual.FonteDesabilitada, fontSize = 12.sp)
    }
}