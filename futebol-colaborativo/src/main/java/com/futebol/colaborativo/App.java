package com.futebol.colaborativo;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

import com.futebol.colaborativo.api.ApiServer;
import com.futebol.colaborativo.api.EventSocket;
import com.futebol.colaborativo.model.Ambiente;

public class App {
    private static final double AZUL_X_INICIAL = 50;
    private static final double AZUL_Y_INICIAL = 30;
    private static final double VERMELHO_X_INICIAL = 5;
    private static final double VERMELHO_Y_INICIAL = 5;

    public static void main(String[] args) {
        try {

            // 1. Inicializar JADE
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = runtime.createMainContainer(profile);

            // 2. Criar sistema (orquestrador)
            SistemaFutebol sistema = new SistemaFutebol(mainContainer);

            // 3. Criar jogadores iniciais
            sistema.criarJogador("azul", AZUL_X_INICIAL, AZUL_Y_INICIAL, 0);
            sistema.criarJogador(
                "vermelho",
                VERMELHO_X_INICIAL,
                VERMELHO_Y_INICIAL,
                Ambiente.largura
            );

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
