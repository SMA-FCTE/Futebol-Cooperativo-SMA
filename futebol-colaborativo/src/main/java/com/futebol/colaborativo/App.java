package com.futebol.colaborativo;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class App {
    public static void main(String[] args) {
        try {
            // Configurar o JADE Runtime
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true"); // Ativa a GUI do JADE
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            
            // Criar o container principal
            AgentContainer mainContainer = rt.createMainContainer(profile);
            
            // Time A
            AgentController goleiroA = mainContainer.createNewAgent(
                "goleiroA", 
                "com.futebol.colaborativo.agents.GoleiroAgent", 
                new Object[]{"A"}
            );
            
            AgentController zagueiroA = mainContainer.createNewAgent(
                "zagueiroA", 
                "com.futebol.colaborativo.agents.ZagueiroAgent", 
                new Object[]{"A"}
            );
            
            AgentController meiaA = mainContainer.createNewAgent(
                "meiaA", 
                "com.futebol.colaborativo.agents.MeiaAgent", 
                new Object[]{"A"}
            );
            
            AgentController atacanteA = mainContainer.createNewAgent(
                "atacanteA", 
                "com.futebol.colaborativo.agents.AtacanteAgent", 
                new Object[]{"A"}
            );
            
            // Time B
            AgentController goleiroB = mainContainer.createNewAgent(
                "goleiroB", 
                "com.futebol.colaborativo.agents.GoleiroAgent", 
                new Object[]{"B"}
            );
            
            AgentController zagueiroB = mainContainer.createNewAgent(
                "zagueiroB", 
                "com.futebol.colaborativo.agents.ZagueiroAgent", 
                new Object[]{"B"}
            );
            
            AgentController meiaB = mainContainer.createNewAgent(
                "meiaB", 
                "com.futebol.colaborativo.agents.MeiaAgent", 
                new Object[]{"B"}
            );
            
            AgentController atacanteB = mainContainer.createNewAgent(
                "atacanteB", 
                "com.futebol.colaborativo.agents.AtacanteAgent", 
                new Object[]{"B"}
            );
            
            // Iniciar todos os agentes
            goleiroA.start();
            zagueiroA.start();
            meiaA.start();
            atacanteA.start();
            goleiroB.start();
            zagueiroB.start();
            meiaB.start();
            atacanteB.start();
            
            System.out.println("Sistema Multi-Agente de Futebol iniciado com sucesso!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
