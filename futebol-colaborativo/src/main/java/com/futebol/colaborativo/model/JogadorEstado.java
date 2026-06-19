package com.futebol.colaborativo.model;

public class JogadorEstado {
    public String nome;
    public double x;
    public double y;

    public double velocidade = 1.0;

    public boolean comBola = false;
    public int ticksPenalidadePerderDisputaRestantes = 0;
    public double golX;

    public String time;
    public String papel;
    public boolean aguardandoPasse;
    public int ticksCooldownPasse;
}
