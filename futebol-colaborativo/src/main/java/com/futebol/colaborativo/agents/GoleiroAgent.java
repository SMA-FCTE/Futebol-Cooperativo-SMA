package com.futebol.colaborativo.agents;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

public class GoleiroAgent extends Agent {

    private String time;

    protected void setup() {
    	
        // Lê o argumento do time
        Object[] args = getArguments();
        if (args == null || args.length == 0) {
            System.out.println(getLocalName() + ": erro - é necessário informar o argumento 'A' ou 'B' para indicar o time.");
            doDelete();
            return;
        }

        // Armazena o time dele ("A" ou "B")
        time = (String) args[0];
        System.out.println(getLocalName() + " criado para o time " + time + ".");

        // Goleiro "A" inicia o jogo
        if (time.equalsIgnoreCase("A")) {
            addBehaviour(new OneShotBehaviour() {
                public void action() {
                    iniciarJogada();
                }
            });
        }
        
        // Todos os goleiros escutam chutes dos adversários
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                receberMensagens(this);
            }
        });
    }
    
    // FUNÇÕES -----------------------------------------------------------------

    // Lógica inicial do goleiro A — espera 10 segundos e envia o primeiro passe 
    private void iniciarJogada() {
        try {
            Thread.sleep(10000); // 10 segundos de espera
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Goleiro " + time + " iniciando a jogada...");
        
        ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
        mensagem.addReceiver(new AID("zagueiro" + time, AID.ISLOCALNAME));
        mensagem.setContent("passe;time=" + time);
        send(mensagem);

        System.out.println("Goleiro " + time + ": passe enviado para o zagueiro" + time + "!");
    }

    // Lida com as mensagens recebidas (chutes, gols, defesas)
    private void receberMensagens(CyclicBehaviour behaviour) {
        ACLMessage mensagem = receive();
        
        if (mensagem != null) {
            String conteudo = mensagem.getContent();

            if (conteudo.startsWith("chute")) {
                String timeDoChute = conteudo.split("=")[1];
                boolean adversario = !timeDoChute.equalsIgnoreCase(time);

                if (adversario) {
                    if (conteudo.contains("resultado=gol")) {
                        System.out.println("Goleiro " + time + ": GOL do time adversário!");
                        System.out.println("Goleiro " + time + ": se preparando para reiniciar a jogada...");
                        try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }
                        enviarPasse();
                    } else if (conteudo.contains("resultado=errou")) {
                        System.out.println("Goleiro " + time + ": defendeu o chute!");
                        System.out.println("Goleiro " + time + ": reiniciando a jogada...");
                        try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }
                        enviarPasse();
                    }
                }
            }
        } else {
            behaviour.block();
        }
    }

    // Envia um passe para o zagueiro do mesmo time 
    private void enviarPasse() {
        ACLMessage resposta = new ACLMessage(ACLMessage.INFORM);
        resposta.addReceiver(new AID("zagueiro" + time, AID.ISLOCALNAME));
        resposta.setContent("passe;time=" + time);
        send(resposta);

        System.out.println("Goleiro " + time + ": passe enviado para o zagueiro" + time + "!");
    }
}
