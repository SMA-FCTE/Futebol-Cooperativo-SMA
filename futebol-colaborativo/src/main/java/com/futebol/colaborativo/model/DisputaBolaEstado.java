package com.futebol.colaborativo.model;

public class DisputaBolaEstado {
    public String id;
    public int rodada;
    public String jogador1;
    public String jogador2;
    public String jogada1;
    public String jogada2;
    public String vencedor;
    public boolean empate;
    public String resultado;

    public DisputaBolaEstado(
        String id,
        int rodada,
        String jogador1,
        String jogador2,
        String jogada1,
        String jogada2,
        String vencedor,
        boolean empate,
        String resultado
    ) {
        this.id = id;
        this.rodada = rodada;
        this.jogador1 = jogador1;
        this.jogador2 = jogador2;
        this.jogada1 = jogada1;
        this.jogada2 = jogada2;
        this.vencedor = vencedor;
        this.empate = empate;
        this.resultado = resultado;
    }
}
