package com.futebol.colaborativo.movimento;

import com.futebol.colaborativo.model.JogadorEstado;

public class Movimento {

    // Grid (provisório)
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

    // // Contínuo ( para o futuro)
    // public static void moverContinuo(JogadorEstado estado) {
    //     estado.x += estado.velocidade * Math.cos(estado.theta);
    //     estado.y += estado.velocidade * Math.sin(estado.theta);
    // }
}
