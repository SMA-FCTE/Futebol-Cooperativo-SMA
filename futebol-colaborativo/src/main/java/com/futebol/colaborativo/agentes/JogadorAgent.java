package com.futebol.colaborativo.agentes;

import com.futebol.colaborativo.SistemaFutebol;
import com.futebol.colaborativo.estrategia.ControladorDecisaoJogador;
import com.futebol.colaborativo.estrategia.PerfilTatico;
import com.futebol.colaborativo.jogo.ConfiguracaoJogador;
import com.futebol.colaborativo.jogo.ContextoDecisao;
import com.futebol.colaborativo.jogo.PapelJogador;
import com.futebol.colaborativo.jogo.Time;
import com.futebol.colaborativo.jogo.TipoDecisao;
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

    static final String CONVERSA_ID_DISPUTA = "disputa-bola";
    private static final boolean USAR_CHUTE_ALEATORIO_TESTE = true;
    private static final int TICKS_PENALIDADE_PERDER_DISPUTA = 20;
    private static final double DISTANCIA_CHUTE_AO_GOL = 10.0;
    private static final double FORCA_CHUTE = 1.65;
    private static final double MAX_DESVIO_ANGULO = Math.PI / 6.0; // 120 graus
    private static final double FORCA_CHUTE_DIRIGIDO = 1.35;
    private static final double DISTANCIA_POSICAO_DEFENSIVA_DO_GOL = 8.0;
    // Mais distante do gol que a posicao defensiva: o atacante espera proximo ao
    // meio-campo, sem se sobrepor ao zagueiro adversario (que fica colado no gol).
    private static final double DISTANCIA_POSICAO_OFENSIVA_DO_GOL = 35.0;
    private static final AtomicLong CONTADOR_DISPUTAS = new AtomicLong(); // Garante que dois agentes não iniciem disputa com o mesmo ID

    private JogadorEstado estado;
    private SistemaFutebol sistema;

    // Atributos de posição do agente jogador
    private double xInicial;
    private double yInicial;

    // Atributo da posição do gol
    private double golX;
    private Time time;
    private PapelJogador papel;
    private PerfilTatico perfilTatico;
    private ControladorDecisaoJogador controladorDecisao;
    private int ticksBolaLivre = 0;

    // Atributos da posição de interceptação
    private double alvoInterceptacaoX;
    private double alvoInterceptacaoY;
    private boolean temAlvoInterceptacao = false;
    private boolean interceptacaoPossivel = false;

    // Atributos de disputa
    private boolean disputaEmAndamento = false;
    private String disputaId;
    private String oponenteDisputa;
    private int rodadaDisputa;
    private JogadaDisputa jogadaLocalPendente;
    private int ticksPenalidadePerderDisputaRestantes = 0;
    private final Random random = new Random(); // Para sortear jogadas

    @Override
    protected void setup() {

        Object[] args = getArguments(); // Pega os argumentos passados para o agente no momento da criação

        if (args == null || args.length < 2 || !(args[1] instanceof ConfiguracaoJogador)) {
            System.err.println("Erro: argumentos insuficientes para criar o agente jogador " + getLocalName());
            doDelete();
            return;
        }

        sistema = (SistemaFutebol) args[0];
        ConfiguracaoJogador configuracao = (ConfiguracaoJogador) args[1];
        xInicial = configuracao.getXInicial();
        yInicial = configuracao.getYInicial();
        golX = configuracao.getGolX();
        time = configuracao.getTime();
        papel = configuracao.getPapel();
        perfilTatico = switch (papel) {
            case ATACANTE -> PerfilTatico.atacante();
            case ZAGUEIRO -> PerfilTatico.zagueiro();
            default -> PerfilTatico.equilibrado();
        };
        controladorDecisao = new ControladorDecisaoJogador();

        estado = new JogadorEstado();
        estado.x = xInicial;
        estado.y = yInicial;
        estado.golX = golX;
        estado.time = time.name();
        estado.papel = papel.name();

        // Comportamento para ficar ouvindo eventos/mensagens de forma cíclica - representa a “caixa de entrada” do agente
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mensagemTemplate = MessageTemplate.MatchConversationId(CONVERSA_ID_DISPUTA); // Só vai buscar mensagens cuja conversationId seja "disputa-bola"
                ACLMessage mensagem = myAgent.receive(mensagemTemplate);

                if (mensagem == null) {
                    block(); // Se nenhuma mensagem foi encontrada, pausa temporariamente esse behaviour (economizar processamento)
                    return;
                }

                processarMensagemDisputa(mensagem); // Se chegou mensagem, envia para o método que trata a disputa
            }
        });

        // Comportamento principal de decisão do agente
        addBehaviour(new TickerBehaviour(this, 100) {

            @Override
            protected void onTick() {
                decidirAcaoPrincipal();
            }
        });
    }

    // ----------------------------------------------- INICIO - FUNCOES UTILIZADAS NO AGENTE -----------------------------------------------

    private boolean estaEmPenalidade() {
        return ticksPenalidadePerderDisputaRestantes > 0;
    }

    private void reduzirPenalidade() {
        ticksPenalidadePerderDisputaRestantes--;
        estado.ticksPenalidadePerderDisputaRestantes = ticksPenalidadePerderDisputaRestantes;
        temAlvoInterceptacao = false;
        atualizarEstado();
    }

    private void decidirAcaoPrincipal() {
        if (estaEmPenalidade()) {
            reduzirPenalidade();
            return;
        }

        if (disputaEmAndamento) {
            printTerminalEstado();
            return;
        }

        if (tentarIniciarDisputa()) {
            atualizarEstado();
            return;
        }

        ContextoDecisao contexto = montarContextoDecisao();
        TipoDecisao decisao = controladorDecisao.decidir(contexto);
        executarDecisao(decisao, contexto);
        atualizarEstado();
    }

    private ContextoDecisao montarContextoDecisao() {
        JogadorEstado jogadorComBola = sistema == null ? null : sistema.getJogadorComBola();
        atualizarContadorBolaLivre(jogadorComBola);

        return new ContextoDecisao(
                getLocalName(),
                estado,
                jogadorComBola,
                papel,
                time,
                perfilTatico,
                ticksBolaLivre);
    }

    private void atualizarContadorBolaLivre(JogadorEstado jogadorComBola) {
        if (jogadorComBola == null) {
            ticksBolaLivre++;
            return;
        }

        ticksBolaLivre = 0;
    }

    private void executarDecisao(TipoDecisao decisao, ContextoDecisao contexto) {
        switch (decisao) {
            case PERSEGUIR_BOLA:
                agirSemJogadorComBola();
                break;
            case AGIR_COM_BOLA:
                agirComBola();
                break;
            case INTERCEPTAR:
                if (contexto.getJogadorComBola() == null) {
                    manterPosicaoDefensiva();
                } else {
                    agirSemBola(contexto.getJogadorComBola());
                }
                break;
            case MANTER_POSICAO_DEFENSIVA:
                manterPosicaoDefensiva();
                break;
            case MANTER_POSICAO_OFENSIVA:
                manterPosicaoOfensiva();
                break;
            case MANTER_POSICAO:
            default:
                temAlvoInterceptacao = false;
                break;
        }
    }

    private void agirSemJogadorComBola() {
        temAlvoInterceptacao = false; // Limpa alvo de interceptação, porque não há jogador conduzindo a bola
        perseguirBola();
    }

    private void agirComBola() {
        temAlvoInterceptacao = false; // Limpa alvo de interceptação, porque quem tem a bola não precisa interceptar

        if (USAR_CHUTE_ALEATORIO_TESTE) { // Modo de teste para ver o movimento
            chutarDirigidoAoGol();
        } else {
            conduzirAteOGol();
        }
    }

    private void agirSemBola(JogadorEstado algumJogadorComBola) {
        // Caso outro jogador esteja com a bola.

        if (!temAlvoInterceptacao) {
            double[] ponto = calcularPontoIntercepcao(algumJogadorComBola); // Calcula onde o jogador deve tentar interceptar o adversário.

            alvoInterceptacaoX = ponto[0];
            alvoInterceptacaoY = ponto[1];

            temAlvoInterceptacao = true;

            System.out.printf(
                    "[%s] alvo fixado em (%.1f, %.1f)%n",
                    getLocalName(), alvoInterceptacaoX, alvoInterceptacaoY);
        }

        irParaPontoInterceptacao();
    }

    // ----------------------------------------------- FIM - FUNCOES UTILIZADAS NO AGENTE -----------------------------------------------

    private void processarMensagemDisputa(ACLMessage mensagem) {
        switch (mensagem.getPerformative()) {
            case ACLMessage.CFP:
                receberCfpDisputa(mensagem);
                break;
            case ACLMessage.PROPOSE:
                receberPropostaDisputa(mensagem);
                break;
            case ACLMessage.ACCEPT_PROPOSAL:
            case ACLMessage.REJECT_PROPOSAL:
                receberResultadoDisputa(mensagem);
                break;
            case ACLMessage.REFUSE:
                receberRecusaDisputa(mensagem);
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
        return !estado.comBola && oponente.comBola;
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
        cfp.setConversationId(CONVERSA_ID_DISPUTA);
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

    private void receberCfpDisputa(ACLMessage mensagem) {
        Map<String, String> dados = parseConteudo(mensagem.getContent());
        String id = dados.get("id");
        int rodada = parseInt(dados.get("rodada"), 1);
        String desafiante = mensagem.getSender().getLocalName();

        if (id == null) {
            responderRecusa(mensagem, "id-ausente");
            return;
        }

        if (estaEmPenalidade()) {
            responderRecusa(mensagem, "em-penalidade");
            return;
        }

        boolean mesmaDisputa = disputaEmAndamento
                && id.equals(disputaId)
                && desafiante.equals(oponenteDisputa);

        if (disputaEmAndamento && !mesmaDisputa) {
            responderRecusa(mensagem, "ocupado");
            return;
        }

        disputaEmAndamento = true;
        disputaId = id;
        oponenteDisputa = desafiante;
        rodadaDisputa = rodada;
        jogadaLocalPendente = sortearJogada();

        ACLMessage proposta = mensagem.createReply();
        proposta.setPerformative(ACLMessage.PROPOSE);
        proposta.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        proposta.setConversationId(CONVERSA_ID_DISPUTA);
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

    private void receberPropostaDisputa(ACLMessage mensagem) {
        Map<String, String> dados = parseConteudo(mensagem.getContent());
        String id = dados.get("id");
        int rodada = parseInt(dados.get("rodada"), 1);

        if (!disputaEmAndamento || !disputaId.equals(id) || rodada != rodadaDisputa) {
            return;
        }

        String nomeOponente = mensagem.getSender().getLocalName();
        JogadaDisputa jogadaOponente = parseJogada(dados.get("jogada"));
        if (jogadaOponente == null) {
            responderRecusa(mensagem, "jogada-invalida");
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

        enviarResultadoDisputa(mensagem, performative, vencedor, perdedor, jogadaLocalPendente, jogadaOponente);
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
        resultado.setConversationId(CONVERSA_ID_DISPUTA);
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

    private void receberResultadoDisputa(ACLMessage mensagem) {
        Map<String, String> dados = parseConteudo(mensagem.getContent());
        String id = dados.get("id");

        if (!disputaEmAndamento || !disputaId.equals(id)) {
            return;
        }

        aplicarResultadoDisputa(dados.get("vencedor"), dados.get("perdedor"));
    }

    private void receberRecusaDisputa(ACLMessage mensagem) {
        Map<String, String> dados = parseConteudo(mensagem.getContent());
        String id = dados.get("id");

        if (disputaId != null && disputaId.equals(id)) {
            System.out.printf("[%s] disputa recusada por %s%n", getLocalName(), mensagem.getSender().getLocalName());
            limparDisputa();
        }
    }

    private void responderRecusa(ACLMessage mensagem, String motivo) {
        ACLMessage recusa = mensagem.createReply();
        recusa.setPerformative(ACLMessage.REFUSE);
        recusa.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        recusa.setConversationId(CONVERSA_ID_DISPUTA);
        recusa.setContent(mensagem.getContent() + ";motivo=" + motivo);
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
            ticksPenalidadePerderDisputaRestantes = TICKS_PENALIDADE_PERDER_DISPUTA;
            estado.ticksPenalidadePerderDisputaRestantes = ticksPenalidadePerderDisputaRestantes;
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

    private void atualizarEstado() {
        if (sistema != null) {
            sistema.atualizarEstado(getLocalName(), estado);
        }

        printTerminalEstado();
    }

    private void printTerminalEstado() {
        System.out.printf(
                "Agente: %s | Pos: (%.2f, %.2f) | Bola: (%.2f, %.2f) | Com bola: %s | Recuperacao: %d%n",
                getLocalName(),
                estado.x, estado.y,
                Ambiente.bola.x, Ambiente.bola.y,
                estado.comBola,
                ticksPenalidadePerderDisputaRestantes);
    }

    private void perseguirBola() {
        Movimento.mover(estado, Ambiente.bola.x, Ambiente.bola.y);

        if (tocouNaBola()) {
            estado.comBola = true;
            if (sistema != null) {
                sistema.definirPosseBola(getLocalName());
            } else {
                Ambiente.bola.posicionarComPosse(getLocalName(), estado.x, estado.y);
            }
        }
    }

    private void conduzirAteOGol() {

        double distanciaGol = Movimento.calcularDistancia(estado.x, estado.y, golX, Ambiente.golY);

        // DEBUG DO CAMINHO
        System.out.println("[" + getLocalName() + "] indo para o gol" +
                " | atual: (" + estado.x + ", " + estado.y + ")" +
                " | alvo: (" + golX + ", " + Ambiente.golY + ")");

        if (distanciaGol <= DISTANCIA_CHUTE_AO_GOL) {
            chutarParaOGol();
            return;
        }

        Movimento.conduzirBola(estado, getLocalName(), golX, Ambiente.golY);

        if (chegouNoGol()) {
            sistema.registrarGol(getLocalName());
        }
    }

    private boolean tocouNaBola() {
        return Movimento.estaPerto(
                estado.x,
                estado.y,
                Ambiente.bola.x,
                Ambiente.bola.y,
                Movimento.RAIO_CONTATO_BOLA);
    }

    private boolean chegouNoGol() {
        return Movimento.estaPerto(estado.x, estado.y, golX, Ambiente.golY, Movimento.RAIO_GOL);
    }

    private void chutarParaOGol() {
        double deltaX = golX - estado.x;
        double deltaY = Ambiente.golY - estado.y;
        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distancia == 0) {
            distancia = 1;
            deltaX = golX == 0 ? -1 : 1;
            deltaY = 0;
        }

        double forcaX = (deltaX / distancia) * FORCA_CHUTE;
        double forcaY = (deltaY / distancia) * FORCA_CHUTE;

        estado.comBola = false;

        ACLMessage chute = new ACLMessage(ACLMessage.INFORM);
        chute.addReceiver(new AID("bola", AID.ISLOCALNAME));
        chute.setContent(forcaX + "," + forcaY);
        send(chute);

        if (sistema != null) {
            sistema.atualizarEstado(getLocalName(), estado);
        }

        System.out.printf("[%s] chutou para o gol com forca (%.2f, %.2f)%n", getLocalName(), forcaX, forcaY);
    }

    private void chutarDirigidoAoGol() {
        double anguloBase = Math.atan2(Ambiente.golY - estado.y, golX - estado.x);
        double ruido = (random.nextDouble() * 2.0 - 1.0) * MAX_DESVIO_ANGULO;
        double anguloFinal = anguloBase + ruido;

        double forcaX = Math.cos(anguloFinal) * FORCA_CHUTE_DIRIGIDO;
        double forcaY = Math.sin(anguloFinal) * FORCA_CHUTE_DIRIGIDO;

        estado.comBola = false;

        ACLMessage chute = new ACLMessage(ACLMessage.INFORM);
        chute.addReceiver(new AID("bola", AID.ISLOCALNAME));
        chute.setContent(forcaX + "," + forcaY);
        send(chute);

        if (sistema != null) {
            sistema.atualizarEstado(getLocalName(), estado);
        }

        System.out.printf("[%s] chute dirigido | angulo_base=%.0f° | desvio=%.0f° | forca=(%.2f, %.2f)%n",
                getLocalName(),
                Math.toDegrees(anguloBase),
                Math.toDegrees(ruido),
                forcaX, forcaY);
    }

    private double getProprioGolX() {
        return golX == 0 ? Ambiente.largura : 0;
    }

    private void manterPosicaoDefensiva() {
        temAlvoInterceptacao = false;
        Movimento.mover(estado, getPosicaoDefensivaX(), Ambiente.golY);
    }

    private double getPosicaoDefensivaX() {
        double proprioGolX = getProprioGolX();

        if (proprioGolX == 0) {
            return DISTANCIA_POSICAO_DEFENSIVA_DO_GOL;
        }

        return Ambiente.largura - DISTANCIA_POSICAO_DEFENSIVA_DO_GOL;
    }

    private void manterPosicaoOfensiva() {
        temAlvoInterceptacao = false;
        Movimento.mover(estado, getPosicaoOfensivaX(), Ambiente.golY);
    }

    private double getPosicaoOfensivaX() {
        // Espelho da posicao defensiva: avancada, perto do gol que o jogador ataca.
        if (golX == 0) {
            return DISTANCIA_POSICAO_OFENSIVA_DO_GOL;
        }

        return Ambiente.largura - DISTANCIA_POSICAO_OFENSIVA_DO_GOL;
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

        if (interceptacaoPossivel) {
            Movimento.mover(estado, alvoInterceptacaoX, alvoInterceptacaoY);
        } else {
            Movimento.mover(estado, getProprioGolX(), Ambiente.golY);
        }
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
