package com.futebol.colaborativo.model;

public class Bola {
    public double x;
    public double y;

    public Bola() {
        this.x = Ambiente.largura / 2.0;
        this.y = Ambiente.altura / 2.0;
    }
}
