package com.papito.provas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight // Import necessário para o erro da imagem
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.papito.provas.ui.theme.Tema

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onImportFile: () -> Unit,
    onImportGemini: (String) -> Unit
) {
    // 1. ORDEM INVERTIDA AQUI: Gemini primeiro
    val options = listOf("Importar Gemini", "Importar JSON")

    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options[0]) } // Começa selecionado no Gemini
    var geminiPrompt by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 550.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Tema.atual.CardEscuro)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Selecione o Método",
                    color = Tema.atual.FontePadrao,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // --- SELETOR (COMBOBOX) ---
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedOption,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Método de Importação") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = Tema.atual.FontePadrao,
                                focusedTextColor = Tema.atual.FontePadrao,
                                unfocusedLabelColor = Tema.atual.BordaPadraoSuave,
                                focusedLabelColor = Tema.atual.BordaPadrao,
                                focusedBorderColor = Tema.atual.BordaPadrao
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Tema.atual.CardMedio)
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = Tema.atual.FontePadrao) },
                                    onClick = {
                                        selectedOption = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // --- CAMPO DE TEXTO (Aparece se Gemini estiver selecionado) ---
                    if (selectedOption == "Importar Gemini") {
                        OutlinedTextField(
                            value = geminiPrompt,
                            onValueChange = { geminiPrompt = it },
                            label = { Text("Ementa / Assuntos") },
                            placeholder = { Text("Ex: Português: Verbos e Concordância. Redes: Camada OSI...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = Tema.atual.FontePadrao,
                                focusedTextColor = Tema.atual.FontePadrao,
                                unfocusedLabelColor = Tema.atual.FonteSuave,
                                focusedLabelColor = Tema.atual.BordaPadraoSuave,
                                focusedBorderColor = Tema.atual.BordaPadraoSuave
                            )
                        )
                    }
                }

                // --- BOTÕES ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = Tema.atual.FontePadrao)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (selectedOption == "Importar JSON") {
                                onImportFile()
                            } else {
                                onImportGemini(geminiPrompt)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Tema.atual.BotaoPrincipal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CONTINUAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}