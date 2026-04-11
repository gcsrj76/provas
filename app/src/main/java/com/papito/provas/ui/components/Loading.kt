package com.papito.provas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.papito.provas.ui.theme.AppTheme
import com.papito.provas.ui.theme.Tema

@Composable
fun GeminiLoadingOverlay(
    message: String = "O Gemini está pensando..."
) {
    // Usamos um Dialog para travar a tela enquanto carrega
    Dialog(
        onDismissRequest = { /* Não fecha ao clicar fora */ },
        properties = DialogProperties(
            dismissOnBackPress = false, // Impede fechar com botão voltar
            dismissOnClickOutside = false // Impede fechar ao clicar fora
        )
    ) {
        // Caixa centralizada com bordas arredondadas e cor escura do seu tema
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .background(Tema.atual.CardEscuro, shape = RoundedCornerShape(16.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // O círculo giratório padrão do Material3
                CircularProgressIndicator(
                    color = Tema.atual.BotaoPadrao,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(50.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = message,
                    color = Tema.atual.FontePadrao,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}