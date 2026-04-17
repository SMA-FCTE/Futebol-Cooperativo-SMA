package com.futebol.colaborativo;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.futebol.colaborativo.api.EventSocket;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;

public class SistemaFutebol {

    private final AgentContainer container;

    private final Map<String, AgentController> jogadores = new HashMap<>();
    private final Map<String, JogadorEstado> estados = new HashMap<>();
    private final Map<String, ConfiguracaoJogador> configuracoes = new HashMap<>();

    private static final Gson gson = new Gson();

    public SistemaFutebol(AgentContainer container) {
        this.container = container;
    }

    public String criarJogador(String nome, double xInicial, double yInicial, double golX) {
        try {
            if (jogadores.containsKey(nome)) {
                return "Jogador já existe";
            }

            configuracoes.put(nome, new ConfiguracaoJogador(xInicial, yInicial, golX));
            estados.put(nome, criarEstadoInicial(xInicial, yInicial));

            AgentController agent = container.createNewAgent(
                nome,
                "com.futebol.colaborativo.agentes.JogadorAgent",
                new Object[]{ this, xInicial, yInicial, golX }
            );

            agent.start();
            jogadores.put(nome, agent);

            return "Jogador criado: " + nome;

        } catch (Exception e) {
            return "Erro ao criar jogador";
        }
    }

    public synchronized void atualizarEstado(String nome, JogadorEstado estado) {
        estados.put(nome, estado);
        enviarEstadoAtualParaClientes();
    }

    public synchronized void registrarGol(String nome) {
        System.out.println(nome + " marcou um gol!");

        Ambiente.bola.posicionarNoCentro();

        for (Map.Entry<String, ConfiguracaoJogador> entry : configuracoes.entrySet()) {
            JogadorEstado estado = estados.get(entry.getKey());
            if (estado == null) {
                continue;
            }

            ConfiguracaoJogador configuracao = entry.getValue();
            estado.x = configuracao.xInicial;
            estado.y = configuracao.yInicial;
            estado.comBola = false;
        }

        enviarEstadoAtualParaClientes();
    }

    public Map<String, JogadorEstado> getEstados() {
        return estados;
    }

    public String getStatus() {
        return "Sistema rodando com " + jogadores.size() + " jogadores";
    }

    private JogadorEstado criarEstadoInicial(double xInicial, double yInicial) {
        JogadorEstado estado = new JogadorEstado();
        estado.x = xInicial;
        estado.y = yInicial;
        return estado;
    }

    private void enviarEstadoAtualParaClientes() {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("jogadores", estados);
        resposta.put("bola", Ambiente.bola);

        String json = gson.toJson(resposta);
        EventSocket.enviarMensagemParaClientes(json);
    }

    private static class ConfiguracaoJogador {
        private final double xInicial;
        private final double yInicial;
        @SuppressWarnings("unused")
        private final double golX;

        private ConfiguracaoJogador(double xInicial, double yInicial, double golX) {
            this.xInicial = xInicial;
            this.yInicial = yInicial;
            this.golX = golX;
        }
    }
}
