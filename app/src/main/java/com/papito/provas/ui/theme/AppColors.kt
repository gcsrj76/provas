package com.papito.provas.ui.theme

import androidx.compose.ui.graphics.Color
interface AppThemeColors {
    val BotaoPrincipal: Color
    val BotaoPrincipalDesabilitado: Color
    val BotaoPadrao: Color
    val BotaoSuave: Color
    val FonteDestaque: Color
    val FontePadrao: Color
    val FonteSuave: Color
    val FonteDesabilitada: Color
    val FonteAlerta: Color
    val BordaPadrao: Color
    val BordaPadraoSuave: Color
    val BordaDesabilitada: Color

    val CardEscuro: Color
    val CardMedio: Color

    val Certo: Color
    val Errado: Color
}

object TemaRosa : AppThemeColors {
    override val BotaoPrincipal = Color(0xFF5B0B4D)
    override val BotaoPrincipalDesabilitado = Color(0x335B0B4D)
    override val BotaoPadrao = Color(0xFF5B0B4D)
    override val BotaoSuave = Color(0x995B0B4D)
    override val FonteDestaque = Color(0xFF5B0B4D)
    //override val FontePadrao = Color(0xFFE1BADB)
    override val FontePadrao = Color(0xFFE3D1E0)
    override val FonteSuave = Color(0xB3E1BADB)
    override val FonteDesabilitada = Color(0x33E1BADB)
    override val FonteAlerta = Color(0xFF850E3E)
    override val BordaPadrao = Color(0xFFF165D8)
    override val BordaPadraoSuave = Color(0x80F165D8)
    override val BordaDesabilitada = Color(0x33F165D8)
    override val CardEscuro = Color(0xFF2A0423)
    override val CardMedio = Color(0xFF4F0641)
//    override val Certo = Color(0xFF166419)
//    override val Errado = Color(0xFF981E1E)
    override val Certo = Color(0xFF88B688)
    override val Errado = Color(0xFFC47D7D)

}

object TemaPadrao : AppThemeColors {
    override val BotaoPrincipal = Color(0xFF354080)
    override val BotaoPrincipalDesabilitado = Color(0x33354080)
    override val BotaoPadrao = Color(0xFF354080)
    override val BotaoSuave = Color(0x80354080)
    override val FonteDestaque = Color(0xFF131838)
    override val FontePadrao = Color(0xFFFFFFFF)
    override val FonteSuave = Color(0xCCFFFFFF)
    override val FonteDesabilitada = Color(0x33FFFFFF)
    override val FonteAlerta = Color(0x99FC0000)
    override val BordaPadrao = Color(0xFF5A6CD7)
    override val BordaPadraoSuave = Color(0x805A6CD7)
    override val BordaDesabilitada = Color(0x335A6CD7)
    override val CardEscuro = Color(0xFF171C36)
    override val CardMedio = Color(0xFF262E59)
    //    override val Certo = Color(0xFF166419)
//    override val Errado = Color(0xFF981E1E)
    override val Certo = Color(0xFF88B688)
    override val Errado = Color(0xFFC47D7D)

}

object AppColors {
    val atual: AppThemeColors = TemaPadrao
}