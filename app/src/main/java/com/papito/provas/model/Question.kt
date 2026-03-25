package com.papito.provas.model

data class Answer(
    val id: Int = 0,
    val questionId: Int,
    val text: String,
    val isCorrect: Boolean,
    val sortOrder: Int // 0 para A, 1 para B, etc.
)
data class Question(
    val id: Int,
    val statement: String,
    val answers: List<Answer> = emptyList(),
    val referenceText: String? = null,
    val tip: String? = null,
    var givenAnswerId: Int? = null // Agora armazena o ID da Answer selecionada
)