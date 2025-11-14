package com.teamformation.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class CoachAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Coach-Agent " + getAID().getLocalName() + " iniciado.");

        // Registro no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("coach");
        sd.setName("JADE-team-formation");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Primeiro comportamento: esperar jogadores e enviar CFP
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    System.out.println("Aguardando jogadores se registrarem...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }

                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("player");
                    template.addServices(sd);
                    DFAgentDescription[] result = DFService.search(myAgent, template);

                    if (result.length > 0) {
                        System.out.println("Encontrados " + result.length + " jogadores");
                        AID[] players = new AID[result.length];

                        for (int i = 0; i < result.length; i++) {
                            players[i] = result[i].getName();
                            System.out.println("Jogador: " + players[i].getLocalName());
                        }

                        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                        for (AID player : players) {
                            cfp.addReceiver(player);
                        }

                        cfp.setContent("Início da escalação. Envie sua proposta.");
                        cfp.setConversationId("team-formation");
                        cfp.setReplyWith("cfp" + System.currentTimeMillis());

                        myAgent.send(cfp);

                        System.out.println("Mensagem CFP enviada.");
                    } else {
                        System.out.println("Nenhum jogador encontrado.");
                    }

                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        // Segundo comportamento: receber PROPOSE dos jogadores
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    System.out.println("Coach recebeu PROPOSE de "
                            + msg.getSender().getLocalName() + ": " + msg.getContent());

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    reply.setContent("Parabéns, você foi escalado!");

                    myAgent.send(reply);

                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("Coach-Agent " + getAID().getLocalName() + " encerrado.");
    }
}
