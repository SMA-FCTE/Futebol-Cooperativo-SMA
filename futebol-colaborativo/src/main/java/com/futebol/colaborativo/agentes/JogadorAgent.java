package com.futebol.colaborativo.agentes;

import com.futebol.colaborativo.SistemaFutebol;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;
import com.futebol.colaborativo.movimento.Movimento;

import jade.core.*;
import jade.core.behaviours.*;
// import jade.lang.acl.ACLMessage;

public class JogadorAgent extends Agent {

    private JogadorEstado estado;
    private SistemaFutebol sistema;

    protected void setup() {

        // receber sistema
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            sistema = (SistemaFutebol) args[0];
        }

        // Posição inicial
        estado = new JogadorEstado();
        estado.x = 10;
        estado.y = 10;

        addBehaviour(new TickerBehaviour(this, 500) {

            protected void onTick() {

                // Verificar posição da bola
                double deltaX = Ambiente.bola.x - estado.x;
                double deltaY = Ambiente.bola.y - estado.y;

                // Decisão
                String direcao;

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    direcao = deltaX > 0 ? "DIREITA" : "ESQUERDA";
                } else {
                    direcao = deltaY > 0 ? "BAIXO" : "CIMA";
                }

                // Movimento
                Movimento.moverGrid(estado, direcao);

                if (sistema != null) {
                    sistema.atualizarEstado(getLocalName(), estado);
                }

                // Log terminal
                System.out.printf(
                    "Agente: %s | Pos: (%.2f, %.2f) | Bola: (%.2f, %.2f)%n",
                    getLocalName(),
                    estado.x, estado.y,
                    Ambiente.bola.x, Ambiente.bola.y
                );
            }
        });
    }
}