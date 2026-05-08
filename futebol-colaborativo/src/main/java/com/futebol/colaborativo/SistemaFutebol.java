package com.futebol.colaborativo;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.futebol.colaborativo.api.EventSocket;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.dto.DisputaBolaEstadoDTO;
import com.futebol.colaborativo.model.JogadorEstado;

public class SistemaFutebol {

    private final AgentContainer container;

    private final Map<String, AgentController> jogadores = new HashMap<>();
    private final Map<String, JogadorEstado> estados = new HashMap<>();
    private final Map<String, ConfiguracaoJogador> configuracoes = new HashMap<>();
    private DisputaBolaEstadoDTO ultimaDisputa = null;

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
            estados.put(nome, criarEstadoInicial(xInicial, yInicial, golX));

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

    public synchronized JogadorEstado getJogadorComBola() {
        for (JogadorEstado estado : estados.values()) {
            if (estado.comBola) {
                return estado;
            }
        }
        return null;
    }

    public synchronized OponenteDisputa localizarOponenteProximoParaDisputa(String nome, JogadorEstado estadoAtual) {
        OponenteDisputa oponenteEncontrado = null;

        for (Map.Entry<String, JogadorEstado> entry : estados.entrySet()) {
            String nomeOponente = entry.getKey();
            JogadorEstado estadoOponente = entry.getValue();

            if (nome.equals(nomeOponente) || estadoOponente == null) {
                continue;
            }

            if (!estaProximoParaDisputa(estadoAtual, estadoOponente)) {
                continue;
            }

            if (oponenteEncontrado == null || nomeOponente.compareTo(oponenteEncontrado.nome) < 0) {
                oponenteEncontrado = new OponenteDisputa(nomeOponente, estadoOponente.comBola);
            }
        }

        return oponenteEncontrado;
    }

    public synchronized boolean definirPosseBola(String nomeVencedor) {
        JogadorEstado estadoVencedor = estados.get(nomeVencedor);
        if (estadoVencedor == null) {
            return false;
        }

        for (Map.Entry<String, JogadorEstado> entry : estados.entrySet()) {
            entry.getValue().comBola = entry.getKey().equals(nomeVencedor);
        }

        Ambiente.bola.x = estadoVencedor.x;
        Ambiente.bola.y = estadoVencedor.y;
        enviarEstadoAtualParaClientes();

        return true;
    }

    public static boolean estaProximoParaDisputa(JogadorEstado primeiro, JogadorEstado segundo) {
        if (primeiro == null || segundo == null) {
            return false;
        }

        double distanciaManhattan = Math.abs(primeiro.x - segundo.x) + Math.abs(primeiro.y - segundo.y);
        return distanciaManhattan <= 1.0;
    }

    public synchronized void registrarInicioDisputa(
        String id,
        int rodada,
        String jogador1,
        String jogador2
    ) {
        ultimaDisputa = new DisputaBolaEstadoDTO(
            id,
            rodada,
            jogador1,
            jogador2,
            null,
            null,
            null,
            false,
            "Disputa em andamento entre " + jogador1 + " e " + jogador2
        );

        enviarEstadoAtualParaClientes();
    }

    public synchronized void registrarEmpateDisputa(
        String id,
        int rodada,
        String jogador1,
        String jogador2,
        String jogada1,
        String jogada2
    ) {
        ultimaDisputa = new DisputaBolaEstadoDTO(
            id,
            rodada,
            jogador1,
            jogador2,
            jogada1,
            jogada2,
            null,
            true,
            "Empate: " + jogador1 + " e " + jogador2 + " jogaram " + jogada1
        );

        enviarEstadoAtualParaClientes();
    }

    public synchronized void registrarResultadoDisputa(
        String id,
        int rodada,
        String jogador1,
        String jogador2,
        String jogada1,
        String jogada2,
        String vencedor
    ) {
        ultimaDisputa = new DisputaBolaEstadoDTO(
            id,
            rodada,
            jogador1,
            jogador2,
            jogada1,
            jogada2,
            vencedor,
            false,
            vencedor + " venceu com " + (vencedor.equals(jogador1) ? jogada1 : jogada2)
                + " contra " + (vencedor.equals(jogador1) ? jogada2 : jogada1)
        );

        enviarEstadoAtualParaClientes();
    }

    public synchronized DisputaBolaEstadoDTO getUltimaDisputa() {
        return ultimaDisputa;
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

    public static class OponenteDisputa {
        public final String nome;
        public final boolean comBola;

        private OponenteDisputa(String nome, boolean comBola) {
            this.nome = nome;
            this.comBola = comBola;
        }
    }

    private JogadorEstado criarEstadoInicial(double xInicial, double yInicial, double golX) {
        JogadorEstado estado = new JogadorEstado();
        estado.x = xInicial;
        estado.y = yInicial;
        estado.golX = golX;
        return estado;
    }

    private void enviarEstadoAtualParaClientes() {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("jogadores", estados);
        resposta.put("bola", Ambiente.bola);
        resposta.put("disputa", ultimaDisputa);

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
