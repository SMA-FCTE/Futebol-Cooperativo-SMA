package com.futebol.colaborativo.agentes;

import com.futebol.colaborativo.SistemaFutebol;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;
import com.futebol.colaborativo.movimento.Movimento;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class JogadorAgent extends Agent {

    static final String CONVERSATION_ID_DISPUTA = "disputa-bola";
    private static final int TICKS_RECUPERACAO = 4;
    private static final AtomicLong CONTADOR_DISPUTAS = new AtomicLong();

    private JogadorEstado estado;
    private SistemaFutebol sistema;
    private double xInicial;
    private double yInicial;
    private double golX;
    private double alvoInterceptacaoX;
    private double alvoInterceptacaoY;
    private boolean temAlvoInterceptacao = false;
    private boolean interceptacaoPossivel = false;
    private boolean disputaEmAndamento = false;
    private String disputaId;
    private String oponenteDisputa;
    private int rodadaDisputa;
    private JogadaDisputa jogadaLocalPendente;
    private int recuperacaoTicksRestantes = 0;
    private final Random random = new Random();

    @Override
    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            sistema = (SistemaFutebol) args[0];
            xInicial = ((Number) args[1]).doubleValue();
            yInicial = ((Number) args[2]).doubleValue();
            golX = ((Number) args[3]).doubleValue();
        }

        estado = new JogadorEstado();
        estado.x = xInicial;
        estado.y = yInicial;
        estado.golX = golX;

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId(CONVERSATION_ID_DISPUTA);
                ACLMessage msg = myAgent.receive(mt);

                if (msg == null) {
                    block();
                    return;
                }

                processarMensagemDisputa(msg);
            }
        });

        addBehaviour(new TickerBehaviour(this, 50) {

            @Override
            protected void onTick() {

                if (recuperacaoTicksRestantes > 0) {
                    recuperacaoTicksRestantes--;
                    temAlvoInterceptacao = false;
                    atualizarEstadoELogar();
                    return;
                }

                if (disputaEmAndamento) {
                    logarEstado();
                    return;
                }

                if (tentarIniciarDisputa()) {
                    atualizarEstadoELogar();
                    return;
                }

                JogadorEstado algumJogadorComBola = sistema.getJogadorComBola();

                if (algumJogadorComBola == null) {
                    temAlvoInterceptacao = false;
                    perseguirBola();

                } else if (estado.comBola) {
                    temAlvoInterceptacao = false;
                    conduzirAteOGol();

                } else {

                    if (!temAlvoInterceptacao) {
                        double[] ponto = calcularPontoIntercepcao(algumJogadorComBola);

                        alvoInterceptacaoX = ponto[0];
                        alvoInterceptacaoY = ponto[1];

                        temAlvoInterceptacao = true;

                        System.out.printf(
                                "[%s] alvo fixado em (%.1f, %.1f)%n",
                                getLocalName(), alvoInterceptacaoX, alvoInterceptacaoY);
                    }

                    irParaPontoInterceptacao();
                }

                atualizarEstadoELogar();
            }
        });
    }

    private void processarMensagemDisputa(ACLMessage msg) {
        switch (msg.getPerformative()) {
            case ACLMessage.CFP:
                receberCfpDisputa(msg);
                break;
            case ACLMessage.PROPOSE:
                receberPropostaDisputa(msg);
                break;
            case ACLMessage.ACCEPT_PROPOSAL:
            case ACLMessage.REJECT_PROPOSAL:
                receberResultadoDisputa(msg);
                break;
            case ACLMessage.REFUSE:
                receberRecusaDisputa(msg);
                break;
            default:
                break;
        }
    }

    private boolean tentarIniciarDisputa() {
        if (sistema == null) {
            return false;
        }

        SistemaFutebol.OponenteDisputa oponente = sistema.localizarOponenteProximoParaDisputa(getLocalName(), estado);

        if (oponente == null || !deveIniciarDisputa(oponente)) {
            return false;
        }

        iniciarDisputa(oponente.nome);
        return true;
    }

    private boolean deveIniciarDisputa(SistemaFutebol.OponenteDisputa oponente) {
        if (!estado.comBola && oponente.comBola) {
            return true;
        }

        if (estado.comBola && !oponente.comBola) {
            return false;
        }

        return getLocalName().compareTo(oponente.nome) < 0;
    }

    private void iniciarDisputa(String nomeOponente) {
        disputaEmAndamento = true;
        disputaId = "disputa-" + CONTADOR_DISPUTAS.incrementAndGet();
        oponenteDisputa = nomeOponente;
        rodadaDisputa = 1;
        jogadaLocalPendente = sortearJogada();

        enviarCfpDisputa();
    }

    private void enviarCfpDisputa() {
        if (sistema != null) {
            sistema.registrarInicioDisputa(disputaId, rodadaDisputa, getLocalName(), oponenteDisputa);
        }

        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.addReceiver(new AID(oponenteDisputa, AID.ISLOCALNAME));
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.setConversationId(CONVERSATION_ID_DISPUTA);
        cfp.setReplyWith(disputaId + "-" + rodadaDisputa);
        cfp.setContent(
                "id=" + disputaId +
                        ";rodada=" + rodadaDisputa +
                        ";desafiante=" + getLocalName());

        send(cfp);

        System.out.printf(
                "[%s] CFP disputa enviado para %s | id=%s | rodada=%d | jogada=%s%n",
                getLocalName(), oponenteDisputa, disputaId, rodadaDisputa, jogadaLocalPendente);
    }

    private void receberCfpDisputa(ACLMessage msg) {
        Map<String, String> dados = parseConteudo(msg.getContent());
        String id = dados.get("id");
        int rodada = parseInt(dados.get("rodada"), 1);
        String desafiante = msg.getSender().getLocalName();

        if (id == null) {
            responderRecusa(msg, "id-ausente");
            return;
        }

        boolean mesmaDisputa = disputaEmAndamento
                && id.equals(disputaId)
                && desafiante.equals(oponenteDisputa);

        if (disputaEmAndamento && !mesmaDisputa) {
            responderRecusa(msg, "ocupado");
            return;
        }

        disputaEmAndamento = true;
        disputaId = id;
        oponenteDisputa = desafiante;
        rodadaDisputa = rodada;
        jogadaLocalPendente = sortearJogada();

        ACLMessage proposta = msg.createReply();
        proposta.setPerformative(ACLMessage.PROPOSE);
        proposta.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        proposta.setConversationId(CONVERSATION_ID_DISPUTA);
        proposta.setContent(
                "id=" + disputaId +
                        ";rodada=" + rodadaDisputa +
                        ";jogador=" + getLocalName() +
                        ";jogada=" + jogadaLocalPendente);

        send(proposta);

        System.out.printf(
                "[%s] PROPOSE disputa para %s | id=%s | rodada=%d | jogada=%s%n",
                getLocalName(), oponenteDisputa, disputaId, rodadaDisputa, jogadaLocalPendente);
    }

    private void receberPropostaDisputa(ACLMessage msg) {
        Map<String, String> dados = parseConteudo(msg.getContent());
        String id = dados.get("id");
        int rodada = parseInt(dados.get("rodada"), 1);

        if (!disputaEmAndamento || !disputaId.equals(id) || rodada != rodadaDisputa) {
            return;
        }

        String nomeOponente = msg.getSender().getLocalName();
        JogadaDisputa jogadaOponente = parseJogada(dados.get("jogada"));
        if (jogadaOponente == null) {
            responderRecusa(msg, "jogada-invalida");
            limparDisputa();
            return;
        }

        ResultadoDisputa resultado = compararJogadas(jogadaLocalPendente, jogadaOponente);

        if (resultado == ResultadoDisputa.EMPATE) {
            if (sistema != null) {
                sistema.registrarEmpateDisputa(
                        disputaId,
                        rodadaDisputa,
                        getLocalName(),
                        nomeOponente,
                        jogadaLocalPendente.name(),
                        jogadaOponente.name());
            }

            System.out.printf(
                    "[%s] empate na disputa com %s | rodada=%d | jogada=%s%n",
                    getLocalName(), nomeOponente, rodadaDisputa, jogadaLocalPendente);

            rodadaDisputa++;
            jogadaLocalPendente = sortearJogada();
            enviarCfpDisputa();
            return;
        }

        boolean localVenceu = resultado == ResultadoDisputa.VITORIA_LOCAL;
        String vencedor = localVenceu ? getLocalName() : nomeOponente;
        String perdedor = localVenceu ? nomeOponente : getLocalName();
        int performative = localVenceu ? ACLMessage.REJECT_PROPOSAL : ACLMessage.ACCEPT_PROPOSAL;

        if (sistema != null) {
            sistema.registrarResultadoDisputa(
                    disputaId,
                    rodadaDisputa,
                    getLocalName(),
                    nomeOponente,
                    jogadaLocalPendente.name(),
                    jogadaOponente.name(),
                    vencedor);
        }

        enviarResultadoDisputa(msg, performative, vencedor, perdedor, jogadaLocalPendente, jogadaOponente);
        aplicarResultadoDisputa(vencedor, perdedor);
    }

    private void enviarResultadoDisputa(
            ACLMessage proposta,
            int performative,
            String vencedor,
            String perdedor,
            JogadaDisputa jogadaLocal,
            JogadaDisputa jogadaOponente) {
        ACLMessage resultado = proposta.createReply();
        resultado.setPerformative(performative);
        resultado.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        resultado.setConversationId(CONVERSATION_ID_DISPUTA);
        resultado.setContent(
                "id=" + disputaId +
                        ";rodada=" + rodadaDisputa +
                        ";vencedor=" + vencedor +
                        ";perdedor=" + perdedor +
                        ";jogadaLocal=" + jogadaLocal +
                        ";jogadaOponente=" + jogadaOponente);

        send(resultado);

        System.out.printf(
                "[%s] resultado disputa | vencedor=%s | perdedor=%s | local=%s | oponente=%s%n",
                getLocalName(), vencedor, perdedor, jogadaLocal, jogadaOponente);
    }

    private void receberResultadoDisputa(ACLMessage msg) {
        Map<String, String> dados = parseConteudo(msg.getContent());
        String id = dados.get("id");

        if (!disputaEmAndamento || !disputaId.equals(id)) {
            return;
        }

        aplicarResultadoDisputa(dados.get("vencedor"), dados.get("perdedor"));
    }

    private void receberRecusaDisputa(ACLMessage msg) {
        Map<String, String> dados = parseConteudo(msg.getContent());
        String id = dados.get("id");

        if (disputaId != null && disputaId.equals(id)) {
            System.out.printf("[%s] disputa recusada por %s%n", getLocalName(), msg.getSender().getLocalName());
            limparDisputa();
        }
    }

    private void responderRecusa(ACLMessage msg, String motivo) {
        ACLMessage recusa = msg.createReply();
        recusa.setPerformative(ACLMessage.REFUSE);
        recusa.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        recusa.setConversationId(CONVERSATION_ID_DISPUTA);
        recusa.setContent(msg.getContent() + ";motivo=" + motivo);
        send(recusa);
    }

    private void aplicarResultadoDisputa(String vencedor, String perdedor) {
        boolean venceu = getLocalName().equals(vencedor);
        boolean perdeu = getLocalName().equals(perdedor);

        if (venceu) {
            estado.comBola = true;
            temAlvoInterceptacao = false;

            if (sistema != null) {
                sistema.definirPosseBola(getLocalName());
            } else {
                Ambiente.bola.x = estado.x;
                Ambiente.bola.y = estado.y;
            }
        } else if (perdeu) {
            estado.comBola = false;
            temAlvoInterceptacao = false;
            recuperacaoTicksRestantes = TICKS_RECUPERACAO;
        }

        limparDisputa();

        if (sistema != null) {
            sistema.atualizarEstado(getLocalName(), estado);
        }
    }

    private void limparDisputa() {
        disputaEmAndamento = false;
        disputaId = null;
        oponenteDisputa = null;
        rodadaDisputa = 0;
        jogadaLocalPendente = null;
    }

    private JogadaDisputa sortearJogada() {
        JogadaDisputa[] jogadas = JogadaDisputa.values();
        return jogadas[random.nextInt(jogadas.length)];
    }

    private static Map<String, String> parseConteudo(String content) {
        Map<String, String> valores = new HashMap<>();

        if (content == null || content.isBlank()) {
            return valores;
        }

        for (String parte : content.split(";")) {
            String[] chaveValor = parte.split("=", 2);
            if (chaveValor.length == 2) {
                valores.put(chaveValor[0], chaveValor[1]);
            }
        }

        return valores;
    }

    private static int parseInt(String valor, int padrao) {
        try {
            return valor == null ? padrao : Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private static JogadaDisputa parseJogada(String valor) {
        try {
            return valor == null ? null : JogadaDisputa.valueOf(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void atualizarEstadoELogar() {
        if (sistema != null) {
            sistema.atualizarEstado(getLocalName(), estado);
        }

        logarEstado();
    }

    private void logarEstado() {
        System.out.printf(
                "Agente: %s | Pos: (%.2f, %.2f) | Bola: (%.2f, %.2f) | Com bola: %s | Recuperacao: %d%n",
                getLocalName(),
                estado.x, estado.y,
                Ambiente.bola.x, Ambiente.bola.y,
                estado.comBola,
                recuperacaoTicksRestantes);
    }

    private void perseguirBola() {
        String direcao = calcularDirecao(Ambiente.bola.x, Ambiente.bola.y);
        Movimento.moverGrid(estado, direcao);

        if (tocouNaBola()) {
            estado.comBola = true;
            if (sistema != null) {
                sistema.definirPosseBola(getLocalName());
            } else {
                Ambiente.bola.x = estado.x;
                Ambiente.bola.y = estado.y;
            }
        }
    }

    private void conduzirAteOGol() {

        String direcao = calcularDirecao(golX, Ambiente.golY);

        // DEBUG DO CAMINHO
        System.out.println("[" + getLocalName() + "] indo para o gol -> direção: " + direcao +
                " | atual: (" + estado.x + ", " + estado.y + ")" +
                " | alvo: (" + golX + ", " + Ambiente.golY + ")");

        Movimento.conduzirBola(estado, direcao);

        if (chegouNoGol()) {
            sistema.registrarGol(getLocalName());
        }
    }

    private boolean tocouNaBola() {
        return estado.x == Ambiente.bola.x && estado.y == Ambiente.bola.y;
    }

    private boolean chegouNoGol() {
        return estado.x == golX && estado.y == Ambiente.golY;
    }

    private String calcularDirecao(double alvoX, double alvoY) {
        double deltaX = alvoX - estado.x;
        double deltaY = alvoY - estado.y;

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX > 0 ? "DIREITA" : "ESQUERDA";
        } else {
            return deltaY > 0 ? "BAIXO" : "CIMA";
        }
    }

    private double getProprioGolX() {
        return golX == 0 ? Ambiente.largura : 0;
    }

    private double[] calcularPontoIntercepcao(JogadorEstado alvo) {

        double posicaoJogadorSimuladoX = alvo.x;
        double posicaoJogadorSimuladoY = alvo.y;

        double golAlvoDoJogadorSimulado = alvo.golX;

        int passo = 0;

        while (posicaoJogadorSimuladoX != golAlvoDoJogadorSimulado || posicaoJogadorSimuladoY != Ambiente.golY) {

            double deltaX = golAlvoDoJogadorSimulado - posicaoJogadorSimuladoX;
            double deltaY = Ambiente.golY - posicaoJogadorSimuladoY;

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                posicaoJogadorSimuladoX += Math.signum(deltaX);
            } else {
                posicaoJogadorSimuladoY += Math.signum(deltaY);
            }

            passo++;

            // tempo do vermelho até esse ponto
            double distVermelho = Math.abs(posicaoJogadorSimuladoX - estado.x) +
                    Math.abs(posicaoJogadorSimuladoY - estado.y);

            // condição de interceptação
            if (distVermelho <= passo) {
                interceptacaoPossivel = true;
                return new double[] { posicaoJogadorSimuladoX, posicaoJogadorSimuladoY };
            }

            if (passo > 200)
                break;
        }

        interceptacaoPossivel = false;
        return new double[] { getProprioGolX(), Ambiente.golY };
    }

    private void irParaPontoInterceptacao() {

        String direcao;

        if (interceptacaoPossivel) {
            direcao = calcularDirecao(alvoInterceptacaoX, alvoInterceptacaoY);
        } else {
            direcao = calcularDirecao(getProprioGolX(), Ambiente.golY);
        }

        Movimento.moverGrid(estado, direcao);
    }

    static ResultadoDisputa compararJogadas(JogadaDisputa local, JogadaDisputa oponente) {
        if (local == oponente) {
            return ResultadoDisputa.EMPATE;
        }

        boolean localVence = (local == JogadaDisputa.PEDRA && oponente == JogadaDisputa.TESOURA) ||
                (local == JogadaDisputa.TESOURA && oponente == JogadaDisputa.PAPEL) ||
                (local == JogadaDisputa.PAPEL && oponente == JogadaDisputa.PEDRA);

        return localVence ? ResultadoDisputa.VITORIA_LOCAL : ResultadoDisputa.VITORIA_OPONENTE;
    }

    enum JogadaDisputa {
        PEDRA,
        PAPEL,
        TESOURA
    }

    enum ResultadoDisputa {
        VITORIA_LOCAL,
        VITORIA_OPONENTE,
        EMPATE
    }
}
