package com.futebol.colaborativo.agentes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JogadorAgentTest {

    @ParameterizedTest
    @CsvSource({
        "PEDRA,TESOURA",
        "TESOURA,PAPEL",
        "PAPEL,PEDRA"
    })
    void deveIdentificarVitoriaDaJogadaLocal(String local, String oponente) {
        assertEquals(
            JogadorAgent.ResultadoDisputa.VITORIA_LOCAL,
            JogadorAgent.compararJogadas(
                JogadorAgent.JogadaDisputa.valueOf(local),
                JogadorAgent.JogadaDisputa.valueOf(oponente)
            )
        );
    }

    @ParameterizedTest
    @CsvSource({
        "TESOURA,PEDRA",
        "PAPEL,TESOURA",
        "PEDRA,PAPEL"
    })
    void deveIdentificarVitoriaDaJogadaOponente(String local, String oponente) {
        assertEquals(
            JogadorAgent.ResultadoDisputa.VITORIA_OPONENTE,
            JogadorAgent.compararJogadas(
                JogadorAgent.JogadaDisputa.valueOf(local),
                JogadorAgent.JogadaDisputa.valueOf(oponente)
            )
        );
    }

    @Test
    void deveEmpatarQuandoJogadasSaoIguais() {
        assertEquals(
            JogadorAgent.ResultadoDisputa.EMPATE,
            JogadorAgent.compararJogadas(
                JogadorAgent.JogadaDisputa.PEDRA,
                JogadorAgent.JogadaDisputa.PEDRA
            )
        );
    }
}
