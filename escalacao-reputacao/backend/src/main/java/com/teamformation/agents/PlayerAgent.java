package com.teamformation.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.Hashtable;

public class PlayerAgent extends Agent {

    private Hashtable<String, String> attributes;
    private PlayerGui myGui;

    @Override
    protected void setup() {

        // Informações iniciais
        attributes = new Hashtable<>();
        
        // ======================= ALTERAÇÃO 1 =======================
        // CADASTRO AUTOMÁTICO DE ATRIBUTOS BASEADO NO NOME DO AGENTE
        String nome = getLocalName().toLowerCase();

        if (nome.contains("goleiro")) {
            attributes.put("posicao", "Goleiro");
            attributes.put("reputacao", "8.5");

        } else if (nome.contains("zagueiro1")) {
            attributes.put("posicao", "Zagueiro");
            attributes.put("reputacao", "6.0");

        } else if (nome.contains("zagueiro2")) {
            attributes.put("posicao", "Zagueiro");
            attributes.put("reputacao", "8.2"); // MAIS ALTO → DEVE GANHAR

        } else if (nome.contains("volante")) {
            attributes.put("posicao", "Volante");
            attributes.put("reputacao", "7.1");

        } else if (nome.contains("meia")) {
            attributes.put("posicao", "Meia");
            attributes.put("reputacao", "7.8");

        } else if (nome.contains("atacante1")) {
            attributes.put("posicao", "Atacante");
            attributes.put("reputacao", "7.0");

        } else if (nome.contains("atacante2")) {
            attributes.put("posicao", "Atacante");
            attributes.put("reputacao", "9.1"); // MELHOR ATACANTE

        } else {
            // VALOR PADRÃO CASO NÃO CAIA EM NENHUM IF
            attributes.put("posicao", "Atacante");
            attributes.put("reputacao", "5.0");
        }
        // ======================= FIM DA ALTERAÇÃO ==================

        // GUI simples do player
        myGui = new PlayerGui(this);
        myGui.showGui();

        // Registro no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("player");
        sd.setName("JADE-team-formation");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("Player-agent " + getLocalName() + " registrado!");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Receber CFP do técnico
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    System.out.println(getLocalName() + " recebeu CFP: " + msg.getContent());

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);

                    String content = "jogador=" + getLocalName()
                            + ";posicao=" + attributes.get("posicao")
                            + ";reputacao=" + attributes.get("reputacao");

                    reply.setContent(content);

                    myAgent.send(reply);

                } else {
                    block();
                }
            }
        });

        // Receber ACCEPT_PROPOSAL
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    System.out.println(getLocalName() + " escalado! -> " + msg.getContent());
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
        System.out.println("Player-agent " + getLocalName() + " encerrado.");
    }

    public void updateAttributes(final String posicao, final String reputacao) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                attributes.put("posicao", posicao);
                attributes.put("reputacao", reputacao);
                System.out.println(getLocalName() + " atualizou: posição=" + posicao + " reputação=" + reputacao);
            }
        });
    }
}
