package com.futebol.colaborativo;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.futebol.colaborativo.api.EventSocket;
import com.futebol.colaborativo.model.JogadorEstado;
import com.futebol.colaborativo.model.Ambiente;

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

    public void atualizarEstado(String nome, JogadorEstado estado) {
        estados.put(nome, estado);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("jogadores", estados);
        resposta.put("bola", Ambiente.bola);

        String json = gson.toJson(resposta);
        EventSocket.broadcastMessage(json);
    }

    public Map<String, JogadorEstado> getEstados() {
        return estados;
    }

    public String getStatus() {
        return "Sistema rodando com " + jogadores.size() + " jogadores";
    }
}