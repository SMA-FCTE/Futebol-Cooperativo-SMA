package com.teamformation;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainContainer {

    public static void main(String[] args) {

        try {
            Runtime rt = Runtime.instance();

            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true");

            AgentContainer container = rt.createMainContainer(p);

            // ===================================================================
            // ===================== MUDANÇA 1 ==================================
            // CRIA APENAS O COACH, MAS AGORA CRIAMOS VÁRIOS JOGADORES FIXOS
            // USANDO NOMES QUE O PlayerAgent RECONHECE PARA ATRIBUTOS AUTOMÁTICOS
            // ===================================================================

            AgentController coach = container.createNewAgent(
                    "coach",
                    "com.teamformation.agents.CoachAgent",
                    null
            );
            coach.start();


            // ===================== MUDANÇA 2 ================================
            // JOGADORES PRÉ-CADASTRADOS PARA TESTES SEM GUI
            // Esses nomes ativam o cadastro automático no PlayerAgent
            // ================================================================

            String[][] jogadores = {
                    {"goleiro",    "com.teamformation.agents.PlayerAgent"},
                    {"zagueiro1",  "com.teamformation.agents.PlayerAgent"},
                    {"zagueiro2",  "com.teamformation.agents.PlayerAgent"},
                    {"volante",    "com.teamformation.agents.PlayerAgent"},
                    {"meia",       "com.teamformation.agents.PlayerAgent"},
                    {"atacante1",  "com.teamformation.agents.PlayerAgent"},
                    {"atacante2",  "com.teamformation.agents.PlayerAgent"}
            };

            // LOOP QUE CRIA TODOS OS JOGADORES
            for (String[] info : jogadores) {
                AgentController ag = container.createNewAgent(
                        info[0],   // nome do agente
                        info[1],   // classe PlayerAgent
                        null
                );
                ag.start();
            }

            // ===================================================================
            // ===================== FIM DAS MUDANÇAS =============================
            // ===================================================================

            System.out.println("\nMainContainer iniciado com Coach + Jogadores!\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
