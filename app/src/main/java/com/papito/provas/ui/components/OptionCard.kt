package com.papito.provas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papito.provas.ui.theme.AppColors


@Composable
fun OptionCard(
    letra: String,
    texto: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isClickable: Boolean,
    onClick: () -> Unit,
    correctAnswerLetter: String? = null
) {
    // Lógica da borda colorida (Verde para acerto, Vermelho para erro)
    val borderStroke = when {
        isCorrect == true && isSelected -> BorderStroke(2.dp, AppColors.atual.Certo)
        isCorrect == false && isSelected -> BorderStroke(2.dp, AppColors.atual.Errado)
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable) { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppColors.atual.BotaoPadrao),
        shape = RoundedCornerShape(8.dp),
        border = borderStroke
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${letra.uppercase()})",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.atual.FontePadrao,
                    modifier = Modifier.width(30.dp)
                )

                if (isCorrect == false && isSelected) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Erro",
                        tint = AppColors.atual.Errado,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(texto, color = AppColors.atual.FontePadrao, fontSize = 14.sp, modifier = Modifier.weight(1f))
            }

            if (isCorrect == false && isSelected) {
                Text(
                    text = "✓ Resposta correta: ${correctAnswerLetter ?: ""}",
                    color = AppColors.atual.Certo,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}