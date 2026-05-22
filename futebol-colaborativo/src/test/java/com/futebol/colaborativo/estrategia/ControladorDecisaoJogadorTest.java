package com.futebol.colaborativo.estrategia;

import com.futebol.colaborativo.jogo.ContextoDecisao;
import com.futebol.colaborativo.jogo.PapelJogador;
import com.futebol.colaborativo.jogo.Time;
import com.futebol.colaborativo.jogo.TipoDecisao;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControladorDecisaoJogadorTest {

    @Test
    void retornaAgirComBolaQuandoJogadorAtualEstaComBola() {
        JogadorEstado estadoAtual = criarEstado(10, 10, true);
        ControladorDecisaoJogador controlador = criarControlador(85);

        TipoDecisao decisao = controlador.decidir(criarContexto(estadoAtual, null, 0));

        assertEquals(TipoDecisao.AGIR_COM_BOLA, decisao);
    }

    @Test
    void retornaInterceptarQuandoOutroJogadorEstaComBola() {
        JogadorEstado estadoAtual = criarEstado(10, 10, false);
        JogadorEstado outroJogadorComBola = criarEstado(20, 20, true);
        ControladorDecisaoJogador controlador = criarControlador(85);

        TipoDecisao decisao = controlador.decidir(criarContexto(estadoAtual, outroJogadorComBola, 0));

        assertEquals(TipoDecisao.INTERCEPTAR, decisao);
    }

    @Test
    void sorteiaDecisaoQuandoBolaEstaLivre() {
        posicionarBola(50, 50);
        JogadorEstado estadoAtual = criarEstado(10, 10, false);
        ControladorDecisaoJogador controlador = criarControlador(85);

        TipoDecisao decisao = controlador.decidir(criarContexto(estadoAtual, null, 0));

        assertEquals(TipoDecisao.MANTER_POSICAO_DEFENSIVA, decisao);
    }

    @Test
    void forcaPerseguirQuandoJogadorEstaPertoDaBolaLivre() {
        posicionarBola(10, 10);
        JogadorEstado estadoAtual = criarEstado(10, 10, false);
        ControladorDecisaoJogador controlador = criarControlador(85);

        TipoDecisao decisao = controlador.decidir(criarContexto(estadoAtual, null, 0));

        assertEquals(TipoDecisao.PERSEGUIR_BOLA, decisao);
    }

    @Test
    void forcaPerseguirQuandoBolaFicouLivrePorMuitosTicks() {
        posicionarBola(50, 50);
        JogadorEstado estadoAtual = criarEstado(10, 10, false);
        ControladorDecisaoJogador controlador = criarControlador(85);

        TipoDecisao decisao = controlador.decidir(criarContexto(
                estadoAtual,
                null,
                ControladorDecisaoJogador.TICKS_BOLA_LIVRE_FORCAR_PERSEGUIR));

        assertEquals(TipoDecisao.PERSEGUIR_BOLA, decisao);
    }

    private ControladorDecisaoJogador criarControlador(int valorSorteado) {
        return new ControladorDecisaoJogador(new SeletorDecisaoPonderada(new RandomFixo(valorSorteado)));
    }

    private ContextoDecisao criarContexto(
            JogadorEstado estadoAtual,
            JogadorEstado jogadorComBola,
            int ticksBolaLivre) {
        return new ContextoDecisao(
                "azul",
                estadoAtual,
                jogadorComBola,
                PapelJogador.JOGADOR,
                Time.AZUL,
                PerfilTatico.equilibrado(),
                ticksBolaLivre);
    }

    private JogadorEstado criarEstado(double x, double y, boolean comBola) {
        JogadorEstado estado = new JogadorEstado();
        estado.x = x;
        estado.y = y;
        estado.comBola = comBola;
        return estado;
    }

    private void posicionarBola(double x, double y) {
        Ambiente.bola.x = x;
        Ambiente.bola.y = y;
    }

    private static class RandomFixo extends Random {
        private final int valor;

        private RandomFixo(int valor) {
            this.valor = valor;
        }

        @Override
        public int nextInt(int bound) {
            return valor;
        }
    }
}
