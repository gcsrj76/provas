package com.papito.provas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papito.provas.ui.components.OptionCard
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import com.papito.provas.viewmodel.ExamViewModel
import kotlin.math.abs // Para a função abs() de distância

@Composable
fun QuestionScreen(
    viewModel: ExamViewModel,
    currentIndex: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFinalizar: () -> Unit,
    onQuestionSelect: (Int) -> Unit
) {
    val questions = viewModel.questoesCarregadas
    if (questions.isEmpty()) return

    val currentQuestion = questions[currentIndex]
    val scrollState = rememberScrollState()

    val correctCount = questions.count { question ->
        question.answers.find { it.id == question.givenAnswerId }?.isCorrect == true
    }

    val wrongCount = questions.count { question ->
        val selectedAnswer = question.answers.find { it.id == question.givenAnswerId }
        question.givenAnswerId != null && selectedAnswer?.isCorrect == false
    }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Função utilitária para converter ordem (0,1,2...) em letra (a,b,c...)
    fun getLetterByOrder(order: Int): String = ('a' + order).toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .pointerInput(Unit) {
                // Usamos o Initial para "espiar" o toque antes dos botões
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    var dragged = false

                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val dragChange = event.changes.firstOrNull()

                        if (dragChange != null && dragChange.pressed) {
                            // Acumulamos o movimento
                            offsetX += dragChange.positionChange().x
                            offsetY += dragChange.positionChange().y

                            // Se moveu mais de 10 pixels, consideramos um arrasto e não um clique
                            if (abs(offsetX) > 10 || abs(offsetY) > 10) {
                                dragged = true
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // Ao soltar o dedo (onDragEnd manual)
                    if (dragged) {
                        if (offsetX < -160) onNext()
                        else if (offsetX > 160) onPrevious()
                        else if (offsetY < -250) onFinalizar()
                    }

                    // Reseta para o próximo toque
                    offsetX = 0f
                    offsetY = 0f
                }
            }
    )
    {
        // --- CABEÇALHO (Paginação) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Questão ${currentIndex + 1}/${questions.size}",
                color = Color.White,
                fontSize = 14.sp
            )

            Slider(
                value = currentIndex.toFloat(),
                onValueChange = { onQuestionSelect(it.toInt()) },
                valueRange = 0f..(if (questions.size > 1) (questions.size - 1).toFloat() else 1f),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White
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
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Enunciado
            Text(
                text = "${currentIndex + 1}) ${currentQuestion.statement}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- LISTA DINÂMICA DE RESPOSTAS ---
            currentQuestion.answers.forEach { answer ->
                val isSelected = currentQuestion.givenAnswerId == answer.id

                OptionCard(
                    letra = getLetterByOrder(answer.sortOrder),
                    texto = answer.text,
                    isSelected = isSelected,
                    // Mostra se está correto apenas se a questão já tiver uma resposta dada
                    isCorrect = if (currentQuestion.givenAnswerId != null) answer.isCorrect else null,
                    isClickable = currentQuestion.givenAnswerId == null,
                    onClick = {
                        viewModel.selectAnswer(currentQuestion.id, answer.id)
                    },
                    correctAnswerLetter = currentQuestion.answers
                        .find { it.isCorrect }
                        ?.let { getLetterByOrder(it.sortOrder) } ?: ""
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BOTÕES DE NAVEGAÇÃO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                modifier = Modifier
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Voltar", color = Color.White)
            }

            OutlinedButton(
                onClick = {onFinalizar()},
                modifier = Modifier
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF009688))
            ) {
                Text("Pausa",color = Color.White)
            }

            Button(
                onClick = {
                    if (currentIndex == questions.size - 1) {
                        // Se for a última questão, chama a função de finalizar (que grava no banco)
                        onFinalizar()
                    } else {
                        // Se não for a última, apenas navega para a próxima (sem gravar)
                        onNext()
                    }
                },
                modifier = Modifier
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(
                    text = if (currentIndex == questions.size - 1) "Finalizar" else "Próxima",
                    color = Color.White
                )
            }
        }
    }
}