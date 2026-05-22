package com.futebol.colaborativo.estrategia;

import com.futebol.colaborativo.jogo.TipoDecisao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class SeletorDecisaoPonderada {

    private final Random random;

    public SeletorDecisaoPonderada() {
        this(new Random());
    }

    public SeletorDecisaoPonderada(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public TipoDecisao sortear(Map<TipoDecisao, Integer> pesos) {
        Map<TipoDecisao, Integer> opcoesValidas = filtrarOpcoesValidas(pesos);

        if (opcoesValidas.isEmpty()) {
            return TipoDecisao.MANTER_POSICAO;
        }

        if (opcoesValidas.size() == 1) {
            return opcoesValidas.keySet().iterator().next();
        }

        int pesoTotal = calcularPesoTotal(opcoesValidas);
        int valorSorteado = random.nextInt(pesoTotal);
        int acumulado = 0;

        for (Map.Entry<TipoDecisao, Integer> opcao : opcoesValidas.entrySet()) {
            acumulado += opcao.getValue();

            if (valorSorteado < acumulado) {
                return opcao.getKey();
            }
        }

        return TipoDecisao.MANTER_POSICAO;
    }

    private Map<TipoDecisao, Integer> filtrarOpcoesValidas(Map<TipoDecisao, Integer> pesos) {
        Map<TipoDecisao, Integer> opcoesValidas = new LinkedHashMap<>();

        if (pesos == null) {
            return opcoesValidas;
        }

        for (Map.Entry<TipoDecisao, Integer> opcao : pesos.entrySet()) {
            Integer peso = opcao.getValue();

            if (opcao.getKey() != null && peso != null && peso > 0) {
                opcoesValidas.put(opcao.getKey(), peso);
            }
        }

        return opcoesValidas;
    }

    private int calcularPesoTotal(Map<TipoDecisao, Integer> pesos) {
        int pesoTotal = 0;

        for (Integer peso : pesos.values()) {
            pesoTotal += peso;
        }

        return pesoTotal;
    }
}
