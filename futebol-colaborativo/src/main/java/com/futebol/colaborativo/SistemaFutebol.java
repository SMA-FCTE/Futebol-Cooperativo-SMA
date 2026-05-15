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

    private static final double ATRITO_BOLA = 0.985;
    private static final double VELOCIDADE_MINIMA_BOLA = 0.02;
    private static final double RESTITUICAO_BORDA = 0.82;
    private static final double RAIO_GOL = 2.4;

    private final AgentContainer container;

    private final Map<String, AgentController> jogadores = new HashMap<>();
    private AgentController bolaAgent;
    private final Map<String, JogadorEstado> estados = new HashMap<>();
    private final Map<String, ConfiguracaoJogador> configuracoes = new HashMap<>();
    private DisputaBolaEstadoDTO ultimaDisputa = null;

    private static final Gson gson = new Gson();

    public SistemaFutebol(AgentContainer container) {
        this.container = container;
    }

    public String iniciarBola() {
        try {
            if (bolaAgent != null) {
                return "Bola ja esta em campo";
            }

            bolaAgent = container.createNewAgent(
                "bola",
                "com.futebol.colaborativo.agentes.BolaAgent",
                new Object[]{ this }
            );

            bolaAgent.start();
            return "Bola criada";
        } catch (Exception e) {
            return "Erro ao criar bola";
        }
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

            if (nome.equals(nomeOponente) || estadoOponente == null || estaEmPenalidade(estadoOponente)) {
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

        Ambiente.bola.posicionarComPosse(nomeVencedor, estadoVencedor.x, estadoVencedor.y);
        enviarEstadoAtualParaClientes();

        return true;
    }

    public static boolean estaProximoParaDisputa(JogadorEstado primeiro, JogadorEstado segundo) {
        if (primeiro == null || segundo == null) {
            return false;
        }

        double deltaX = primeiro.x - segundo.x;
        double deltaY = primeiro.y - segundo.y;
        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return distancia <= 1.8;
    }

    private static boolean estaEmPenalidade(JogadorEstado estado) {
        return estado.ticksPenalidadePerderDisputaRestantes > 0;
    }

    public synchronized void chutarBola(String nomeJogador, double forcaX, double forcaY) {
        JogadorEstado estadoJogador = estados.get(nomeJogador);
        if (estadoJogador != null) {
            estadoJogador.comBola = false;
        }

        Ambiente.bola.emPosseDe = null;
        Ambiente.bola.soltarComForca(forcaX, forcaY);
        enviarEstadoAtualParaClientes();
    }

    public synchronized void aplicarFisicaBola() {
        if (Ambiente.bola.emPosseDe != null) {
            JogadorEstado dono = estados.get(Ambiente.bola.emPosseDe);
            if (dono != null && dono.comBola) {
                Ambiente.bola.posicionarComPosse(Ambiente.bola.emPosseDe, dono.x, dono.y);
            } else {
                Ambiente.bola.emPosseDe = null;
            }
            enviarEstadoAtualParaClientes();
            return;
        }

        Ambiente.bola.x += Ambiente.bola.velocidadeX;
        Ambiente.bola.y += Ambiente.bola.velocidadeY;

        Ambiente.bola.velocidadeX *= ATRITO_BOLA;
        Ambiente.bola.velocidadeY *= ATRITO_BOLA;

        if (Math.abs(Ambiente.bola.velocidadeX) < VELOCIDADE_MINIMA_BOLA) {
            Ambiente.bola.velocidadeX = 0;
        }

        if (Math.abs(Ambiente.bola.velocidadeY) < VELOCIDADE_MINIMA_BOLA) {
            Ambiente.bola.velocidadeY = 0;
        }

        rebaterBolaNasBordas();
        verificarGolDaBola();
        enviarEstadoAtualParaClientes();
    }

    private void rebaterBolaNasBordas() {
        if (Ambiente.bola.x < 0) {
            Ambiente.bola.x = 0;
            Ambiente.bola.velocidadeX = Math.abs(Ambiente.bola.velocidadeX) * RESTITUICAO_BORDA;
        } else if (Ambiente.bola.x > Ambiente.largura) {
            Ambiente.bola.x = Ambiente.largura;
            Ambiente.bola.velocidadeX = -Math.abs(Ambiente.bola.velocidadeX) * RESTITUICAO_BORDA;
        }

        if (Ambiente.bola.y < 0) {
            Ambiente.bola.y = 0;
            Ambiente.bola.velocidadeY = Math.abs(Ambiente.bola.velocidadeY) * RESTITUICAO_BORDA;
        } else if (Ambiente.bola.y > Ambiente.altura) {
            Ambiente.bola.y = Ambiente.altura;
            Ambiente.bola.velocidadeY = -Math.abs(Ambiente.bola.velocidadeY) * RESTITUICAO_BORDA;
        }
    }

    private void verificarGolDaBola() {
        boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0 && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;
        boolean bolaNoGolDireito = Ambiente.bola.x >= Ambiente.largura && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;

        if (!bolaNoGolEsquerdo && !bolaNoGolDireito) {
            return;
        }

        String marcador = localizarUltimoAtacante(bolaNoGolEsquerdo ? 0 : Ambiente.largura);
        registrarGol(marcador == null ? "Bola" : marcador);
    }

    private String localizarUltimoAtacante(double golAtingido) {
        for (Map.Entry<String, ConfiguracaoJogador> entry : configuracoes.entrySet()) {
            if (entry.getValue().golX == golAtingido) {
                return entry.getKey();
            }
        }

        return null;
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
        private final double golX;

        private ConfiguracaoJogador(double xInicial, double yInicial, double golX) {
            this.xInicial = xInicial;
            this.yInicial = yInicial;
            this.golX = golX;
        }
    }
}
