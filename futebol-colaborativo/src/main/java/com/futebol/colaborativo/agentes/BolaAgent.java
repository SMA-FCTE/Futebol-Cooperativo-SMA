package com.futebol.colaborativo.agentes;

import com.futebol.colaborativo.SistemaFutebol;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BolaAgent extends Agent {

    private SistemaFutebol sistema;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 1) {
            sistema = (SistemaFutebol) args[0];
        }

        System.out.println("A bola esta em campo!");

        addBehaviour(new TickerBehaviour(this, 16) {
            @Override
            protected void onTick() {
                ouvirChutes();

                if (sistema != null) {
                    sistema.aplicarFisicaBola();
                }
            }
        });
    }

    private void ouvirChutes() {
        MessageTemplate mensagemTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage mensagem;

        while ((mensagem = receive(mensagemTemplate)) != null) {
            double[] forca = parseForca(mensagem.getContent());
            if (forca == null || sistema == null) {
                continue;
            }

            sistema.chutarBola(mensagem.getSender().getLocalName(), forca[0], forca[1]);
        }
    }

    private double[] parseForca(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            return null;
        }

        String[] partes = conteudo.split(",");
        if (partes.length != 2) {
            return null;
        }

        try {
            return new double[] {
                Double.parseDouble(partes[0].trim()),
                Double.parseDouble(partes[1].trim())
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
