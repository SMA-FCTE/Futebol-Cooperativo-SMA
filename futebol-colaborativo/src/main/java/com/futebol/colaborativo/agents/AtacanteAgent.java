package com.futebol.colaborativo.agents;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

public class AtacanteAgent extends Agent {

    private String time;

    protected void setup() {
        // identifica o time
        Object[] args = getArguments();
        if (args == null || args.length == 0) {
            System.out.println(getLocalName() + ": erro - é necessário informar o argumento 'A' ou 'B' para indicar o time.");
            doDelete();
            return;
        }

        time = (String) args[0];
        System.out.println(getLocalName() + " criado para o time " + time + ".");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                receberMensagens(this);
            }
        });
    }

    private void receberMensagens(CyclicBehaviour behaviour) {
        ACLMessage mensagem = receive();

        if (mensagem != null) {
            String conteudo = mensagem.getContent();

            if (conteudo.startsWith("passe")) {
                String timeDoPasse = conteudo.split("=")[1];
                if (timeDoPasse.equalsIgnoreCase(time)) {
                    processarPasse(false);
                }
            } else if (conteudo.startsWith("roubo")) {
                String timeDoRoubo = conteudo.split("=")[1];
                if (timeDoRoubo.equalsIgnoreCase(time)) {
                    System.out.println("Atacante " + time + ": roubou a bola do zagueiro adversário!");
                    processarPasse(true);
                }
            }
        } else {
            behaviour.block();
        }
    }

    private void processarPasse(boolean aposRoubo) {
        if (!aposRoubo)
            System.out.println("Atacante " + time + ": recebeu passe do meio-campo.");
        System.out.println("Atacante " + time + ": preparando o chute...");

        try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }

        int chance = (int) (Math.random() * 101);
        String timeAdversario = time.equalsIgnoreCase("A") ? "B" : "A";

        if (chance <= 50) {
            System.out.println("Atacante " + time + ": CHUTOU E É GOOOOOOOL!!! (50% chance)");
            enviarChute(timeAdversario, "resultado=gol");
        } else {
            System.out.println("Atacante " + time + ": chutou... mas o goleiro defendeu!");
            enviarChute(timeAdversario, "resultado=errou");
        }
    }

    private void enviarChute(String timeAdversario, String resultado) {
        try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }

        ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
        mensagem.addReceiver(new AID("goleiro" + timeAdversario, AID.ISLOCALNAME));
        mensagem.setContent("chute;time=" + time + ";" + resultado);
        send(mensagem);

        System.out.println("Atacante " + time + ": chute enviado ao goleiro" + timeAdversario + ".");
    }
}
