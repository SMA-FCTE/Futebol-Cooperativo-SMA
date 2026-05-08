package com.futebol.colaborativo.model;

public class Bola {
    public double x;
    public double y;
    public double velocidadeX;
    public double velocidadeY;
    public String emPosseDe;

    public Bola() {
        this.x = Ambiente.largura / 2.0;
        this.y = Ambiente.altura / 2.0;
    }

    public void posicionarNoCentro() {
        this.x = Ambiente.largura / 2.0;
        this.y = Ambiente.altura / 2.0;
        this.velocidadeX = 0;
        this.velocidadeY = 0;
        this.emPosseDe = null;
    }

    public void posicionarComPosse(String jogador, double x, double y) {
        this.x = x;
        this.y = y;
        this.velocidadeX = 0;
        this.velocidadeY = 0;
        this.emPosseDe = jogador;
    }

    public void soltarComForca(double forcaX, double forcaY) {
        this.velocidadeX += forcaX;
        this.velocidadeY += forcaY;
        this.emPosseDe = null;
    }

}
