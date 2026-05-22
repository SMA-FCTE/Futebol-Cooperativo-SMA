package com.futebol.colaborativo.estrategia;

import com.futebol.colaborativo.jogo.PapelJogador;
import com.futebol.colaborativo.jogo.TipoDecisao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerfilTaticoTest {

    @Test
    void equilibradoRepresentaPesosIniciais() {
        PerfilTatico perfil = PerfilTatico.equilibrado();

        assertEquals(PapelJogador.JOGADOR, perfil.getPapel());
        assertEquals(85, perfil.getPesosBolaLivre().get(TipoDecisao.PERSEGUIR_BOLA));
        assertEquals(15, perfil.getPesosBolaLivre().get(TipoDecisao.MANTER_POSICAO_DEFENSIVA));
        assertEquals(100, perfil.getPesosOutroJogadorComBola().get(TipoDecisao.INTERCEPTAR));
        assertEquals(100, perfil.getPesosComBola().get(TipoDecisao.AGIR_COM_BOLA));
    }
}
