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
    private double xInicial;
    private double yInicial;
    private double golX;
    private double alvoInterceptacaoX;
    private double alvoInterceptacaoY;
    private boolean temAlvoInterceptacao = false;

    @Override
    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            sistema = (SistemaFutebol) args[0];
            xInicial = ((Number) args[1]).doubleValue();
            yInicial = ((Number) args[2]).doubleValue();
            golX = ((Number) args[3]).doubleValue();
        }

        estado = new JogadorEstado();
        estado.x = xInicial;
        estado.y = yInicial;
        estado.golX = golX;

        addBehaviour(new TickerBehaviour(this, 250) {

            @Override
            protected void onTick() {

                JogadorEstado algumJogadorComBola = sistema.getJogadorComBola();

                if (algumJogadorComBola == null) {
                    temAlvoInterceptacao = false;
                    perseguirBola();

                } else if (estado.comBola) {
                    temAlvoInterceptacao = false;
                    conduzirAteOGol();

                } else {

                    if (!temAlvoInterceptacao) {
                        double[] ponto = calcularPontoIntercepcao(algumJogadorComBola);

                        alvoInterceptacaoX = ponto[0];
                        alvoInterceptacaoY = ponto[1];

                        temAlvoInterceptacao = true;

                        System.out.printf(
                            "[%s] alvo fixado em (%.1f, %.1f)%n",
                            getLocalName(), alvoInterceptacaoX, alvoInterceptacaoY
                        );
                    }

                    irParaPontoInterceptacao();
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

        String direcao = calcularDirecao(golX, Ambiente.golY);

        // DEBUG DO CAMINHO
        System.out.println("[" + getLocalName() + "] indo para o gol -> direção: " + direcao +
            " | atual: (" + estado.x + ", " + estado.y + ")" +
            " | alvo: (" + golX + ", " + Ambiente.golY + ")");

        Movimento.conduzirBola(estado, direcao);

        if (chegouNoGol()) {
            sistema.registrarGol(getLocalName());
        }
    }

    private boolean tocouNaBola() {
        return estado.x == Ambiente.bola.x && estado.y == Ambiente.bola.y;
    }

    private boolean chegouNoGol() {
        return estado.x == golX && estado.y == Ambiente.golY;
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

    private double[] calcularPontoIntercepcao(JogadorEstado alvo) {

        double posicaoJogadorSimuladoX = alvo.x;
        double posicaoJogadorSimuladoY = alvo.y;

        double golAlvoDoJogadorSimulado = alvo.golX;

        int passo = 0;

        while (posicaoJogadorSimuladoX != golAlvoDoJogadorSimulado || posicaoJogadorSimuladoY != Ambiente.golY) {

            double deltaX = golAlvoDoJogadorSimulado - posicaoJogadorSimuladoX;
            double deltaY = Ambiente.golY - posicaoJogadorSimuladoY;

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                posicaoJogadorSimuladoX += Math.signum(deltaX);
            } else {
                posicaoJogadorSimuladoY += Math.signum(deltaY);
            }

            passo++;

            // tempo do vermelho até esse ponto
            double distVermelho =
                Math.abs(posicaoJogadorSimuladoX - estado.x) +
                Math.abs(posicaoJogadorSimuladoY - estado.y);

            // condição de interceptação
            if (distVermelho <= passo) {
                return new double[]{ posicaoJogadorSimuladoX, posicaoJogadorSimuladoY };
            }

            if (passo > 200) break;
        }

        // fallback: segue direto o alvo
        return new double[]{ alvo.x, alvo.y };
    }
    
    private void irParaPontoInterceptacao() {

        String direcao = calcularDirecao(alvoInterceptacaoX, alvoInterceptacaoY);

        Movimento.moverGrid(estado, direcao);
    }
}
