package com.futebol.colaborativo.dto;

public class PasseEstadoDTO {
    public String id;
    public String passador;
    public String receptor;
    public String iniciador;
    public String status;
    public Double forcaX;
    public Double forcaY;
    public boolean recebido;
    public String motivoRecusa;
    public String resultado;

    public PasseEstadoDTO(
            String id,
            String passador,
            String receptor,
            String iniciador,
            String status,
            Double forcaX,
            Double forcaY,
            boolean recebido,
            String motivoRecusa,
            String resultado) {
        this.id = id;
        this.passador = passador;
        this.receptor = receptor;
        this.iniciador = iniciador;
        this.status = status;
        this.forcaX = forcaX;
        this.forcaY = forcaY;
        this.recebido = recebido;
        this.motivoRecusa = motivoRecusa;
        this.resultado = resultado;
    }
}
