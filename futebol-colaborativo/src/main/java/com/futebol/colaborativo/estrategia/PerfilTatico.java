package com.futebol.colaborativo.estrategia;

import com.futebol.colaborativo.jogo.PapelJogador;
import com.futebol.colaborativo.jogo.TipoDecisao;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class PerfilTatico {

    private final PapelJogador papel;
    private final Map<TipoDecisao, Integer> pesosBolaLivre;
    private final Map<TipoDecisao, Integer> pesosOutroJogadorComBola;
    private final Map<TipoDecisao, Integer> pesosComBola;

    private PerfilTatico(
            PapelJogador papel,
            Map<TipoDecisao, Integer> pesosBolaLivre,
            Map<TipoDecisao, Integer> pesosOutroJogadorComBola,
            Map<TipoDecisao, Integer> pesosComBola) {
        this.papel = Objects.requireNonNull(papel, "papel");
        this.pesosBolaLivre = copiarPesos(pesosBolaLivre);
        this.pesosOutroJogadorComBola = copiarPesos(pesosOutroJogadorComBola);
        this.pesosComBola = copiarPesos(pesosComBola);
    }

    public static PerfilTatico equilibrado() {
        Map<TipoDecisao, Integer> bolaLivre = new EnumMap<>(TipoDecisao.class);
        bolaLivre.put(TipoDecisao.PERSEGUIR_BOLA, 40);
        bolaLivre.put(TipoDecisao.MANTER_POSICAO_DEFENSIVA, 60);

        Map<TipoDecisao, Integer> outroJogadorComBola = new EnumMap<>(TipoDecisao.class);
        outroJogadorComBola.put(TipoDecisao.INTERCEPTAR, 100);

        Map<TipoDecisao, Integer> comBola = new EnumMap<>(TipoDecisao.class);
        comBola.put(TipoDecisao.AGIR_COM_BOLA, 100);

        return new PerfilTatico(PapelJogador.JOGADOR, bolaLivre, outroJogadorComBola, comBola);
    }

    public PapelJogador getPapel() {
        return papel;
    }

    public Map<TipoDecisao, Integer> getPesosBolaLivre() {
        return pesosBolaLivre;
    }

    public Map<TipoDecisao, Integer> getPesosOutroJogadorComBola() {
        return pesosOutroJogadorComBola;
    }

    public Map<TipoDecisao, Integer> getPesosComBola() {
        return pesosComBola;
    }

    private static Map<TipoDecisao, Integer> copiarPesos(Map<TipoDecisao, Integer> pesos) {
        Map<TipoDecisao, Integer> copia = new EnumMap<>(TipoDecisao.class);

        if (pesos != null) {
            for (Map.Entry<TipoDecisao, Integer> entrada : pesos.entrySet()) {
                if (entrada.getKey() != null && entrada.getValue() != null) {
                    copia.put(entrada.getKey(), entrada.getValue());
                }
            }
        }

        return Collections.unmodifiableMap(copia);
    }
}
