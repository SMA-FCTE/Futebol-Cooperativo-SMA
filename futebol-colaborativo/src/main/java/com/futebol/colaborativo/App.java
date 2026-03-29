package com.futebol.colaborativo;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;

import com.futebol.colaborativo.api.ApiServer;
import com.futebol.colaborativo.api.EventSocket;

public class App {

    public static void main(String[] args) {
        try {

            // 1. Inicializar JADE
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = runtime.createMainContainer(profile);

            // 2. Criar sistema (orquestrador)
            SistemaFutebol sistema = new SistemaFutebol(mainContainer);

            // 3. Criar por enquanto apenas um agente para teste
            sistema.criarJogador("jogador1");

            // 4. Subir API REST
            ApiServer.start(sistema);

            // WebSocket
            EventSocket socket = new EventSocket(9090);
            socket.start();

            System.out.println("Sistema Multi-Agente de Futebol iniciado com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}