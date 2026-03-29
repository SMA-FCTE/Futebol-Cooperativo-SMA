package com.futebol.colaborativo;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.futebol.colaborativo.api.EventSocket;

import com.futebol.colaborativo.model.JogadorEstado;

public class SistemaFutebol {

    private AgentContainer container;

    private Map<String, AgentController> jogadores = new HashMap<>();
    private Map<String, JogadorEstado> estados = new HashMap<>();

    private static final Gson gson = new Gson();

    public SistemaFutebol(AgentContainer container) {
        this.container = container;
    }

    public String criarJogador(String nome) {
        try {
            if (jogadores.containsKey(nome)) {
                return "Jogador já existe";
            }

            AgentController agent = container.createNewAgent(
                nome,
                "com.futebol.colaborativo.agentes.JogadorAgent",
                new Object[]{ this }
            );

            agent.start();
            jogadores.put(nome, agent);

            return "Jogador criado: " + nome;

        } catch (Exception e) {
            return "Erro ao criar jogador";
        }
    }

    // Atualizado pelos agentes
    public void atualizarEstado(String nome, JogadorEstado estado) {
        estados.put(nome, estado);

        // enviar via websocket
        String json = gson.toJson(estados);
        EventSocket.broadcastMessage(json);
    }

    // Endpoint: /api/jogadores
    public Map<String, JogadorEstado> getEstados() {
        return estados;
    }

    // Endpoint: /api/status
    public String getStatus() {
        return "Sistema rodando com " + jogadores.size() + " jogadores";
    }

    
}