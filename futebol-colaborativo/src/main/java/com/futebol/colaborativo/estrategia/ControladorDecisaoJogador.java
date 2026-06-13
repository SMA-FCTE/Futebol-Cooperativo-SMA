package com.futebol.colaborativo.estrategia;

import com.futebol.colaborativo.jogo.ContextoDecisao;
import com.futebol.colaborativo.jogo.PapelJogador;
import com.futebol.colaborativo.jogo.TipoDecisao;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;
import com.futebol.colaborativo.movimento.Movimento;

import java.util.Map;
import java.util.Objects;

public class ControladorDecisaoJogador {

    private final SeletorDecisaoPonderada seletorDecisao;

    public ControladorDecisaoJogador() {
        this(new SeletorDecisaoPonderada());
    }

    public ControladorDecisaoJogador(SeletorDecisaoPonderada seletorDecisao) {
        this.seletorDecisao = Objects.requireNonNull(seletorDecisao, "seletorDecisao");
    }

    public TipoDecisao decidir(ContextoDecisao contexto) {
        Objects.requireNonNull(contexto, "contexto");

        if (contexto.jogadorAtualEstaComBola()) {
            return TipoDecisao.AGIR_COM_BOLA;
        }

        if (contexto.existeJogadorComBola()) {
            return TipoDecisao.INTERCEPTAR;
        }

        if (contexto.bolaEstaLivre()) {
            return decidirComBolaLivre(contexto);
        }

        return TipoDecisao.MANTER_POSICAO;
    }

    private TipoDecisao decidirComBolaLivre(ContextoDecisao contexto) {
        if (jogadorEstaPertoDaBola(contexto.getEstadoAtual())) {
            return TipoDecisao.PERSEGUIR_BOLA;
        }

        // Regras de zona tem prioridade sobre o "forcar por ticks": cada papel
        // cede a bola do lado errado do campo ao companheiro daquela zona.
        if (contexto.getPapel() == PapelJogador.ZAGUEIRO && contexto.isBolaNoFieldAdversario()) {
            // Penalidade forte (nao-rigida): com a bola no campo adversario o
            // zagueiro quase sempre mantem posicao defensiva em vez de avancar.
            return seletorDecisao.sortear(Map.of(
                    TipoDecisao.PERSEGUIR_BOLA, 5,
                    TipoDecisao.MANTER_POSICAO_DEFENSIVA, 95));
        }

        if (contexto.getPapel() == PapelJogador.ATACANTE && contexto.isBolaNoFieldProprio()) {
            // Espelho: com a bola no campo proprio o atacante quase sempre
            // mantem posicao ofensiva avancada em vez de recuar para busca-la.
            return seletorDecisao.sortear(Map.of(
                    TipoDecisao.PERSEGUIR_BOLA, 10,
                    TipoDecisao.MANTER_POSICAO_OFENSIVA, 90));
        }

        if (contexto.getTicksBolaLivre() >= contexto.getPerfilTatico().getTicksBolaLivreParaForcar()) {
            return TipoDecisao.PERSEGUIR_BOLA;
        }

        return seletorDecisao.sortear(contexto.getPerfilTatico().getPesosBolaLivre());
    }

    private boolean jogadorEstaPertoDaBola(JogadorEstado estado) {
        return Movimento.estaPerto(
                estado.x,
                estado.y,
                Ambiente.bola.x,
                Ambiente.bola.y,
                Movimento.RAIO_PERSEGUICAO_BOLA);
    }
}
