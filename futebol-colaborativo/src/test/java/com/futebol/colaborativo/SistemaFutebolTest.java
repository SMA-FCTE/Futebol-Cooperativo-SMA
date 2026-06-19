package com.futebol.colaborativo;

import com.futebol.colaborativo.dto.PasseEstadoDTO;
import com.futebol.colaborativo.jogo.Time;
import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SistemaFutebolTest {

    @Test
    void registraCicloDoUltimoPasseParaOPayload() {
        SistemaFutebol sistema = new SistemaFutebol(null);

        sistema.registrarInicioPasse("passe-1", "zagueiro", "atacante", "atacante");
        sistema.registrarPasseExecutado("passe-1", "zagueiro", "atacante", 1.25, 0.5);
        sistema.registrarPasseRecebido("passe-1", "atacante");

        PasseEstadoDTO passe = sistema.getUltimoPasse();
        assertEquals("passe-1", passe.id);
        assertEquals("zagueiro", passe.passador);
        assertEquals("atacante", passe.receptor);
        assertEquals("atacante", passe.iniciador);
        assertEquals("RECEBIDO", passe.status);
        assertEquals(1.25, passe.forcaX);
        assertEquals(0.5, passe.forcaY);
        assertTrue(passe.recebido);
        assertEquals("atacante recebeu passe de zagueiro", passe.resultado);
    }

    @Test
    void registraMotivoDaRecusaDoPasse() {
        SistemaFutebol sistema = new SistemaFutebol(null);
        sistema.registrarInicioPasse("passe-2", "zagueiro", "atacante", "atacante");

        sistema.registrarPasseRecusado(
                "passe-2", "zagueiro", "RECEPTOR_FORA_DO_RAIO");

        PasseEstadoDTO passe = sistema.getUltimoPasse();
        assertEquals("RECUSADO", passe.status);
        assertEquals("RECEPTOR_FORA_DO_RAIO", passe.motivoRecusa);
        assertEquals("zagueiro recusou o passe", passe.resultado);
    }

    @Test
    void registraComoCanceladoQuandoPosseMudaAntesDaResposta() {
        SistemaFutebol sistema = new SistemaFutebol(null);
        sistema.registrarInicioPasse("passe-3", "zagueiro", "atacante", "atacante");

        sistema.registrarPasseRecusado(
                "passe-3",
                "zagueiro",
                "POSSE_ALTERADA_ANTES_DA_RESPOSTA");

        PasseEstadoDTO passe = sistema.getUltimoPasse();
        assertEquals("CANCELADO", passe.status);
        assertEquals("POSSE_ALTERADA_ANTES_DA_RESPOSTA", passe.motivoRecusa);
        assertEquals(
                "Pedido cancelado porque a posse mudou antes da resposta",
                passe.resultado);
    }

    @Test
    void liberaIndicadoresDePosseDeFormaAtomica() {
        SistemaFutebol sistema = new SistemaFutebol(null);
        JogadorEstado jogador = criarEstado("zagueiro", 20, 30, 100, Time.AZUL);
        jogador.comBola = true;
        sistema.getEstados().put(jogador.nome, jogador);
        Ambiente.bola.posicionarComPosse(jogador.nome, jogador.x, jogador.y);

        boolean liberou = sistema.liberarPosseBola(jogador.nome);

        assertTrue(liberou);
        assertFalse(jogador.comBola);
        assertNull(Ambiente.bola.emPosseDe);
    }

    @Test
    void localizaAliadoDentroDoRaioDePasse() {
        SistemaFutebol sistema = new SistemaFutebol(null);
        JogadorEstado passador = criarEstado("zagueiro", 20, 30, 100, Time.AZUL);
        JogadorEstado aliado = criarEstado("atacante", 40, 30, 100, Time.AZUL);
        JogadorEstado adversario = criarEstado("adversario", 42, 30, 0, Time.VERMELHO);
        sistema.getEstados().put(passador.nome, passador);
        sistema.getEstados().put(aliado.nome, aliado);
        sistema.getEstados().put(adversario.nome, adversario);

        Optional<String> resultado = sistema.localizarAliadoEmPosicaoDePasse(
                passador.nome, passador, Time.AZUL);

        assertEquals(Optional.of("atacante"), resultado);
    }

    @Test
    void localizaAliadoAtrasDoPassadorQuandoEstaDentroDoRaio() {
        SistemaFutebol sistema = new SistemaFutebol(null);
        JogadorEstado passador = criarEstado("zagueiro", 40, 30, 100, Time.AZUL);
        JogadorEstado aliado = criarEstado("atacante", 20, 30, 100, Time.AZUL);
        sistema.getEstados().put(passador.nome, passador);
        sistema.getEstados().put(aliado.nome, aliado);

        Optional<String> resultado = sistema.localizarAliadoEmPosicaoDePasse(
                passador.nome, passador, Time.AZUL);

        assertEquals(Optional.of("atacante"), resultado);
    }

    @Test
    void ignoraAliadoForaDoRaioDePasse() {
        SistemaFutebol sistema = new SistemaFutebol(null);
        JogadorEstado passador = criarEstado("zagueiro", 20, 30, 100, Time.AZUL);
        JogadorEstado aliado = criarEstado("atacante", 41, 30, 100, Time.AZUL);
        sistema.getEstados().put(passador.nome, passador);
        sistema.getEstados().put(aliado.nome, aliado);

        Optional<String> resultado = sistema.localizarAliadoEmPosicaoDePasse(
                passador.nome, passador, Time.AZUL);

        assertTrue(resultado.isEmpty());
        assertEquals(
                "RECEPTOR_FORA_DO_RAIO",
                sistema.identificarMotivoReceptorPasse(
                        passador.nome, passador, Time.AZUL, aliado.nome));
    }

    @Test
    void ignoraAliadoEmPenalidadeOuCooldown() {
        SistemaFutebol sistema = new SistemaFutebol(null);
        JogadorEstado passador = criarEstado("zagueiro", 20, 30, 100, Time.AZUL);
        JogadorEstado penalizado = criarEstado("penalizado", 35, 30, 100, Time.AZUL);
        penalizado.ticksPenalidadePerderDisputaRestantes = 1;
        JogadorEstado emCooldown = criarEstado("cooldown", 40, 30, 100, Time.AZUL);
        emCooldown.ticksCooldownPasse = 1;
        sistema.getEstados().put(passador.nome, passador);
        sistema.getEstados().put(penalizado.nome, penalizado);
        sistema.getEstados().put(emCooldown.nome, emCooldown);

        Optional<String> resultado = sistema.localizarAliadoEmPosicaoDePasse(
                passador.nome, passador, Time.AZUL);

        assertTrue(resultado.isEmpty());
    }

    private JogadorEstado criarEstado(
            String nome,
            double x,
            double y,
            double golX,
            Time time) {
        JogadorEstado estado = new JogadorEstado();
        estado.nome = nome;
        estado.x = x;
        estado.y = y;
        estado.golX = golX;
        estado.time = time.name();
        return estado;
    }
}
