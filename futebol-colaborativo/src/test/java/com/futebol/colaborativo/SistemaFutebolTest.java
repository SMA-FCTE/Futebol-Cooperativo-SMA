package com.futebol.colaborativo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.futebol.colaborativo.model.DisputaBolaEstado;
import com.futebol.colaborativo.model.JogadorEstado;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SistemaFutebolTest {

    @ParameterizedTest
    @CsvSource({
        "0,0,0,0,true",
        "0,0,1,0,true",
        "0,0,0,1,true",
        "0,0,1,1,false",
        "0,0,2,0,false"
    })
    void deveCalcularProximidadeManhattanParaDisputa(
        double primeiroX,
        double primeiroY,
        double segundoX,
        double segundoY,
        boolean esperado
    ) {
        assertEquals(
            esperado,
            SistemaFutebol.estaProximoParaDisputa(
                jogadorEm(primeiroX, primeiroY),
                jogadorEm(segundoX, segundoY)
            )
        );
    }

    @Test
    void deveRegistrarInicioDisputaSemJogadasEVencedor() {
        SistemaFutebol sistema = new SistemaFutebol(null);

        sistema.registrarInicioDisputa("disputa-1", 1, "azul", "vermelho");

        DisputaBolaEstado disputa = sistema.getUltimaDisputa();
        assertEquals("disputa-1", disputa.id);
        assertEquals(1, disputa.rodada);
        assertEquals("azul", disputa.jogador1);
        assertEquals("vermelho", disputa.jogador2);
        assertNull(disputa.jogada1);
        assertNull(disputa.jogada2);
        assertNull(disputa.vencedor);
        assertEquals(false, disputa.empate);
    }

    @Test
    void deveRegistrarEmpateDisputaComJogadas() {
        SistemaFutebol sistema = new SistemaFutebol(null);

        sistema.registrarEmpateDisputa("disputa-1", 1, "azul", "vermelho", "PEDRA", "PEDRA");

        DisputaBolaEstado disputa = sistema.getUltimaDisputa();
        assertEquals("PEDRA", disputa.jogada1);
        assertEquals("PEDRA", disputa.jogada2);
        assertNull(disputa.vencedor);
        assertEquals(true, disputa.empate);
        assertEquals("Empate: azul e vermelho jogaram PEDRA", disputa.resultado);
    }

    @Test
    void deveRegistrarResultadoFinalDisputa() {
        SistemaFutebol sistema = new SistemaFutebol(null);

        sistema.registrarResultadoDisputa("disputa-1", 2, "azul", "vermelho", "PAPEL", "PEDRA", "azul");

        DisputaBolaEstado disputa = sistema.getUltimaDisputa();
        assertEquals("disputa-1", disputa.id);
        assertEquals(2, disputa.rodada);
        assertEquals("PAPEL", disputa.jogada1);
        assertEquals("PEDRA", disputa.jogada2);
        assertEquals("azul", disputa.vencedor);
        assertEquals(false, disputa.empate);
        assertEquals("azul venceu com PAPEL contra PEDRA", disputa.resultado);
    }

    private static JogadorEstado jogadorEm(double x, double y) {
        JogadorEstado estado = new JogadorEstado();
        estado.x = x;
        estado.y = y;
        return estado;
    }
}
