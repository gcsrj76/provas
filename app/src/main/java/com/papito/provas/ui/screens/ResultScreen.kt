package com.papito.provas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.papito.provas.R
import com.papito.provas.model.Question
import com.papito.provas.ui.theme.Tema

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

    Tema.atual.BackgroundResource?.let { resId ->
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.8f // Ajuste a transparência conforme o gosto
        )
    }

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
            color = Tema.atual.FonteDestaque
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CARD PRINCIPAL - Ajustado para centralização total
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Tema.atual.BotaoPadrao),
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
                    color = Tema.atual.FontePadrao,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "questões corretas",
                    color = Tema.atual.FontePadrao,
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
            colors = ButtonDefaults.buttonColors(containerColor = Tema.atual.BotaoPrincipal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp), tint = Tema.atual.FontePadrao)
            Spacer(Modifier.width(8.dp))
            Text("TELA PRINCIPAL", fontWeight = FontWeight.Bold, color = Tema.atual.FontePadrao)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReiniciarSimulado,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Tema.atual.BotaoPadrao),
            colors = ButtonDefaults.buttonColors(containerColor = Tema.atual.BotaoSuave)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Tema.atual.BotaoPadrao)
            Spacer(Modifier.width(8.dp))
            Text("Reiniciar Avaliação", color = Tema.atual.BotaoPadrao, fontSize = 16.sp)
        }
    }
}
@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Tema.atual.BotaoPadrao,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Tema.atual.BordaPadrao)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Tema.atual.FontePadrao, modifier = Modifier.size(16.dp))
            Text(value, color = Tema.atual.FontePadrao, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Tema.atual.FontePadrao, fontSize = 12.sp)
        }
    }
}