package com.futebol.colaborativo;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.google.gson.Gson;
import com.futebol.colaborativo.api.EventSocket;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.dto.DisputaBolaEstadoDTO;
import com.futebol.colaborativo.dto.PasseEstadoDTO;
import com.futebol.colaborativo.jogo.ConfiguracaoJogador;
import com.futebol.colaborativo.jogo.Time;
import com.futebol.colaborativo.model.JogadorEstado;

public class SistemaFutebol {

    private static final double ATRITO_BOLA = 0.985;
    private static final double VELOCIDADE_MINIMA_BOLA = 0.02;
    private static final double RESTITUICAO_BORDA = 0.82;
    private static final double RAIO_GOL = 2.4;
    private static final double RAIO_PASSE = 100.0;

    private final AgentContainer container;

    private final Map<String, AgentController> jogadores = new HashMap<>();
    private AgentController bolaAgent;
    private final Map<String, JogadorEstado> estados = new HashMap<>();
    private final Map<String, ConfiguracaoJogador> configuracoes = new HashMap<>();
    private DisputaBolaEstadoDTO ultimaDisputa = null;
    private PasseEstadoDTO ultimoPasse = null;

    private static final Gson gson = new Gson();

    public SistemaFutebol(AgentContainer container) {
        this.container = container;
    }

