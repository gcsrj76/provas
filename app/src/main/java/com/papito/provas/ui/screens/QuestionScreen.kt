package com.papito.provas.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papito.provas.ui.components.OptionCard
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.papito.provas.R
import com.papito.provas.viewmodel.ExamViewModel
import com.papito.provas.ui.theme.Tema
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun QuestionScreen(
    viewModel: ExamViewModel,
    currentIndex: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFinalizar: () -> Unit,
    onPausar: () -> Unit,
    onQuestionSelect: (Int) -> Unit
) {
    val questions = viewModel.questoesCarregadas
    if (questions.isEmpty()) return

    val currentQuestion = questions[currentIndex]
    var showHint by remember { mutableStateOf(false) }

    // --- ESTADOS DE EDIÇÃO ---
    var isEditing by remember { mutableStateOf(false) }
    var editedStatement by remember(currentQuestion.id, isEditing) { mutableStateOf(currentQuestion.statement) }
    val editedAnswers = remember(currentQuestion.id, isEditing) {
        currentQuestion.answers.map { mutableStateOf(it.text) }
    }
    var editedCorrectAnswerId by remember(currentQuestion.id, isEditing) {
        mutableStateOf(currentQuestion.answers.find { it.isCorrect }?.id)
    }

    // --- CONTADORES ---
    val correctCount = questions.count { question ->
        question.answers.find { it.id == question.givenAnswerId }?.isCorrect == true
    }
    val wrongCount = questions.count { question ->
        val selectedAnswer = question.answers.find { it.id == question.givenAnswerId }
        question.givenAnswerId != null && selectedAnswer?.isCorrect == false
    }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    fun getLetterByOrder(order: Int): String = ('a' + order).toString()

    LaunchedEffect(currentIndex) {
        viewModel.mostrarAnimacaoAcerto = false
        viewModel.mostrarAnimacaoErro = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- FUNDO DINÂMICO ---
        Tema.atual.BackgroundResource?.let { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(Unit) {
                    if (!isEditing) {
                        awaitEachGesture {
                            val down = awaitFirstDown(pass = PointerEventPass.Initial)
                            var dragged = false
                            do {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                val dragChange = event.changes.firstOrNull()
                                if (dragChange != null && dragChange.pressed) {
                                    offsetX += dragChange.positionChange().x
                                    offsetY += dragChange.positionChange().y
                                    if (abs(offsetX) > 10 || abs(offsetY) > 10) dragged = true
                                }
                            } while (event.changes.any { it.pressed })

                            if (dragged) {
                                if (offsetX < -160) onNext()
                                else if (offsetX > 160) onPrevious()
                            }
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
        ) {
            // --- CABEÇALHO ---
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Questão ${currentIndex + 1}/${questions.size}",
                        color = Tema.atual.FonteDestaque,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = viewModel.formatarTempo(),
                            color = Tema.atual.FonteDestaque,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (isEditing) {
                                    val answerUpdates = currentQuestion.answers.mapIndexed { idx, ans ->
                                        val isThisCorrect = ans.id == editedCorrectAnswerId
                                        Triple(ans.id, editedAnswers[idx].value, isThisCorrect)
                                    }
                                    viewModel.updateQuestion(currentQuestion.id, editedStatement, answerUpdates)
                                    isEditing = false
                                } else {
                                    isEditing = true
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isEditing) Tema.atual.BotaoPadrao else Tema.atual.BotaoSuave,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Slider(
                    value = currentIndex.toFloat(),
                    onValueChange = { if (!isEditing) onQuestionSelect(it.toInt()) },
                    valueRange = 0f..(if (questions.size > 1) (questions.size - 1).toFloat() else 1f),
                    modifier = Modifier.weight(2f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Tema.atual.BotaoPadrao,
                        activeTrackColor = Tema.atual.BotaoPadrao
                    )
                )

                Row {
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Tema.atual.Certo), contentAlignment = Alignment.Center) {
                        Text("$correctCount", color = Tema.atual.FontePadrao, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Tema.atual.Errado), contentAlignment = Alignment.Center) {
                        Text("$wrongCount", color = Tema.atual.FontePadrao, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CORPO DA QUESTÃO ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                    .background(Tema.atual.BotaoSuave, RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Enunciado
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (isEditing) {
                        BasicTextField(
                            value = editedStatement,
                            onValueChange = { editedStatement = it },
                            textStyle = TextStyle(color = Tema.atual.FontePadrao, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier.weight(1f).background(Tema.atual.BotaoPadrao.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(4.dp)
                        )
                    } else {
                        Text(
                            text = "${currentIndex + 1}) ${currentQuestion.statement}",
                            color = Tema.atual.FontePadrao,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    IconButton(onClick = { showHint = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFFFC107))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Respostas
                currentQuestion.answers.forEachIndexed { index, answer ->
                    if (isEditing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = editedCorrectAnswerId == answer.id,
                                onClick = { editedCorrectAnswerId = answer.id },
                                colors = RadioButtonDefaults.colors(selectedColor = Tema.atual.Certo)
                            )
                            Text(getLetterByOrder(answer.sortOrder).uppercase() + ")", color = Tema.atual.FonteDestaque, modifier = Modifier.width(25.dp))
                            BasicTextField(
                                value = editedAnswers[index].value,
                                onValueChange = { editedAnswers[index].value = it },
                                textStyle = TextStyle(color = Tema.atual.FontePadrao, fontSize = 14.sp),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier.fillMaxWidth().background(Tema.atual.BotaoPadrao.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(8.dp)
                            )
                        }
                    } else {
                        OptionCard(
                            letra = getLetterByOrder(answer.sortOrder),
                            texto = answer.text,
                            isSelected = currentQuestion.givenAnswerId == answer.id,
                            isCorrect = if (currentQuestion.givenAnswerId != null) answer.isCorrect else null,
                            isClickable = currentQuestion.givenAnswerId == null,
                            onClick = { viewModel.selectAnswer(currentQuestion.id, answer.id) },
                            correctAnswerLetter = currentQuestion.answers.find { it.isCorrect }?.let { getLetterByOrder(it.sortOrder).uppercase() } ?: ""
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- NAVEGAÇÃO ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPrevious, enabled = currentIndex > 0 && !isEditing, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Tema.atual.BotaoPadrao)) { Text("Anterior") }
                Button(onClick = onPausar, enabled = !isEditing, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Tema.atual.BotaoSuave)) { Text("Pausa") }
                Button(onClick = { if (currentIndex == questions.size - 1) onFinalizar() else onNext() }, enabled = !isEditing, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Tema.atual.BotaoPadrao)) {
                    Text(if (currentIndex == questions.size - 1) "Finalizar" else "Próxima")
                }
            }
        }

        // Acerto (Gatinho Feliz)
        Tema.atual.AnimacaoRight?.let { resId ->
            AnimacaoFeedback(
                exibir = viewModel.mostrarAnimacaoAcerto,
                lottieResId = resId,
                aoFinalizar = { viewModel.mostrarAnimacaoAcerto = false },
                isSuccess = true
            )
        }

        Tema.atual.AnimacaoWrong?.let { resId ->
            AnimacaoFeedback(
                exibir = viewModel.mostrarAnimacaoErro,
                lottieResId = resId,
                aoFinalizar = { viewModel.mostrarAnimacaoErro = false },
                isSuccess = false
            )
        }
    }

    if (showHint) {
        AlertDialog(
            onDismissRequest = { showHint = false },
            title = { Text("Dica", color = Tema.atual.FontePadrao) },
            text = { Text(currentQuestion.tip ?: "Sem dicas.", color = Color.LightGray) },
            confirmButton = { TextButton(onClick = { showHint = false }) { Text("OK") } },
            containerColor = Color(0xFF1A1A1A)
        )
    }
}

/**
@Composable
fun AnimacaoGatinhoSucesso(
    exibir: Boolean,
    aoFinalizar: () -> Unit
) {
    if (exibir) {
        // MUITO IMPORTANTE: O nome gatinho_dancante deve ser igual ao arquivo em res/raw
        val composicao by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gatinho_dancante))
        val progresso by animateLottieCompositionAsState(composicao)

        val subirAnimado by animateFloatAsState(
            targetValue = if (exibir) -250f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "GatinhoSubindo"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .offset(y = subirAnimado.dp)
                    .size(280.dp)
            ) {
                LottieAnimation(
                    composition = composicao,
                    progress = { progresso }
                )
            }
        }

        LaunchedEffect(exibir) {
            if (exibir) {
                delay(2500)
                aoFinalizar()
            }
        }
    }
}
*/
@Composable
fun AnimacaoFeedback(
    exibir: Boolean,
    lottieResId: Int,
    aoFinalizar: () -> Unit,
    isSuccess: Boolean
) {
    if (exibir) {
        val composicao by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
        val progresso by animateLottieCompositionAsState(composicao)

        // Se for sucesso, ele sobe do fundo. Se for erro, ele pode aparecer no centro ou cair do topo.
        // Vou manter o efeito de "subida" para ambos, mas você pode mudar o targetValue se quiser.
        val animY by animateFloatAsState(
            targetValue = if (exibir) -250f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "AnimacaoY"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (isSuccess) Alignment.BottomCenter else Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(y = if (isSuccess) animY.dp else 0.dp)
                    .size(if (isSuccess) 280.dp else 220.dp)
            ) {
                LottieAnimation(
                    composition = composicao,
                    progress = { progresso }
                )
            }
        }

        LaunchedEffect(exibir) {
            if (exibir) {
                delay(2500)
                aoFinalizar()
            }
        }
    }
}