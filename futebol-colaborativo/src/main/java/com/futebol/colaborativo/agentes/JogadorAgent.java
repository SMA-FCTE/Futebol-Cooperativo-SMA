package com.futebol.colaborativo.agentes;

import com.futebol.colaborativo.SistemaFutebol;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;
import com.futebol.colaborativo.movimento.Movimento;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class JogadorAgent extends Agent {

    private JogadorEstado estado;
    private SistemaFutebol sistema;

    @Override
    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            sistema = (SistemaFutebol) args[0];
        }

        estado = new JogadorEstado();
        estado.x = 10;
        estado.y = 10;

        addBehaviour(new TickerBehaviour(this, 500) {

            @Override
            protected void onTick() {

                if (!estado.comBola) {
                    perseguirBola();
                } else {
                    conduzirAteOGol();
                }

                if (sistema != null) {
                    sistema.atualizarEstado(getLocalName(), estado);
                }

                System.out.printf(
                    "Agente: %s | Pos: (%.2f, %.2f) | Bola: (%.2f, %.2f) | Com bola: %s%n",
                    getLocalName(),
                    estado.x, estado.y,
                    Ambiente.bola.x, Ambiente.bola.y,
                    estado.comBola
                );
            }
        });
    }

    private void perseguirBola() {
        String direcao = calcularDirecao(Ambiente.bola.x, Ambiente.bola.y);
        Movimento.moverGrid(estado, direcao);

        if (tocouNaBola()) {
            estado.comBola = true;
            Ambiente.bola.x = estado.x;
            Ambiente.bola.y = estado.y;
        }
    }

    private void conduzirAteOGol() {
        String direcao = calcularDirecao(Ambiente.golX, Ambiente.golY);
        Movimento.conduzirBola(estado, direcao);

        if (chegouNoGol()) {
            System.out.println(getLocalName() + " marcou um gol!");

            estado.comBola = false;

            // reposiciona bola no centro
            Ambiente.bola.x = Ambiente.largura / 2;
            Ambiente.bola.y = Ambiente.altura / 2;

            // reposiciona jogador
            estado.x = 10;
            estado.y = 10;
        }
    }

    private boolean tocouNaBola() {
        return estado.x == Ambiente.bola.x && estado.y == Ambiente.bola.y;
    }

    private boolean chegouNoGol() {
        return estado.x == Ambiente.golX && estado.y == Ambiente.golY;
    }

    private String calcularDirecao(double alvoX, double alvoY) {
        double deltaX = alvoX - estado.x;
        double deltaY = alvoY - estado.y;

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX > 0 ? "DIREITA" : "ESQUERDA";
        } else {
            return deltaY > 0 ? "BAIXO" : "CIMA";
        }
    }
}