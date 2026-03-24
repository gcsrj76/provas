package com.papito.provas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.papito.provas.model.Question

@Composable
fun ResultScreen(
    questions: List<Question>,
    tempoFormatado: String,
    onVoltarMenu: () -> Unit,
    onReiniciarSimulado: () -> Unit
) {
    val acertos = questions.count { q ->
        q.answers.any { it.id == q.givenAnswerId && it.isCorrect }
    }
    val total = questions.size
    val aproveitamento = if (total > 0) (acertos * 100) / total else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Resultado Final",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CARD PRINCIPAL - Ajustado para centralização total
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth() // Garante que a coluna ocupe toda a largura do card
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally, // Centraliza os textos
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$acertos/$total",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "questões corretas",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                val corStatus = if (aproveitamento >= 70) Color(0xFF009688) else Color(0xFFCF6679)
                Text(
                    text = "$aproveitamento% de aproveitamento",
                    color = corStatus,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // GRID DE ESTATÍSTICAS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Tempo", tempoFormatado, Icons.Default.DateRange, Modifier.weight(1f))
            StatCard("Erros", "${total - acertos}", Icons.Default.Close, Modifier.weight(1f))
        }

        // Este Spacer empurra os botões para o rodapé
        Spacer(modifier = Modifier.weight(1f))

        // BOTÕES DE AÇÃO
        Button(
            onClick = onVoltarMenu,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("TELA PRINCIPAL", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReiniciarSimulado,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF009688))
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF009688))
            Spacer(Modifier.width(8.dp))
            Text("Reiniciar Avaliação", color = Color(0xFF009688))
        }
    }
}
//segunda
/**
@Composable
fun ResultScreen(
    questions: List<Question>,
    tempoFormatado: String, // Passe o viewModel.formatarTempo() aqui
    onVoltarMenu: () -> Unit,
    onReiniciarSimulado: () -> Unit
) {
    val acertos = questions.count { q ->
        q.answers.any { it.id == q.givenAnswerId && it.isCorrect }
    }
    val total = questions.size
    val aproveitamento = if (total > 0) (acertos * 100) / total else 0

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Resultado Final", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(24.dp))

        // CARD PRINCIPAL DE NOTA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "$acertos/$total", fontSize = 56.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(text = "questões corretas", color = Color.Gray)

                val corStatus = if (aproveitamento >= 70) Color(0xFF009688) else Color(0xFFCF6679)
                Text(
                    text = "$aproveitamento% de aproveitamento",
                    color = corStatus,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GRID DE ESTATÍSTICAS RÁPIDAS
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Tempo", tempoFormatado, Icons.Default.DateRange, Modifier.weight(1f))
            StatCard("Erros", "${total - acertos}", Icons.Default.Close, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.weight(1f))

        // BOTÕES PADRONIZADOS
        Button(
            onClick = onVoltarMenu,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("TELA PRINCIPAL")
        }

        // ... Botão de Reiniciar abaixo
    }
}
*/
@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color.DarkGray)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

//primeira




/**
@Composable
fun ResultScreen(
    questions: SnapshotStateList<Question>,
    tempoFormatado: String,
    onVoltarMenu: () -> Unit,           // Apenas volta para a tela inicial
    onReiniciarSimulado: () -> Unit     // Limpa respostas e volta para o início do quiz
) {

    val totalQuestions = questions.size
    val correctAnswers = questions.count { question ->
        val selectedAnswer = question.answers.find { it.id == question.givenAnswerId }
        selectedAnswer?.isCorrect == true
    }

    val scorePercentage = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Resultado Final",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CARD PRINCIPAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${correctAnswers}/${totalQuestions}",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(text = "questões corretas", color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lógica de cor dinâmica para o aproveitamento
                    val colorStatus = if (scorePercentage >= 70) Color(0xFF009688) else Color.Red

                    Text(
                        text = "$scorePercentage% de aproveitamento",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorStatus
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // BOTÕES (Mantendo o padrão da Home)
            Button(
                onClick = onVoltarMenu,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("TELA PRINCIPAL", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onReiniciarSimulado,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF009688)) // Borda verde para destacar
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF009688))
                Spacer(Modifier.width(8.dp))
                Text("Reiniciar Avaliação", color = Color(0xFF009688))
            }
        }
    }
}*/