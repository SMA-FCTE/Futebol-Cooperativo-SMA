package com.futebol.colaborativo.movimento;

import com.futebol.colaborativo.model.Ambiente;
import com.futebol.colaborativo.model.JogadorEstado;

public class Movimento {

    public static final double RAIO_CONTATO_BOLA = 1.2;
    // Raio em que um jogador vai atras de uma bola livre mesmo contra a sua
    // disciplina de zona: se a bola passa perto, ele a persegue.
    public static final double RAIO_PERSEGUICAO_BOLA = 10.0;
    public static final double RAIO_GOL = 2.0;

    public static void mover(JogadorEstado estado, double alvoX, double alvoY) {
        double deltaX = alvoX - estado.x;
        double deltaY = alvoY - estado.y;
        double distancia = calcularDistancia(estado.x, estado.y, alvoX, alvoY);

        if (distancia == 0) {
            return;
        }

        double passo = Math.min(estado.velocidade, distancia);
        estado.x += (deltaX / distancia) * passo;
        estado.y += (deltaY / distancia) * passo;
        limitarAoCampo(estado);
    }

    public static void conduzirBola(JogadorEstado estado, String jogador, double alvoX, double alvoY) {
        mover(estado, alvoX, alvoY);
        Ambiente.bola.posicionarComPosse(jogador, estado.x, estado.y);
    }

    public static boolean estaPerto(double origemX, double origemY, double alvoX, double alvoY, double raio) {
        return calcularDistancia(origemX, origemY, alvoX, alvoY) <= raio;
    }

    public static double calcularDistancia(double origemX, double origemY, double alvoX, double alvoY) {
        double deltaX = alvoX - origemX;
        double deltaY = alvoY - origemY;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public static void limitarAoCampo(JogadorEstado estado) {
        estado.x = limitar(estado.x, 0, Ambiente.largura);
        estado.y = limitar(estado.y, 0, Ambiente.altura);
    }

    private static double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(valor, maximo));
    }
}
