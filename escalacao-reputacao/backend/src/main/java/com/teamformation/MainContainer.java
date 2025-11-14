package com.teamformation;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainContainer {

    public static void main(String[] args) {

        try {
            // Inicializa o runtime JADE
            Runtime rt = Runtime.instance();

            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true"); // habilita a RMA igual ao IntelliJ

            AgentContainer container = rt.createMainContainer(p);

            /*
             * Aqui criamos os agentes já na inicialização,
             * igual você fazia com:
             *
             * -gui coach:examples.TeamFormation.CoachAgent;player1:examples.TeamFormation.PlayerAgent
             */

            AgentController coach = container.createNewAgent(
                    "coach",
                    "com.teamformation.agents.CoachAgent",
                    null
            );
            coach.start();

            AgentController player1 = container.createNewAgent(
                    "player1",
                    "com.teamformation.agents.PlayerAgent",
                    null
            );
            player1.start();

            AgentController player2 = container.createNewAgent(
                    "player2",
                    "com.teamformation.agents.PlayerAgent",
                    null
            );
            player2.start();

            System.out.println("MainContainer iniciado com Coach + Players!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
