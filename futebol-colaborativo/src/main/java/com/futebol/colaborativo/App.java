package com.futebol.colaborativo;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

import com.futebol.colaborativo.api.ApiServer;
import com.futebol.colaborativo.api.EventSocket;
import com.futebol.colaborativo.jogo.ConfiguracaoJogador;
import com.futebol.colaborativo.jogo.PapelJogador;
import com.futebol.colaborativo.jogo.Time;
import com.futebol.colaborativo.model.Ambiente;

public class App {
    private static final boolean CHUTAR_BOLA_NO_INICIO = true;
    private static final double Y_INICIAL = 30;
    private static final double AZUL_ATACANTE_X = 45;
    private static final double AZUL_ZAGUEIRO_X = 20;
    private static final double VERMELHO_ATACANTE_X = 55;
    private static final double VERMELHO_ZAGUEIRO_X = 80;

    public static void main(String[] args) {
        try {

            // 1. Inicializar JADE
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = runtime.createMainContainer(profile);

            // 2. Criar sistema (orquestrador)
            SistemaFutebol sistema = new SistemaFutebol(mainContainer);
            sistema.iniciarBola();

            // 3. Criar jogadores iniciais
            sistema.criarJogador(new ConfiguracaoJogador(
                "atacante-1", AZUL_ATACANTE_X, Y_INICIAL, Ambiente.largura, Time.AZUL, PapelJogador.ATACANTE));
            sistema.criarJogador(new ConfiguracaoJogador(
                "zagueiro-1", AZUL_ZAGUEIRO_X, Y_INICIAL, Ambiente.largura, Time.AZUL, PapelJogador.ZAGUEIRO));
            sistema.criarJogador(new ConfiguracaoJogador(
                "atacante-2", VERMELHO_ATACANTE_X, Y_INICIAL, 0, Time.VERMELHO, PapelJogador.ATACANTE));
            sistema.criarJogador(new ConfiguracaoJogador(
                "zagueiro-2", VERMELHO_ZAGUEIRO_X, Y_INICIAL, 0, Time.VERMELHO, PapelJogador.ZAGUEIRO));

            // 4. Chute inicial na bola (opcional)
            if (CHUTAR_BOLA_NO_INICIO) {
                sistema.chuteBolaInicialAleatorio();
            }

            // 5. Subir API REST
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
