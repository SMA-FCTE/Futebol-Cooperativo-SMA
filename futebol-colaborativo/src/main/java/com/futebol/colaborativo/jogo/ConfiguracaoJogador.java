package com.futebol.colaborativo.jogo;

import java.util.Objects;

public class ConfiguracaoJogador {

    private final String nome;
    private final double xInicial;
    private final double yInicial;
    private final double golX;
    private final Time time;
    private final PapelJogador papel;

    public ConfiguracaoJogador(
            String nome,
            double xInicial,
            double yInicial,
            double golX,
            Time time,
            PapelJogador papel) {
        this.nome = Objects.requireNonNull(nome, "nome");
        this.xInicial = xInicial;
        this.yInicial = yInicial;
        this.golX = golX;
        this.time = Objects.requireNonNull(time, "time");
        this.papel = Objects.requireNonNull(papel, "papel");
    }

    public String getNome() {
        return nome;
    }

    public double getXInicial() {
        return xInicial;
    }

    public double getYInicial() {
        return yInicial;
    }

    public double getGolX() {
        return golX;
    }

    public Time getTime() {
        return time;
    }

    public PapelJogador getPapel() {
        return papel;
    }
}
