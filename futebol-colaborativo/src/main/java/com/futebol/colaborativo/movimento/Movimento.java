package com.futebol.colaborativo.movimento;

import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;

public class Movimento {

    // Grid computacional (Eixo Y é positivo para baixo e negativo para cima)
    public static void moverGrid(JogadorEstado estado, String direcao) {

        switch (direcao) {
            case "CIMA":
                estado.y -= 1;
                break;

            case "BAIXO":
                estado.y += 1;
                break;

            case "DIREITA":
                estado.x += 1;
                break;

            case "ESQUERDA":
                estado.x -= 1;
                break;
        }
    }

    public static void conduzirBola(JogadorEstado estado, String direcao) {
        moverGrid(estado, direcao);

        Ambiente.bola.x = estado.x;
        Ambiente.bola.y = estado.y;
    }

}
