package com.papito.provas.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
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
import com.papito.provas.viewmodel.ExamViewModel
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

    // Estados para Edição
    var isEditing by remember { mutableStateOf(false) }
    var editedStatement by remember(currentQuestion.id, isEditing) { mutableStateOf(currentQuestion.statement) }
    val editedAnswers = remember(currentQuestion.id, isEditing) {
        currentQuestion.answers.map { mutableStateOf(it.text) }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Questão ${currentIndex + 1}/${questions.size}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = viewModel.formatarTempo(),
                        color = Color(0xFF009688),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // BOTÃO DE EDIÇÃO
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                // Salva as alterações através do ViewModel
                                val answerUpdates = currentQuestion.answers.mapIndexed { idx, ans ->
                                    ans.id to editedAnswers[idx].value
                                }
                                viewModel.updateQuestion(currentQuestion.id, editedStatement, answerUpdates)
                                isEditing = false
                            } else {
                                isEditing = true
                            }
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Salvar" else "Editar",
                            tint = if (isEditing) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(16.dp)
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
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF009688)
                )
            )

            Row {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$correctCount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF44336)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$wrongCount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MOLDURA DA PERGUNTA ---
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = editedStatement,
                        onValueChange = { editedStatement = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    )
                } else {
                    Text(
                        text = "${currentIndex + 1}) ${currentQuestion.statement}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                IconButton(
                    onClick = { showHint = true },
                    modifier = Modifier.size(24.dp).padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Dica",
                        tint = Color(0xFFFFC107)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LISTA DE RESPOSTAS
            currentQuestion.answers.forEachIndexed { index, answer ->
                val isSelected = currentQuestion.givenAnswerId == answer.id

                if (isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLetterByOrder(answer.sortOrder).uppercase() + ")",
                            color = Color.Gray,
                            modifier = Modifier.width(30.dp)
                        )
                        BasicTextField(
                            value = editedAnswers[index].value,
                            onValueChange = { editedAnswers[index].value = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A1A1A), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    OptionCard(
                        letra = getLetterByOrder(answer.sortOrder),
                        texto = answer.text,
                        isSelected = isSelected,
                        isCorrect = if (currentQuestion.givenAnswerId != null) answer.isCorrect else null,
                        isClickable = currentQuestion.givenAnswerId == null,
                        onClick = {
                            viewModel.selectAnswer(currentQuestion.id, answer.id)
                        },
                        correctAnswerLetter = currentQuestion.answers
                            .find { it.isCorrect }
                            ?.let { getLetterByOrder(it.sortOrder).uppercase() } ?: ""
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BOTÕES DE NAVEGAÇÃO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPrevious,
                enabled = currentIndex > 0 && !isEditing,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Anterior", fontSize = 12.sp)
            }

            Button(
                onClick = onPausar,
                enabled = !isEditing,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
            ) {
                Text("Pausa", fontSize = 12.sp)
            }

            Button(
                onClick = { if (currentIndex == questions.size - 1) onFinalizar() else onNext() },
                enabled = !isEditing,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(
                    text = if (currentIndex == questions.size - 1) "Finalizar" else "Próxima",
                    fontSize = 12.sp
                )
            }
        }
    }

    if (showHint) {
        AlertDialog(
            onDismissRequest = { showHint = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFC107))
                    Spacer(Modifier.width(8.dp))
                    Text("Dica da Questão", color = Color.White)
                }
            },
            text = {
                Text(
                    text = currentQuestion.tip ?: "Não há comentário extra para esta questão.",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                TextButton(onClick = { showHint = false }) {
                    Text("FECHAR", color = Color(0xFF009688))
                }
            },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}