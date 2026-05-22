package com.futebol.colaborativo.estrategia;

import com.futebol.colaborativo.jogo.TipoDecisao;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeletorDecisaoPonderadaTest {

    @Test
    void retornaUnicaOpcaoValida() {
        Map<TipoDecisao, Integer> pesos = new LinkedHashMap<>();
        pesos.put(TipoDecisao.INTERCEPTAR, 100);

        TipoDecisao decisao = new SeletorDecisaoPonderada(new Random(1)).sortear(pesos);

        assertEquals(TipoDecisao.INTERCEPTAR, decisao);
    }

    @Test
    void ignoraPesosInvalidos() {
        Map<TipoDecisao, Integer> pesos = new LinkedHashMap<>();
        pesos.put(TipoDecisao.PERSEGUIR_BOLA, 0);
        pesos.put(TipoDecisao.AGIR_COM_BOLA, -10);
        pesos.put(TipoDecisao.INTERCEPTAR, 5);

        TipoDecisao decisao = new SeletorDecisaoPonderada(new Random(1)).sortear(pesos);

        assertEquals(TipoDecisao.INTERCEPTAR, decisao);
    }

    @Test
    void retornaManterPosicaoQuandoNaoHaOpcoesValidas() {
        Map<TipoDecisao, Integer> pesos = new LinkedHashMap<>();
        pesos.put(TipoDecisao.PERSEGUIR_BOLA, 0);
        pesos.put(TipoDecisao.AGIR_COM_BOLA, -10);

        TipoDecisao decisao = new SeletorDecisaoPonderada(new Random(1)).sortear(pesos);

        assertEquals(TipoDecisao.MANTER_POSICAO, decisao);
    }

    @Test
    void sorteiaComBaseNosPesos() {
        Map<TipoDecisao, Integer> pesos = new LinkedHashMap<>();
        pesos.put(TipoDecisao.PERSEGUIR_BOLA, 85);
        pesos.put(TipoDecisao.MANTER_POSICAO_DEFENSIVA, 15);

        TipoDecisao primeiraFaixa = new SeletorDecisaoPonderada(new RandomFixo(84)).sortear(pesos);
        TipoDecisao segundaFaixa = new SeletorDecisaoPonderada(new RandomFixo(85)).sortear(pesos);

        assertEquals(TipoDecisao.PERSEGUIR_BOLA, primeiraFaixa);
        assertEquals(TipoDecisao.MANTER_POSICAO_DEFENSIVA, segundaFaixa);
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
