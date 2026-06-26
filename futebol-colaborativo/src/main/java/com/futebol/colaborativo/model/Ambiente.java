package com.futebol.colaborativo.model;

public class Ambiente {
    public static int largura = 300;
    public static int altura = 150;

    public static Bola bola = new Bola();

    public static int golX = largura;
    public static int golY = altura / 2;

    // Fracao da altura que o frontend usa para desenhar a goalArea: 0.36 * 0.56.
    private static final double FRACAO_ALTURA_GOL = 0.36 * 0.56;
    public static double golYMin = golY - (altura * FRACAO_ALTURA_GOL / 2);
    public static double golYMax = golY + (altura * FRACAO_ALTURA_GOL / 2);
}
