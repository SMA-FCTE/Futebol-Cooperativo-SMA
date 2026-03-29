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

import java.util.HashMap;
import java.util.Map;

import java.util.List;         // ADICIONE ESTA LINHA
import java.util.ArrayList;    // ADICIONE ESTA LINHA

public class CoachAgent extends Agent {

    private Map<String, PlayerInfo> bestPlayers = new HashMap<>();

    
    // ======================= MUDANÇA 1 ============================
    // AGORA GUARDAMOS TODAS AS PROPOSTAS E O TOTAL ESPERADO
    private List<PlayerInfo> propostasRecebidas = new ArrayList<>();
    private int totalEsperado = 0;
    // ==============================================================

    @Override
    protected void setup() {
        System.out.println("Coach-Agent " + getAID().getLocalName() + " iniciado.");

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
                        totalEsperado = result.length;  // <<<==================== MUDANÇA 2
                        // GUARDAR QUANTOS PROPOSES PRECISAMOS RECEBER

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

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {

                    System.out.println("Coach recebeu PROPOSE de "
                        + msg.getSender().getLocalName() + ": " + msg.getContent());

                    String content = msg.getContent();
                    String[] parts = content.split(";");
                    String posicao = parts[1].split("=")[1];
                    double reputacao = Double.parseDouble(parts[2].split("=")[1]);

                    PlayerInfo novo = new PlayerInfo(msg.getSender(), posicao, reputacao);

                    // ======================= MUDANÇA 3 ============================
                    // AGORA SOMENTE ARMAZENAMOS A PROPOSTA — SEM DECIDIR AINDA
                    propostasRecebidas.add(novo);
                    // ==============================================================

                    // ======================= MUDANÇA 4 ============================
                    // QUANDO RECEBER TODAS AS PROPOSTAS → AÍ SIM SELECIONA OS MELHORES
                    if (propostasRecebidas.size() == totalEsperado) {

                        System.out.println("\nTODAS AS PROPOSTAS RECEBIDAS! INICIANDO SELEÇÃO...\n");

                        // PROCESSA TODAS AS PROPOSTAS DE UMA VEZ
                        for (PlayerInfo p : propostasRecebidas) {

                            if (!bestPlayers.containsKey(p.posicao)) {
                                bestPlayers.put(p.posicao, p);
                            } else {
                                PlayerInfo atual = bestPlayers.get(p.posicao);
                                if (p.reputacao > atual.reputacao) {
                                    bestPlayers.put(p.posicao, p);
                                }
                            }
                        }

                        // AGORA QUE ESCOLHEU OS MELHORES — RESPONDE ACEITANDO OU REJEITANDO
                        for (PlayerInfo p : propostasRecebidas) {
                            ACLMessage reply = new ACLMessage(
                                    p.equals(bestPlayers.get(p.posicao))
                                            ? ACLMessage.ACCEPT_PROPOSAL
                                            : ACLMessage.REJECT_PROPOSAL
                            );

                            reply.addReceiver(p.id);

                            if (p.equals(bestPlayers.get(p.posicao))) {
                                reply.setContent("Parabéns! Você foi escalado como " + p.posicao + "!");
                            } else {
                                reply.setContent("Você NÃO foi escalado para a posição " + p.posicao + ".");
                            }

                            send(reply);
                        }

                        System.out.println("\nSELEÇÃO FINALIZADA!\n");
                    }
                    // ==============================================================

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

    private static class PlayerInfo {
        AID id;
        String posicao;
        double reputacao;

        PlayerInfo(AID id, String posicao, double reputacao) {
            this.id = id;
            this.posicao = posicao;
            this.reputacao = reputacao;
        }
    }
}
