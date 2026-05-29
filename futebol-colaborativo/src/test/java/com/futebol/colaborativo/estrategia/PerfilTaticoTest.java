package com.futebol.colaborativo.estrategia;

import com.futebol.colaborativo.jogo.PapelJogador;
import com.futebol.colaborativo.jogo.TipoDecisao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerfilTaticoTest {

    @Test
    void equilibradoRepresentaPesosIniciais() {
        PerfilTatico perfil = PerfilTatico.equilibrado();

        assertEquals(PapelJogador.JOGADOR, perfil.getPapel());
        assertEquals(10, perfil.getPesosBolaLivre().get(TipoDecisao.PERSEGUIR_BOLA));
        assertEquals(90, perfil.getPesosBolaLivre().get(TipoDecisao.MANTER_POSICAO_DEFENSIVA));
        assertEquals(100, perfil.getPesosOutroJogadorComBola().get(TipoDecisao.INTERCEPTAR));
        assertEquals(100, perfil.getPesosComBola().get(TipoDecisao.AGIR_COM_BOLA));
    }

    @Test
    void atacanteTemPapelCorreto() {
        assertEquals(PapelJogador.ATACANTE, PerfilTatico.atacante().getPapel());
    }

    @Test
    void zagueiroTemPapelCorreto() {
        assertEquals(PapelJogador.ZAGUEIRO, PerfilTatico.zagueiro().getPapel());
    }

    @Test
    void atacantePersegueBolaComMaisFrequenciaquezagueiro() {
        PerfilTatico atacante = PerfilTatico.atacante();
        PerfilTatico zagueiro = PerfilTatico.zagueiro();

        int perseguirAtacante = atacante.getPesosBolaLivre().get(TipoDecisao.PERSEGUIR_BOLA);
        int perseguirZagueiro = zagueiro.getPesosBolaLivre().get(TipoDecisao.PERSEGUIR_BOLA);

        assertTrue(perseguirAtacante > perseguirZagueiro,
                "Atacante deve ter peso maior para PERSEGUIR_BOLA do que zagueiro");
    }

    @Test
    void zagueiroMantemPosicaoComMaisFrequenciaQueAtacante() {
        PerfilTatico atacante = PerfilTatico.atacante();
        PerfilTatico zagueiro = PerfilTatico.zagueiro();

        int defensivoAtacante = atacante.getPesosBolaLivre().get(TipoDecisao.MANTER_POSICAO_DEFENSIVA);
        int defensivoZagueiro = zagueiro.getPesosBolaLivre().get(TipoDecisao.MANTER_POSICAO_DEFENSIVA);

        assertTrue(defensivoZagueiro > defensivoAtacante,
                "Zagueiro deve ter peso maior para MANTER_POSICAO_DEFENSIVA do que atacante");
    }

    @Test
    void atacanteEZagueiroCompartilhamPesosDeInterceptacaoEAcao() {
        PerfilTatico atacante = PerfilTatico.atacante();
        PerfilTatico zagueiro = PerfilTatico.zagueiro();

        assertEquals(
                atacante.getPesosOutroJogadorComBola().get(TipoDecisao.INTERCEPTAR),
                zagueiro.getPesosOutroJogadorComBola().get(TipoDecisao.INTERCEPTAR));

        assertEquals(
                atacante.getPesosComBola().get(TipoDecisao.AGIR_COM_BOLA),
                zagueiro.getPesosComBola().get(TipoDecisao.AGIR_COM_BOLA));
    }
}
