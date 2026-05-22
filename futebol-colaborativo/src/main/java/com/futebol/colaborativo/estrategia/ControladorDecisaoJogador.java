package com.futebol.colaborativo.estrategia;

import com.futebol.colaborativo.jogo.ContextoDecisao;
import com.futebol.colaborativo.jogo.TipoDecisao;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;
import com.futebol.colaborativo.movimento.Movimento;

import java.util.Objects;

public class ControladorDecisaoJogador {

    static final int TICKS_BOLA_LIVRE_FORCAR_PERSEGUIR = 10;

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

        if (contexto.getTicksBolaLivre() >= TICKS_BOLA_LIVRE_FORCAR_PERSEGUIR) {
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
                Movimento.RAIO_CONTATO_BOLA);
    }
}
