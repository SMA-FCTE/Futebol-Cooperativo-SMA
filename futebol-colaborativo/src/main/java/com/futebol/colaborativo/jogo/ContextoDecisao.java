package com.futebol.colaborativo.jogo;

import com.futebol.colaborativo.estrategia.PerfilTatico;
import com.futebol.colaborativo.model.JogadorEstado;

import java.util.Objects;

public class ContextoDecisao {

    private final String nomeJogador;
    private final JogadorEstado estadoAtual;
    private final JogadorEstado jogadorComBola;
    private final PapelJogador papel;
    private final Time time;
    private final PerfilTatico perfilTatico;
    private final int ticksBolaLivre;

    public ContextoDecisao(
            String nomeJogador,
            JogadorEstado estadoAtual,
            JogadorEstado jogadorComBola,
            PapelJogador papel,
            Time time,
            PerfilTatico perfilTatico,
            int ticksBolaLivre) {
        this.nomeJogador = Objects.requireNonNull(nomeJogador, "nomeJogador");
        this.estadoAtual = Objects.requireNonNull(estadoAtual, "estadoAtual");
        this.jogadorComBola = jogadorComBola;
        this.papel = Objects.requireNonNull(papel, "papel");
        this.time = Objects.requireNonNull(time, "time");
        this.perfilTatico = Objects.requireNonNull(perfilTatico, "perfilTatico");
        this.ticksBolaLivre = Math.max(0, ticksBolaLivre);
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public JogadorEstado getEstadoAtual() {
        return estadoAtual;
    }

    public JogadorEstado getJogadorComBola() {
        return jogadorComBola;
    }

    public PapelJogador getPapel() {
        return papel;
    }

    public Time getTime() {
        return time;
    }

    public PerfilTatico getPerfilTatico() {
        return perfilTatico;
    }

    public int getTicksBolaLivre() {
        return ticksBolaLivre;
    }

    public boolean existeJogadorComBola() {
        return jogadorComBola != null;
    }

    public boolean jogadorAtualEstaComBola() {
        return estadoAtual.comBola;
    }

    public boolean bolaEstaLivre() {
        return !existeJogadorComBola();
    }
}