    public void chuteBolaInicialAleatorio() {
        Random random = new Random();
        double angulo = random.nextDouble() * 2 * Math.PI;
        double forca = 0.5 + random.nextDouble() * 0.3;
        double forcaX = Math.cos(angulo) * forca;
        double forcaY = Math.sin(angulo) * forca;
        Ambiente.bola.soltarComForca(forcaX, forcaY);
        System.out.printf("Chute inicial aleatorio: forca (%.2f, %.2f)%n", forcaX, forcaY);
        enviarEstadoAtualParaClientes();
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

    public String criarJogador(ConfiguracaoJogador configuracao) {
        try {
            String nome = configuracao.getNome();
            if (jogadores.containsKey(nome)) {
                return "Jogador já existe";
            }

            configuracoes.put(nome, configuracao);
            estados.put(nome, criarEstadoInicial(configuracao));

            AgentController agent = container.createNewAgent(
                nome,
                "com.futebol.colaborativo.agentes.JogadorAgent",
                new Object[]{ this, configuracao }
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

    public synchronized JogadorEstado getEstado(String nomeJogador) {
        return estados.get(nomeJogador);
    }

    public synchronized Optional<String> localizarAliadoEmPosicaoDePasse(
            String nomeJogador,
            JogadorEstado estadoAtual,
            Time time) {
        if (nomeJogador == null || estadoAtual == null || time == null) {
            return Optional.empty();
        }

        String melhorAliado = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Map.Entry<String, JogadorEstado> entry : estados.entrySet()) {
            String nomeCandidato = entry.getKey();
            JogadorEstado candidato = entry.getValue();

            if (identificarMotivoReceptorPasse(
                    nomeJogador, estadoAtual, time, nomeCandidato, candidato) != null) {
                continue;
            }

            double deltaX = candidato.x - estadoAtual.x;
            double deltaY = candidato.y - estadoAtual.y;
            double distanciaPasse = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distanciaPasse > RAIO_PASSE || distanciaPasse >= menorDistancia) {
                continue;
            }

            melhorAliado = nomeCandidato;
            menorDistancia = distanciaPasse;
        }

        return Optional.ofNullable(melhorAliado);
    }

    public synchronized String identificarMotivoReceptorPasse(
            String nomeJogador,
            JogadorEstado estadoAtual,
            Time time,
            String nomeReceptor) {
        return identificarMotivoReceptorPasse(
                nomeJogador, estadoAtual, time, nomeReceptor, estados.get(nomeReceptor));
    }

    private String identificarMotivoReceptorPasse(
            String nomeJogador,
            JogadorEstado estadoAtual,
            Time time,
            String nomeReceptor,
            JogadorEstado receptor) {
        if (receptor == null) {
            return "RECEPTOR_NAO_ENCONTRADO";
        }
        if (nomeJogador.equals(nomeReceptor)) {
            return "MESMO_JOGADOR";
        }
        if (!time.name().equals(receptor.time)) {
            return "RECEPTOR_OUTRO_TIME";
        }
        if (estaEmPenalidade(receptor)) {
            return "RECEPTOR_EM_PENALIDADE";
        }
        if (receptor.ticksCooldownPasse > 0) {
            return "RECEPTOR_EM_COOLDOWN";
        }

        double deltaX = receptor.x - estadoAtual.x;
        double deltaY = receptor.y - estadoAtual.y;
        double distanciaPasse = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return distanciaPasse > RAIO_PASSE ? "RECEPTOR_FORA_DO_RAIO" : null;
    }

    public synchronized OponenteDisputa localizarOponenteProximoParaDisputa(String nome, JogadorEstado estadoAtual) {
        OponenteDisputa oponenteEncontrado = null;

        ConfiguracaoJogador confAtual = configuracoes.get(nome);

        for (Map.Entry<String, JogadorEstado> entry : estados.entrySet()) {
            String nomeOponente = entry.getKey();
            JogadorEstado estadoOponente = entry.getValue();

            if (nome.equals(nomeOponente) || estadoOponente == null || estaEmPenalidade(estadoOponente)) {
                continue;
            }

            ConfiguracaoJogador confOponente = configuracoes.get(nomeOponente);
            if (confAtual == null || confOponente == null || confAtual.getTime() == confOponente.getTime()) {
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

    public synchronized boolean liberarPosseBola(String nomeJogador) {
        JogadorEstado estadoJogador = estados.get(nomeJogador);
        boolean jogadorTinhaPosse = nomeJogador.equals(Ambiente.bola.emPosseDe)
                || estadoJogador != null && estadoJogador.comBola;

        if (estadoJogador != null) {
            estadoJogador.comBola = false;
        }

        if (nomeJogador.equals(Ambiente.bola.emPosseDe)) {
            Ambiente.bola.emPosseDe = null;
        }

        enviarEstadoAtualParaClientes();
        return jogadorTinhaPosse;
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
            if (entry.getValue().getGolX() == golAtingido) {
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

    public synchronized void registrarInicioPasse(
            String id,
            String passador,
            String receptor,
            String iniciador) {
        ultimoPasse = new PasseEstadoDTO(
                id,
                passador,
                receptor,
                iniciador,
                "NEGOCIANDO",
                null,
                null,
                false,
                null,
                iniciador + " iniciou passe de " + passador + " para " + receptor);
        enviarEstadoAtualParaClientes();
    }

    public synchronized void registrarPasseExecutado(
            String id,
            String passador,
            String receptor,
            double forcaX,
            double forcaY) {
        if (ultimoPasse == null || !id.equals(ultimoPasse.id)) {
            ultimoPasse = new PasseEstadoDTO(
                    id, passador, receptor, passador, "EM_TRAJETO",
                    forcaX, forcaY, false, null, passador + " passou para " + receptor);
        } else {
            ultimoPasse.status = "EM_TRAJETO";
            ultimoPasse.forcaX = forcaX;
            ultimoPasse.forcaY = forcaY;
            ultimoPasse.motivoRecusa = null;
            ultimoPasse.resultado = passador + " passou para " + receptor;
        }

        enviarEstadoAtualParaClientes();
    }

    public synchronized void registrarPasseRecebido(String id, String receptor) {
        if (ultimoPasse == null || !id.equals(ultimoPasse.id)) {
            return;
        }

        ultimoPasse.status = "RECEBIDO";
        ultimoPasse.recebido = true;
        ultimoPasse.resultado = receptor + " recebeu passe de " + ultimoPasse.passador;
        enviarEstadoAtualParaClientes();
    }

    public synchronized void registrarPasseRecusado(String id, String jogador, String motivo) {
        if (ultimoPasse == null || !id.equals(ultimoPasse.id)) {
            return;
        }

        boolean posseAlterada = "POSSE_ALTERADA_ANTES_DA_RESPOSTA".equals(motivo);
        ultimoPasse.status = posseAlterada ? "CANCELADO" : "RECUSADO";
        ultimoPasse.motivoRecusa = motivo;
        ultimoPasse.resultado = posseAlterada
                ? "Pedido cancelado porque a posse mudou antes da resposta"
                : jogador + " recusou o passe";
        enviarEstadoAtualParaClientes();
    }

    public synchronized void registrarPasseExpirado(String id) {
        if (ultimoPasse == null || !id.equals(ultimoPasse.id) || ultimoPasse.recebido) {
            return;
        }

        ultimoPasse.status = "NAO_RECEBIDO";
        ultimoPasse.resultado = "Passe de " + ultimoPasse.passador + " para "
                + ultimoPasse.receptor + " nao foi recebido";
        enviarEstadoAtualParaClientes();
    }

    public synchronized PasseEstadoDTO getUltimoPasse() {
        return ultimoPasse;
    }

    public synchronized void atualizarEstado(String nome, JogadorEstado estado) {
        estados.put(nome, estado);
        enviarEstadoAtualParaClientes();
    }

    public synchronized void registrarGol(String nome) {
        System.out.println(nome + " marcou um gol!");

        Ambiente.bola.posicionarNoCentro();
        chuteBolaInicialAleatorio();

        for (Map.Entry<String, ConfiguracaoJogador> entry : configuracoes.entrySet()) {
            JogadorEstado estado = estados.get(entry.getKey());
            if (estado == null) {
                continue;
            }

            ConfiguracaoJogador configuracao = entry.getValue();
            estado.x = configuracao.getXInicial();
            estado.y = configuracao.getYInicial();
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

    private JogadorEstado criarEstadoInicial(ConfiguracaoJogador configuracao) {
        JogadorEstado estado = new JogadorEstado();
        estado.nome = configuracao.getNome();
        estado.x = configuracao.getXInicial();
        estado.y = configuracao.getYInicial();
        estado.golX = configuracao.getGolX();
        estado.time = configuracao.getTime().name();
        estado.papel = configuracao.getPapel().name();
        return estado;
    }

    private void enviarEstadoAtualParaClientes() {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("jogadores", estados);
        resposta.put("bola", Ambiente.bola);
        resposta.put("disputa", ultimaDisputa);
        resposta.put("passe", ultimoPasse);

        String json = gson.toJson(resposta);
        EventSocket.enviarMensagemParaClientes(json);
    }
}
