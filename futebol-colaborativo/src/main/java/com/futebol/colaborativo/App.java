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
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true"); // Ativa a GUI do JADE
            
            // Criar o container principal
            AgentContainer mainContainer = runtime.createMainContainer(profile);
            
            // Jogador 1
             AgentController jogador = mainContainer.createNewAgent(
                "jogador1",
                "com.futebol.colaborativo.agentes.JogadorAgent",
                null
            );
            
            // Inicia o agente
            jogador.start();
            
            System.out.println("Sistema Multi-Agente de Futebol iniciado com sucesso!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
